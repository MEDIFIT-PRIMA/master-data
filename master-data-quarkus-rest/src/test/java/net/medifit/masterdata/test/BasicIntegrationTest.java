package net.medifit.masterdata.test;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.medifit.masterdata.api.MasterDataResource;
import net.medifit.masterdata.api.MasterDataSchemaResource;
import org.junit.jupiter.api.Test;

import java.net.URL;

@QuarkusTest
public class BasicIntegrationTest {

    @TestHTTPEndpoint(MasterDataSchemaResource.class)
    @TestHTTPResource
    URL schemaUrl;

    @TestHTTPEndpoint(MasterDataResource.class)
    @TestHTTPResource
    URL dataUrl;

    @Test
    public void createSchemaTest() {
        final Response response =
                RestAssured.given()
                        .contentType(ContentType.JSON)
                        .body(getClass().getResourceAsStream("/product-class.json"))
                        .when()
                        .post(schemaUrl.toString().concat("/test/ProductClass"));
    }

}
