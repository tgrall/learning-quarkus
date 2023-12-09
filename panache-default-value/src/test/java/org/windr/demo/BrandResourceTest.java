package org.windr.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;



@QuarkusTest
public class BrandResourceTest {

    @Inject
    ObjectMapper objectMapper;

    @Test
    void logoValueTest() throws Exception {

        given()
                .when().get("/api/v1/brands/1")
                .then()
                .statusCode(200)
                .body(
                    "name", is("Apple"),
                    "logo", is("apple.png")
                );

        given()
                .when().get("/api/v1/brands/2")
                .then()
                .statusCode(200)
                .body(
                    "name", is("Samsung"),
                    "logo", is("default-logo.png")
                );



        Brand fanatic = new Brand();
        fanatic.name = "Fanatic";
        fanatic.description = "Fanatic Description";
        fanatic.logo = "fanatic.png";

        String fanaticString = objectMapper.writeValueAsString(fanatic);

        given()
                .body(fanaticString)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/api/v1/brands")
                .then()
                .statusCode(201)
                .body(
                        "name", is(fanatic.name),
                        "description", is(fanatic.description),
                        "logo", is(fanatic.logo)
                );


        Brand fanaticWithoutLogo = new Brand();
        fanaticWithoutLogo.name = "Fanatic";
        fanaticWithoutLogo.description = "Fanatic Description";

        String fanaticWithoutLogoString = objectMapper.writeValueAsString(fanaticWithoutLogo);
        given()
                .body(fanaticWithoutLogoString)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when().post("/api/v1/brands")
                .then()
                .statusCode(201)
                .body(
                        "name", is(fanaticWithoutLogo.name),
                        "description", is(fanaticWithoutLogo.description),
                        "logo", is("default-logo.png")
                );


    }

}