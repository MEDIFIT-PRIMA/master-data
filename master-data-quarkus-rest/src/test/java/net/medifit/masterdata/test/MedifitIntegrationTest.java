package net.medifit.masterdata.test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class MedifitIntegrationTest {

    //In progress class will update the url to https://masterdata.medifit-prima.net/masterdata

    String url="http://localhost:8081/masterdata";


    @Test
    public void postAllFilesInSchemsDirectory() {
        String path="/schemas/openbis/";
        List<File> files = getFilesFromDirectory("/schema"); // Update the directory path as needed
        for (File file : files) {
            try {
                String fileContent = FileUtils.readFileToString(file, "UTF-8");
                String schemaname = file.getName().replace(".json", "");
                postJsonData(schemaname, fileContent,path);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void postAllDataFilesDirectory() {
        String path="/openbis/product_data_schema/";
        List<File> files = getFilesFromDirectory("/datatoload"); // Update the directory path as needed
        for (File file : files) {
            try {
                String fileContent = FileUtils.readFileToString(file, "UTF-8");
                String schemaname = file.getName().replace(".json", "");
                System.out.println(schemaname);
                postJsonData(schemaname, fileContent,path);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<File> getFilesFromDirectory(String directoryPath) {
        List<File> files = new ArrayList<>();
        File directory = new File(getClass().getResource(directoryPath).getFile());

        if (directory.exists() && directory.isDirectory()) {
            File[] fileList = directory.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isFile() && file.getName().endsWith(".json")) {
                        files.add(file);
                    }
                }
            }
        }
        return files;
    }

    private void postJsonData(String fileName, String jsonData, String path) {
        System.out.println(url.concat(path).concat(fileName));
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(jsonData)
                .post(url.concat(path).concat(fileName));

        assertEquals(201, response.getStatusCode(), "Failed to post JSON data from file: " + fileName);
        // You can add more assertions or logging here if needed
    }

}
