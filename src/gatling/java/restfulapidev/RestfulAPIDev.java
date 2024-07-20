package restfulapidev;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import templates.Templates;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class RestfulAPIDev extends Simulation {

    String baseUrl = System.getProperty("baseUrl", "https://api.restful-api.dev");
    String concurrentUsers = System.getProperty("concurrentUsers", "5");
    String durationOfSeconds = System.getProperty("durationOfSeconds", "5");

    FeederBuilder<String> feeder = csv("data/object.csv").random();
    ObjectMapper objectMapper = new ObjectMapper();

    private HttpProtocolBuilder httpProtocol = http
            .baseUrl(baseUrl).contentTypeHeader("application/json");

    ChainBuilder create = exec(
            http("Create object #{name}")
                .post("/objects")
                .body(StringBody(Templates.createObjectPayload("#{name}",
                        "#{year}",
                        "#{price}",
                        "#{model}",
                        "#{disk}")))
                .check(status().is(200))
                .check(jsonPath("$.id").exists())
                .check(jmesPath("name").isEL("#{name}"))
                .check(jmesPath("id").find().saveAs("id"))
    );

    //GET OBJECT
    ChainBuilder get = exec(session -> session.set("id", session.getString("id")))
            .exec(
                    http("Get object by id: #{id}")
                            .get("/objects/#{id}")
                            .check(status().is(200))
                            .check(jsonPath("$.id").exists())
                            .check(jmesPath("name").isEL("#{name}"))
            );

    ChainBuilder update = exec(session -> session.set("id", session.getString("id")))
            .exec(
                    http("Update Object #{id}")
                            .put("/objects/#{id}")
                            .body(StringBody(Templates.createObjectPayload("UPDATE - #{name}",
                                    "#{year}",
                                    "#{price}",
                                    "#{model}",
                                    "#{disk}")))
                            .check(status().is(200))
                            .check(jsonPath("$.id").exists())
                            .check(jmesPath("name").isEL("UPDATE - #{name}"))
            );

    ChainBuilder getUpdate = exec(session -> session.set("id", session.getString("id")))
            .exec(
                    http("Get object by id: #{id}")
                            .get("/objects/#{id}")
                            .check(status().is(200))
                            .check(jsonPath("$.id").exists())
                            .check(jmesPath("name").isEL("UPDATE - #{name}"))
            );

    ScenarioBuilder scn = scenario("Restful API Dev").feed(feeder).exec(create, get, update, getUpdate);

    {
        setUp(
//                scn.injectOpen(
////                        atOnceUsers(10),
////                        nothingFor(Duration.ofSeconds(5)),
////                        rampUsers(10).during(Duration.ofSeconds(10)),
////                        constantUsersPerSec(20).during(Duration.ofSeconds(10)
//                        )
//                )
                scn.injectClosed(
                        constantConcurrentUsers(Integer.parseInt(concurrentUsers))
                                .during(Duration.ofSeconds(Integer.parseInt(durationOfSeconds)))
                )
        ).protocols(httpProtocol);
    }
}
