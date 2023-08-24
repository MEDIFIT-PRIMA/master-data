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

import org.junit.Ignore;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URL;

@QuarkusTest
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    @Disabled
    @Order(1)

    public void createSchemaTest() {
        System.out.println("--------" + schemaUrl.toString().concat("/test/TestNewProductClass"));
        final Response response =
                RestAssured.given()
                        .contentType(ContentType.JSON)
                        .body(getClass().getResourceAsStream("/product-class.json"))
                        .when()
                        .post(schemaUrl.toString().concat("/test/TestNewProductClass"));
        assertEquals(201, response.statusCode());
    }

    @Test
    @Disabled
    @Order(2)

    public void countSchemaTest() {
        final Response response =
                RestAssured.given()
                        .when()
                        .get(schemaUrl.toString());
        assertEquals(200, response.statusCode());
        assertEquals(
                1, response.jsonPath().getList("type").stream().filter("sample"::equals).count());
//        System.out.println(response.body().asString());
    }



    @Test
    @Disabled
    @Order(3)

    public void deleteSchemaTest() throws InterruptedException, IOException {
        Response response =
                RestAssured.given()
                        .when()
                        .delete(schemaUrl.toString().concat("/openbis/sample"));
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

    @Test
    @Disabled
    public void postDataWithGroupTypeTest() {
        final Response response =
                RestAssured.given()
                        .contentType(ContentType.JSON)
                        .body(getClass().getResourceAsStream("/product-class-example.json"))
                        .when()
                        .post(dataUrl.toString().concat("/test/TestNewProductClass"));
        assertEquals(201, response.statusCode());
    }


    @Test
    @Disabled
    public void postDataWithGroupTypeIdTest() {
        final Response response =
                RestAssured.given()
                        .contentType(ContentType.JSON)
                        .body(getClass().getResourceAsStream("/product_data_load.json"))
                        .when()
                        .post(dataUrl.toString().concat("/openbis/sample/product_data_load"));
        assertEquals(201, response.statusCode());
    }


    @Test
    @Disabled
    public void getDataTest() {
        final Response response =
                RestAssured.given()
                        .when()

                        .get(dataUrl.toString().concat("/openbis/sample/").concat("product_data_load"));
        assertEquals(200, response.statusCode());

        assertEquals(
                true, response.jsonPath().get("permid").equals("3342209231820153141223233-116"));
//        System.out.println(response.body().asString());
    }


    @Test
    @Disabled
    public void getDataByVersionTest() {
        final Response response =
                RestAssured.given()
                        .when()

                        .get(dataUrl.toString().concat("/openbis/sample/").concat("111111232341202209231820153141223233-116").concat("?versionId=1e2c1701-8886-433d-8dec-79b99b336a26"));
        assertEquals(200, response.statusCode());
        assertEquals(
                true, response.jsonPath().get("permid").equals("111111232341202209231820153141223233-116"));

    }

    @Test
    @Disabled
    public void postInvalidDataTest() {
        final Response response =
                RestAssured.given()
                        .contentType(ContentType.JSON)
                        .body(getClass().getResourceAsStream("/invalid_product_class.json"))
                        .when()
                        .post(dataUrl.toString().concat("/test/TestNewProductClass/invalid_product_class"));
        assertEquals(500, response.statusCode());
    }


}


