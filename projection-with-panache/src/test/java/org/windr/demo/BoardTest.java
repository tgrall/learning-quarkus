package org.windr.demo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class BoardTest {

    @Test
    void getBoards() {
        given()
                .when().get("/api/v1/boards")
                .then()
                .statusCode(200)
                .body(
                "$.size()", is(2),
                        "$[0]", not(hasKey("description")),
                        "$[0]", not(hasKey("brand_id"))
                );
    }

    @Test
    void getBoardOne() {
        given()
                .when().get("/api/v1/boards/1")
                .then()
                .statusCode(200)
                .body(
                    "brandName", is("Duotone")
                );
    }


}