package net.medifit.masterdata.schema;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

@Slf4j
public class JsonSchemaLoader {

  private JsonSchemaLoader() {
    throw new UnsupportedOperationException("Invalid invocation of constructor");
  }

  public static Schema loadSchema(String pathToSchema) throws IOException {
    return loadSchema(new URL(pathToSchema).openConnection().getInputStream(), null);
  }

  public static Schema loadSchema(URL url) throws IOException {
    final InputStream inputStream = url.openConnection().getInputStream();
    return loadSchema(inputStream, url.toString().substring(0, url.toString().lastIndexOf("/")));
  }

  public static Schema loadSchema(InputStream inputStream, String resolutionScope) {
    try {
      final JSONObject jsonSchema = new JSONObject(new JSONTokener(inputStream));
      final SchemaLoader.SchemaLoaderBuilder builder =
          SchemaLoader.builder().schemaJson(jsonSchema).draftV7Support();
      if (resolutionScope != null) {
        builder.resolutionScope(resolutionScope);
      }
      return builder.build().load().build();
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new SchemaValidationException("Error while loading schema: " + e.getMessage(), e);
    }
  }

  public static Schema loadSchema(InputStream inputStream) {
    return loadSchema(inputStream, null);
  }
}
