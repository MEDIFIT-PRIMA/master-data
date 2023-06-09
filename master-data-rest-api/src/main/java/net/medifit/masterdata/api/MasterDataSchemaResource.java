package net.medifit.masterdata.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openepcis.core.exception.ImplementationException;
import io.openepcis.core.exception.ResourceNotFoundException;
import io.openepcis.model.rest.ProblemResponseBody;
import io.openepcis.s3.AmazonS3Service;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.medifit.masterdata.MasterDataSchemaService;
import net.medifit.masterdata.schema.MasterDataSchema;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

@Tag(name = "MasterDataSchema", description = "Endpoints for managing MasterData JSON schemas.")
@Path("masterdata/schemas")
@Slf4j
@RequiredArgsConstructor
@Startup
public class MasterDataSchemaResource {
  private final ManagedExecutor managedExecutor;

  private final ObjectMapper objectMapper;

  private final AmazonS3Service amazonS3Service;

  private final MasterDataSchemaService masterDataSchemaService;

  @Operation(
      description = "Upload single schema file",
      summary = "Upload schema file for a namespace.")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "201",
            description = "Successful capture of JSON schema provided by the " + "client. "),
        @APIResponse(
            responseCode = "401",
            description = "Authorization information is missing or invalid.",
            content =
                @Content(
                    schema = @Schema(implementation = ProblemResponseBody.class),
                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
        @APIResponse(
            responseCode = "403",
            description = "Client is unauthorized to access this resource.",
            content =
                @Content(
                    schema = @Schema(implementation = ProblemResponseBody.class),
                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
        @APIResponse(
            responseCode = "406",
            description = "The server cannot process data as requested.",
            content =
                @Content(
                    schema = @Schema(implementation = ProblemResponseBody.class),
                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
        @APIResponse(
            responseCode = "500",
            description = "An error occurred on the backend.",
            content =
                @Content(
                    schema = @Schema(implementation = ProblemResponseBody.class),
                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
      })
  @POST
  @Path("{group}/{type}")
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  public Uni<RestResponse> userExtensionJSonSchemaPost(
      @Parameter(description = "schema group", required = true, in = ParameterIn.PATH)
          @NotNull
          @RestPath
          String group,
      @Parameter(description = "type name", required = true, in = ParameterIn.PATH)
          @NotNull
          @RestPath
          String type,
      @Parameter(description = "default namespace prefix", required = true, in = ParameterIn.QUERY)
          @NotNull
          @RestQuery
          String defaultPrefix,
      @NotNull InputStream inputStream) {
    return masterDataSchemaService
        .storeJsonSchema(group, type, inputStream, defaultPrefix)
        .onItem()
        .transform(
            s -> {
              return RestResponse.ResponseBuilder.created(
                      URI.create(String.format("/masterdata/schema/%s/%s.json", group, type)))
                  .entity(s)
                  .build();
            });
  }

  @Operation(
      description = "Upload single schema file",
      summary = "Upload schema file for a namespace.")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Successful capture of JSON schema provided by the " + "client. "),
        @APIResponse(
            responseCode = "401",
            description = "Authorization information is missing or invalid.",
            content =
                @Content(
                    schema = @Schema(implementation = ProblemResponseBody.class),
                    example = ResponseBodyExamples.RESPONSE_401_UNAUTHORIZED_REQUEST)),
        @APIResponse(
            responseCode = "403",
            description = "Client is unauthorized to access this resource.",
            content =
                @Content(
                    schema = @Schema(implementation = ProblemResponseBody.class),
                    example = ResponseBodyExamples.RESPONSE_403_CLIENT_UNAUTHORIZED)),
        @APIResponse(
            responseCode = "406",
            description = "The server cannot process data as requested.",
            content =
                @Content(
                    schema = @Schema(implementation = ProblemResponseBody.class),
                    example = ResponseBodyExamples.RESPONSE_406_NOT_ACCEPTABLE)),
        @APIResponse(
            responseCode = "500",
            description = "An error occurred on the backend.",
            content =
                @Content(
                    schema = @Schema(implementation = ProblemResponseBody.class),
                    example = ResponseBodyExamples.RESPONSE_500_IMPLEMENTATION_EXCEPTION))
      })
  @POST
  @Path("{group}/{type}/url")
  @Produces({MediaType.APPLICATION_JSON})
  public Uni<RestResponse> userExtensionJSonSchemaPost(
      @Parameter(description = "schema group", required = true, in = ParameterIn.PATH)
          @NotNull
          @RestPath
          String group,
      @Parameter(description = "type name", required = true, in = ParameterIn.PATH)
          @NotNull
          @RestPath
          String type,
      @Parameter(description = "default namespace prefix", required = true, in = ParameterIn.QUERY)
          @NotNull
          @RestQuery
          String defaultPrefix,
      @Parameter(description = "valid url for json schema", required = true, in = ParameterIn.QUERY)
          @NotNull
          @RestQuery
          String url)
      throws MalformedURLException {
    return masterDataSchemaService
        .storeJsonSchema(group, type, new URL(url), defaultPrefix)
        .onItem()
        .transform(
            s -> {
              return RestResponse.ResponseBuilder.created(
                      URI.create(String.format("/masterdata/schema/%s/%s.json", group, type)))
                  .entity(s)
                  .build();
            });
  }

  @DELETE
  @Path("{group}/{type}")
  @Produces({MediaType.APPLICATION_JSON})
  public Uni<RestResponse> userExtensionJSonSchemaDelete(
      @Parameter(description = "schema group", required = true, in = ParameterIn.PATH)
          @NotNull
          @RestPath
          String group,
      @Parameter(description = "schema type", required = true, in = ParameterIn.PATH)
          @NotNull
          @RestPath
          String type) {
    return masterDataSchemaService
        .getByGroupType(group, type)
        .chain(s -> masterDataSchemaService.deleteJsonSchema(s.getUuid()))
        .onItem()
        .transform(
            i -> {
              if (i) {
                return RestResponse.ok();
              }
              throw new ResourceNotFoundException("user extension not found");
            });
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Multi<MasterDataSchema> list() {
    return masterDataSchemaService.list();
  }

  @GET
  @Path("{group}/{type}.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<RestResponse> schema(
      @Parameter(description = "schema group", required = true, in = ParameterIn.PATH)
          @NotNull
          @RestPath
          String group,
      @Parameter(description = "schema type", required = true, in = ParameterIn.PATH)
          @NotNull
          @RestPath
          String type) {
    return masterDataSchemaService
        .getByGroupType(group, type)
        .onItem()
        .transform(
            s -> {
              try {
                return RestResponse.ok(
                    objectMapper.readTree(amazonS3Service.get(s.getJsonSchemaS3Key())));
              } catch (IOException e) {
                throw new ImplementationException(e.getMessage(), e);
              }
            });
  }
}
