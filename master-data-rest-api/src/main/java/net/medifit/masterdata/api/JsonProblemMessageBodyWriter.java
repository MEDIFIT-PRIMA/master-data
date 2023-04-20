package net.medifit.masterdata.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.resteasy.reactive.jackson.runtime.serialisers.BasicServerJacksonMessageBodyWriter;
import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces("application/problem+json")
public class JsonProblemMessageBodyWriter extends BasicServerJacksonMessageBodyWriter {

  public JsonProblemMessageBodyWriter() {
    super(
        new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .findAndRegisterModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
  }

  @Inject
  public JsonProblemMessageBodyWriter(ObjectMapper mapper) {
    super(mapper);
  }
}
