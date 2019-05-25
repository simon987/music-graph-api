package net.simon987.musicgraph.io;

import com.google.common.collect.ImmutableList;
import net.simon987.musicgraph.entities.*;
import net.simon987.musicgraph.logging.LogManager;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class MusicDatabase extends AbstractBinder {

    private Driver driver;

    private static Logger logger = LogManager.getLogger();

    @Override
    protected void configure() {
        bind(this).to(MusicDatabase.class);
    }

    public MusicDatabase() {
        driver = GraphDatabase.driver("bolt://localhost:7687",
                AuthTokens.basic("neo4j", "neo4j"));
    }

    private StatementResult query(Session session, String query, Map<String, Object> args) {

        long start = System.nanoTime();
        StatementResult result = session.run(query, args);
        long end = System.nanoTime();
        long took = (end - start) / 1000000;

        logger.info(String.format("Query %s (Took %dms)", query.replace('\n', ' '), took));

        return result;
    }


    public SearchResult getArtistMembers(String mbid) {

        try (Session session = driver.session()) {

            Map<String, Object> params = new HashMap<>();
            params.put("mbid", mbid);

            StatementResult result = query(session,
                    "MATCH (a:Artist)-[r:IS_MEMBER_OF]-(b:Artist) " +
                            "WHERE a.id = $mbid " +
                            "RETURN a as artist, a {rels: collect(DISTINCT r), nodes: collect(DISTINCT b)} as rank1\n" +
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
                    "MATCH (a:Artist {id: $mbid})-[:CREDITED_FOR]->(r:Release)\n" +
                            "WITH collect({id: ID(r), mbid:r.id, name:r.name, year:r.year, labels:labels(r)}) as releases, a\n" +
                            "OPTIONAL MATCH (a)-[r:IS_TAGGED]->(t:Tag)\n" +
                            "RETURN a {name:a.name, releases:releases, tags:collect({weight: r.weight, name: t.name, id:ID(t)})}\n" +
                            "LIMIT 1",
                    params);

            ArtistDetails details = new ArtistDetails();

            try {

                if (result.hasNext()) {
                    Map<String, Object> map = result.next().get("a").asMap();

                    details.name = (String) map.get("name");
                    details.releases.addAll(
                            ((List<Map>) map.get("releases"))
                                    .stream()
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
                                    .map(x -> new Tag(
                                            (Long) x.get("id"),
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

    public SearchResult getRelatedById(String mbid) {

        try (Session session = driver.session()) {

            Map<String, Object> params = new HashMap<>();
            params.put("mbid", mbid);

            StatementResult result = query(session,
                    "MATCH (a:Artist)-[r:IS_RELATED_TO]-(b)\n" +
                            "WHERE a.id = $mbid\n" +
                            // Only match artists with > 0 releases
                            "MATCH (b)-[:CREDITED_FOR]->(:Release)\n" +
                            "WHERE r.weight > 0.25\n" +
                            "RETURN a as artist, a {rels: collect(DISTINCT r), nodes: collect(DISTINCT b)} as rank1\n" +
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

    public SearchResult getRelatedByName(String name) {

        try (Session session = driver.session()) {

            Map<String, Object> params = new HashMap<>();
            params.put("name", name);

            StatementResult result = query(session,
                    "MATCH (a:Artist)-[r:IS_RELATED_TO]-(b)\n" +
                            "WHERE a.name = $name\n" +
                            // Only match artists with > 0 releases
                            "MATCH (b)-[:CREDITED_FOR]->(:Release)\n" +
                            "WHERE r.weight > 0.25\n" +
                            "RETURN a as artist, a {rels: collect(DISTINCT r), nodes: collect(DISTINCT b)} as rank1\n" +
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

    private void parseRelatedResult(StatementResult result, SearchResult out) {
        long start = System.nanoTime();
        while (result.hasNext()) {
            Record row = result.next();
            out.artists.add(makeArtist(row.get(0).asNode()));

            var rank1 = row.get(1).asMap();

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
        long end = System.nanoTime();
        long took = (end - start) / 1000000;
        logger.info(String.format("Fetched search result (Took %dms)", took));
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
                    "MATCH (release:Release {id: $mbid})-[:CREDITED_FOR]-(a:Artist)\n" +
                            "OPTIONAL MATCH (release)-[r:IS_TAGGED]->(t:Tag)\n" +
                            "RETURN release {name:release.name, year:release.year," +
                            "tags:collect({weight:r.weight, name:t.name, id:ID(t)}), artist: a.name}\n" +
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
                                    .map(x -> new Tag(
                                            (Long) x.get("id"),
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

}
