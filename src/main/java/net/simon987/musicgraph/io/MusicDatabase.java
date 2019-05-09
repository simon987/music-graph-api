package net.simon987.musicgraph.io;

import com.google.common.collect.ImmutableList;
import net.simon987.musicgraph.entities.*;
import net.simon987.musicgraph.logging.LogManager;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

import javax.inject.Singleton;
import java.util.*;
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

        logger.info(String.format("Query %s (Took %dms)", query, took));

        return result;
    }

    public ArtistDetails getArtistDetails(String mbid) {

        Map<String, Object> params = new HashMap<>();
        params.put("mbid", mbid);

        try (Session session = driver.session()) {

            StatementResult result = query(session,
                    "MATCH (a:Artist {id: $mbid})-[:CREDITED_FOR]->(r:Release)\n" +
                            "WITH collect(r.id) as releases, a\n" +
                            "OPTIONAL MATCH (a)-[r:IS_TAGGED]->(t:Tag)\n" +
                            "RETURN a {name:a.name, releases:releases, tags:collect({weight: r.weight, name: t.name})}\n" +
                            "LIMIT 1",
                    params);

            ArtistDetails details = new ArtistDetails();

            try {

                if (result.hasNext()) {
                    Map<String, Object> map = result.next().get("a").asMap();

                    details.name = (String) map.get("name");
                    details.releases.addAll((Collection<String>) map.get("releases"));
                    details.tags.addAll(
                            ((List<Map>) map.get("tags"))
                                    .stream()
                                    .filter(x -> x.get("name") != null)
                                    .map(x -> new Tag(
                                            (String) x.get("name"),
                                            (Long) x.get("weight")
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

    public SearchResult getRelated(String mbid) {

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

            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Artist makeArtist(Node node) {
        Artist artist = new Artist();
        artist.id = node.id();
        artist.mbid = node.get("id").asString();
        artist.name = node.get("name").asString();
        artist.labels = ImmutableList.copyOf(node.labels());
        artist.listeners = node.get("listeners").asInt();
        artist.playCount = node.get("playcount").asInt();
        return artist;
    }

    private static Relation makeRelation(Relationship rel) {
        Relation relation = new Relation();

        relation.source = rel.startNodeId();
        relation.target = rel.endNodeId();
        relation.weight = rel.get("weight").asFloat();

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
                            "tags:collect({weight:r.weight, name:t.name}), artist: a.name}\n" +
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
                                            (String) x.get("name"),
                                            (Long) x.get("weight")
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
