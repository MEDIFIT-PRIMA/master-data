package net.medifit.masterdata.test;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import net.medifit.masterdata.Repository;
import net.medifit.masterdata.api.MasterDataResource;
import net.medifit.masterdata.api.MasterDataSchemaResource;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

@QuarkusTest
public class BasicIntegrationTest {

    @TestHTTPEndpoint(MasterDataSchemaResource.class)
    @TestHTTPResource
    URL schemaUrl;

    @TestHTTPEndpoint(MasterDataResource.class)
    @TestHTTPResource
    URL dataUrl;

    @Inject
    Repository repository;

    @Test
    @Order(1)
    public void createSchemaTest() {
        final Response response =
                RestAssured.given()
                        .contentType(ContentType.JSON)
                        .body(getClass().getResourceAsStream("/product-class.json"))
                        .when()
                        .post(schemaUrl.toString().concat("/test/TestNewProductClass"));
        assertEquals(201, response.statusCode());
    }
    @Test
    @Order(2)
    public void countSchemaTest() {
        final Response response =
                RestAssured.given()
                        .when()
                        .get(schemaUrl.toString());
        assertEquals(200, response.statusCode());
        assertEquals(
                1, response.jsonPath().getList("type").stream().filter("TestNewProductClass"::equals).count());
//        System.out.println(response.body().asString());
    }


    @Test
    @Order(3)
    public void deleteSchemaTest() throws InterruptedException, IOException {
        Response response =
                RestAssured.given()
                        .when()
                        .delete(schemaUrl.toString().concat("/test/TestNewProductClass"));
        assertEquals(200, response.statusCode());
        repository.refresh();
        response =
                RestAssured.given()
                        .when()
                        .get(schemaUrl.toString());
        assertEquals(200, response.statusCode());
        assertEquals(
                0, response.jsonPath().getList("type").stream().filter("TestNewProductClass"::equals).count());

    }

}
