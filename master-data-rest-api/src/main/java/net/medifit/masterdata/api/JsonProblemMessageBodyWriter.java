package net.medifit.masterdata.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.resteasy.reactive.jackson.runtime.serialisers.BasicServerJacksonMessageBodyWriter;
import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces("application/problem+json")
public class JsonProblemMessageBodyWriter extends BasicServerJacksonMessageBodyWriter {

  @Inject
  public JsonProblemMessageBodyWriter(ObjectMapper mapper) {
    super(mapper);
  }
}
