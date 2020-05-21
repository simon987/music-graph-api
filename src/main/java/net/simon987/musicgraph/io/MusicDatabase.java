package net.simon987.musicgraph.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import net.simon987.musicgraph.entities.*;
import net.simon987.musicgraph.logging.LogManager;
import net.simon987.musicgraph.webapi.AutoCompleteData;
import net.simon987.musicgraph.webapi.AutocompleteLine;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

import javax.inject.Singleton;
import javax.validation.constraints.Max;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class MusicDatabase extends AbstractBinder {

    private Driver driver;
    private static final int MaxShortestPaths = 5;

    private static Logger logger = LogManager.getLogger();

    @Override
    protected void configure() {
        bind(this).to(MusicDatabase.class);
    }

    public MusicDatabase() {
        driver = GraphDatabase.driver("bolt://" + System.getenv("NEO4J_ADDR"),
                AuthTokens.basic("neo4j", "neo4j"));
    }

    private StatementResult query(Session session, String query, Map<String, Object> args) {

        long start = System.nanoTime();
        StatementResult result = session.run(query, args);
        long end = System.nanoTime();
        long took = (end - start) / 1000000;

        logger.info(String.format("Query %s (Took %dms)", query, took));

        return result;
    }


    public SearchResult getArtistMembers(String mbid) {

        try (Session session = driver.session()) {

            Map<String, Object> params = new HashMap<>();
            params.put("mbid", mbid);

            StatementResult result = query(session,
                    "MATCH (a:Artist)-[r:IS_MEMBER_OF]-(b:Artist) " +
                            "WHERE a.id = $mbid " +
                            "RETURN a as artist, a {rels: collect(DISTINCT r), nodes: collect(DISTINCT b)} as rank1 " +
                            "LIMIT 1",
                    params);

            SearchResult out = new SearchResult();

            parseRelatedResult(result, out);

            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArtistDetails getArtistDetails(String mbid) {

        Map<String, Object> params = new HashMap<>();
        params.put("mbid", mbid);

        try (Session session = driver.session()) {

            StatementResult result = query(session,
                    "MATCH (a:Artist {id: $mbid})" +
                            "WITH a OPTIONAL MATCH (a)-[:CREDITED_FOR]->(r:Release) " +
                            "WITH collect({id: ID(r), mbid:r.id, name:r.name, year:r.year, labels:labels(r)}) as releases, a " +
                            "OPTIONAL MATCH (a)-[r:IS_TAGGED]->(t:Tag) " +
                            "WITH collect({weight: r.weight, name: t.name, id:ID(t), tagid:t.id}) as tags, a, releases " +
                            "OPTIONAL MATCH (a)-[r:CREDITED_FOR]->(:Release)-[]-(l:Label) " +
                            "RETURN a {name:a.name, year:a.year, comment:a.comment, releases:releases, tags:tags," +
                            " track_previews:a.track_previews, labels:collect(DISTINCT {id:ID(l),mbid:l.id,name:l.name})} " +
                            "LIMIT 1",
                    params);

            ArtistDetails details = new ArtistDetails();

            try {

                if (result.hasNext()) {
                    Map<String, Object> map = result.next().get("a").asMap();

                    details.name = (String) map.get("name");
                    details.comment = (String) map.get("comment");
                    details.year = (long) map.get("year");
                    details.releases.addAll(
                            ((List<Map>) map.get("releases"))
                                    .stream()
                                    .filter(x -> x.get("name") != null)
                                    .map(x -> new Release(
                                            (List<String>) x.get("labels"),
                                            (String) x.get("mbid"),
                                            (String) x.get("name"),
                                            (Long) x.get("year"),
                                            (Long) x.get("id")
                                    )).collect(Collectors.toList())

                    );
                    details.tags.addAll(
                            ((List<Map>) map.get("tags"))
                                    .stream()
                                    .filter(x -> x.get("name") != null)
                                    .map(x -> new WeightedTag(
                                            (Long) x.get("id"),
                                            Long.valueOf((String) x.get("tagid")),
                                            (String) x.get("name"),
                                            (Double) x.get("weight")
                                    ))
                                    .collect(Collectors.toList())
                    );
                    details.labels.addAll(
                            ((List<Map>) map.get("labels"))
                                    .stream()
                                    .filter(x -> x.get("id") != null)
                                    .map(x -> new Label(
                                            (Long) x.get("id"),
                                            (String) x.get("mbid"),
                                            (String) x.get("name")
                                    ))
                                    .collect(Collectors.toList())
                    );
                    String preview_urls = (String) map.get("track_previews");
                    if (preview_urls != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        details.spotifyPreviewUrls = mapper.readValue(preview_urls, SpotifyPreviewUrl[].class);
                    } else {
                        details.spotifyPreviewUrls = new SpotifyPreviewUrl[0];
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return details;
        }
    }

    public SearchResult getRelatedById(String mbid) {

        try (Session session = driver.session()) {

            Map<String, Object> params = new HashMap<>();
            params.put("mbid", mbid);

            StatementResult result = query(session,
                    "MATCH (a:Artist) " +
                            "WHERE a.id = $mbid " +
                            "WITH a OPTIONAL MATCH (a)-[r:IS_RELATED_TO]-(b) " +
                            "WHERE r.weight > 0.15 " +
                            "RETURN a as artist, {rels: collect(r), nodes: collect(b)} as rank1 " +
                            "LIMIT 1",
                    params);

            SearchResult out = new SearchResult();

            parseRelatedResult(result, out);

            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public SearchResult getPath(String idFrom, String idTo) {

        try (Session session = driver.session()) {

            Map<String, Object> params = new HashMap<>();
            params.put("idFrom", idFrom);
            params.put("idTo", idTo);

            StatementResult result = query(session,
                    "MATCH p = allShortestPaths(" +
                            "(:Artist {id: $idFrom})-[r:IS_RELATED_TO*..10]-(:Artist {id:$idTo})) " +
                            "WHERE ALL (rel in r WHERE rel.weight > 0.10) " +
                            "return {rels: relationships(p), nodes: nodes(p)} as rank1",
                    params);

            SearchResult out = new SearchResult();
            int count = 0;
            while (result.hasNext() && count <= MaxShortestPaths) {
                Record row = result.next();
                artistsFromRelMap(out, row.get(0).asMap());
                count += 1;
            }

            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public TagSearchResult getRelatedByTag(long id) {

        try (Session session = driver.session()) {

            Map<String, Object> params = new HashMap<>();
            params.put("tag_id", id);

            StatementResult result = query(session,
                    "MATCH (t:Tag)-[r:IS_TAGGED]-(a:Artist) " +
                            "WHERE ID(t) = $tag_id " +
                            // Is rels really necessary?
                            "RETURN t, {rels: collect(DISTINCT r), nodes: collect(DISTINCT a)} as rank1 " +
                            "LIMIT 1",
                    params);

            TagSearchResult out = new TagSearchResult();

            if (result.hasNext()) {
                Record row = result.next();
                out.tag = makeTag(row.get(0).asNode());
                artistsFromRelMap(out, row.get(1).asMap());
            }

            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public LabelSearchResult getRelatedByLabel(long id) {

        try (Session session = driver.session()) {

            Map<String, Object> params = new HashMap<>();
            params.put("label_id", id);

            StatementResult result = query(session,
                    "MATCH (l:Label)-[]-(:Release)-[:CREDITED_FOR]-(a:Artist) " +
                            "WHERE ID(l) = $label_id " +
                            "RETURN l, {nodes: collect(DISTINCT a)} as rank1 " +
                            "LIMIT 1",
                    params);

            LabelSearchResult out = new LabelSearchResult();

            if (result.hasNext()) {
                Record row = result.next();
                out.label = makeLabel(row.get(0).asNode());
                artistsFromRelMap(out, row.get(1).asMap());
            }

            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public RelatedLabels getRelatedLabelByLabel(long id) {

        try (Session session = driver.session()) {

            Map<String, Object> params = new HashMap<>();
            params.put("label_id", id);

            StatementResult result = query(session,
                    "MATCH (l:Label)-[r:IS_RELATED_TO]-(l2:Label) " +
                            "WHERE ID(t) = $tag_id " +
                            "RETURN {rels: collect(DISTINCT r), nodes: collect(DISTINCT t2)} as rank1 " +
                            "LIMIT 1",
                    params);

            RelatedLabels out = new RelatedLabels();

            if (result.hasNext()) {
                Record row = result.next();
                labelsFromRelMap(out, row.get(0).asMap());
            }

            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public RelatedTags getRelatedTagByTag(long id) {

        try (Session session = driver.session()) {

            Map<String, Object> params = new HashMap<>();
            params.put("tag_id", id);

            StatementResult result = query(session,
                    "MATCH (t:Tag)-[r:IS_RELATED_TO]-(t2:Tag) " +
                            "WHERE ID(t) = $tag_id " +
                            "RETURN {rels: collect(DISTINCT r), nodes: collect(DISTINCT t2)} as rank1 " +
                            "LIMIT 1",
                    params);

            RelatedTags out = new RelatedTags();

            if (result.hasNext()) {
                Record row = result.next();
                tagsFromRelMap(out, row.get(0).asMap());
            }

            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void parseRelatedResult(StatementResult result, SearchResult out) {
        long start = System.nanoTime();
        if (result.hasNext()) {
            Record row = result.next();
            out.artists.add(makeArtist(row.get(0).asNode()));

            artistsFromRelMap(out, row.get(1).asMap());
        }
        long end = System.nanoTime();
        long took = (end - start) / 1000000;
        logger.info(String.format("Fetched search result (Took %dms)", took));
    }

    private void artistsFromRelMap(SearchResult out, Map<String, Object> rank1) {
        out.relations.addAll(((List<Relationship>) rank1.get("rels"))
                .stream()
                .map(MusicDatabase::makeRelation)
                .collect(Collectors.toList()
                ));
        out.artists.addAll(((List<Node>) rank1.get("nodes"))
                .stream()
                .map(MusicDatabase::makeArtist)
                .collect(Collectors.toList()
                ));
    }

    private void tagsFromRelMap(RelatedTags out, Map<String, Object> rank1) {
        out.relations.addAll(((List<Relationship>) rank1.get("rels"))
                .stream()
                .map(MusicDatabase::makeRelation)
                .collect(Collectors.toList()
                ));
        out.tags.addAll(((List<Node>) rank1.get("nodes"))
                .stream()
                .map(MusicDatabase::makeTag)
                .collect(Collectors.toList()
                ));
    }

    private void labelsFromRelMap(RelatedLabels out, Map<String, Object> rank1) {
        out.relations.addAll(((List<Relationship>) rank1.get("rels"))
                .stream()
                .map(MusicDatabase::makeRelation)
                .collect(Collectors.toList()
                ));
        out.labels.addAll(((List<Node>) rank1.get("nodes"))
                .stream()
                .map(MusicDatabase::makeLabel)
                .collect(Collectors.toList()
                ));
    }

    private static Artist makeArtist(Node node) {
        Artist artist = new Artist();
        artist.id = node.id();
        artist.mbid = node.get("id").asString();
        artist.name = node.get("name").asString();
        artist.labels = ImmutableList.copyOf(node.labels());
        if (node.containsKey("listeners")) {
            artist.listeners = node.get("listeners").asInt();
            artist.playCount = node.get("playcount").asInt();
        }
        return artist;
    }

    private static Tag makeTag(Node node) {
        return new Tag(
                node.id(),
                Long.valueOf(node.get("id").asString()),
                node.get("name").asString()
        );
    }

    private static Label makeLabel(Node node) {
        return new Label(
                node.id(),
                node.get("id").asString(),
                node.get("name").asString()
        );
    }

    private static Relation makeRelation(Relationship rel) {
        Relation relation = new Relation();

        relation.source = rel.startNodeId();
        relation.target = rel.endNodeId();
        if (rel.containsKey("weight")) {
            relation.weight = rel.get("weight").asFloat();
        }

        return relation;
    }

    public ReleaseDetails getReleaseDetails(String mbid) {

        Map<String, Object> params = new HashMap<>();
        params.put("mbid", mbid);

        try (Session session = driver.session()) {

            StatementResult result = query(session,
                    "MATCH (release:Release {id: $mbid})-[:CREDITED_FOR]-(a:Artist) " +
                            "OPTIONAL MATCH (release)-[r:IS_TAGGED]->(t:Tag) " +
                            "RETURN release {name:release.name, year:release.year," +
                            "tags:collect({weight:r.weight, name:t.name, id:ID(t), tagid:t.id}), artist: a.name} " +
                            "LIMIT 1",
                    params);

            ReleaseDetails details = new ReleaseDetails();

            try {

                if (result.hasNext()) {
                    Map<String, Object> map = result.next().get("release").asMap();

                    details.mbid = mbid;
                    details.name = (String) map.get("name");
                    details.artist = (String) map.get("artist");
                    details.year = (long) map.get("year");
                    details.tags.addAll(
                            ((List<Map>) map.get("tags"))
                                    .stream()
                                    .filter(x -> x.get("name") != null)
                                    .map(x -> new WeightedTag(
                                            (Long) x.get("id"),
                                            Long.valueOf((String) x.get("tagid")),
                                            (String) x.get("name"),
                                            (Double) x.get("weight")
                                    ))
                                    .collect(Collectors.toList())
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return details;
        }
    }

    public AutoCompleteData autoComplete(String prefix) {

        Map<String, Object> params = new HashMap<>();
        params.put("prefix", prefix);

        try (Session session = driver.session()) {

            AutoCompleteData data = new AutoCompleteData();

            StatementResult result = query(session,
                    "MATCH (a:Artist) " +
                            "WHERE a.sortname STARTS WITH $prefix " +
                            "RETURN a ORDER BY a.listeners DESC " +
                            "LIMIT 30",
                    params);

            while (result.hasNext()) {
                Map<String, Object> map = result.next().get("a").asMap();

                AutocompleteLine line = new AutocompleteLine();
                line.name = (String) map.get("name");
                line.type = "artist";
                line.comment = (String) map.get("comment");
                line.year = (long) map.get("year");
                line.id = (String) map.get("id");

                data.lines.add(line);
            }

            params.put("prefix", prefix.toLowerCase());
            StatementResult tagResult = query(session,
                    "MATCH (t:Tag)-[:IS_TAGGED]-(:Artist) " +
                            "WHERE t.name STARTS WITH $prefix " +
                            "RETURN DISTINCT t ORDER BY t.occurrences DESC " +
                            "LIMIT 15",
                    params);

            while (tagResult.hasNext()) {
                Node node = tagResult.next().get("t").asNode();

                AutocompleteLine line = new AutocompleteLine();
                line.name = node.get("name").asString();
                line.type = "tag";
                line.id = String.valueOf(node.id());

                // Interlace tags with the artists, keeping listeners order
                if (data.lines.size() > 0) {
                    for (int i = 0; i < data.lines.size(); i++) {
                        if (data.lines.get(i).name.toLowerCase().compareTo(line.name) > 0) {
                            data.lines.add(i + 1, line);
                            break;
                        }
                    }
                } else {
                    data.lines.add(line);
                }
            }

            return data;
        }
    }
}
