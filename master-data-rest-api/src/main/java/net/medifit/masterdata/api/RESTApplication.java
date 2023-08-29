package net.medifit.masterdata.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@OpenAPIDefinition(info = @Info(title = "MasterData API", version = "1.0.0"),
        components =
@Components(
        securitySchemes = {
                @SecurityScheme(
                        securitySchemeName = "bearerAuth",
                        type = SecuritySchemeType.HTTP,
                        scheme = "bearer",
                        bearerFormat = "JWT"),
                @SecurityScheme(
                        securitySchemeName = "apiKey",
                        type = SecuritySchemeType.APIKEY,
                        in = SecuritySchemeIn.HEADER,
                        apiKeyName = "API-KEY"),
                @SecurityScheme(
                        securitySchemeName = "apiKeySecret",
                        type = SecuritySchemeType.APIKEY,
                        in = SecuritySchemeIn.HEADER,
                        apiKeyName = "API-KEY-SECRET")
        }))
@ApplicationPath("/")
public class RESTApplication extends Application {

  @Produces
  @Singleton
  public ObjectMapper createObjectMapper() {
    return new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .findAndRegisterModules()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
}
