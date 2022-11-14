package net.medifit.masterdata.schema;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.everit.json.schema.Schema;

@Slf4j
@ApplicationScoped
@AllArgsConstructor
public class MaserDataSchemaValidator {

  public boolean validateMasterDataSchema(InputStream schema) throws IOException {
    return true;
  }

  public boolean validateMasterDataSchema(URL url) throws IOException {
    return true;
  }

  public Optional<Schema> readMasterDataSchema(final String type) throws IOException {
    return Optional.empty();
  }

  public Optional<Schema> readMasterDataSchema(final URL url) throws IOException {
    final Schema schema = JsonSchemaLoader.loadSchema(url);
    return Optional.of(schema);
  }
}
