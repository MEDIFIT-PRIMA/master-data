package net.medifit.masterdata.api;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openepcis.model.rest.ProblemResponseBody;
import io.openepcis.s3.AmazonS3Service;
import io.openepcis.s3.UploadMetadata;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.medifit.masterdata.schema.JsonSchemaLoader;
import net.medifit.masterdata.MasterDataSchemaService;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.everit.json.schema.ValidationException;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.json.JSONObject;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

@Tag(name = "MasterData", description = "Endpoints for managing MasterData.")
@Path("masterdata")
@Startup
@Slf4j
@RequiredArgsConstructor
public class MasterDataResource {
    private final ManagedExecutor managedExecutor;

    private final ObjectMapper objectMapper;

    private final AmazonS3Service amazonS3Service;

    private final MasterDataSchemaService masterDataSchemaService;

    @Operation(
            description = "Upload single json data document with id provided",
            summary = "add entry to masterdata database with id")
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "201",
                            description = "Successful capture of JSON object provided by the " + "client. "),
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
    @Path("{group}/{type}/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Uni<RestResponse> postDataWithId(
            @Parameter(description = "schema group", required = true, in = ParameterIn.PATH)
            @NotNull
            @RestPath
            String group,
            @Parameter(description = "type name", required = true, in = ParameterIn.PATH)
            @NotNull
            @RestPath
            String type,
            @Parameter(description = "type object id", required = true, in = ParameterIn.PATH)
            @NotNull
            @RestPath
            String id,
            @NotNull InputStream inputStream) {
        final String key = id.concat(".json");
        return amazonS3Service.putAsync(key, inputStream, UploadMetadata.builder().contentType("application/ld+json").build()).chain( u -> {
            return masterDataSchemaService.getByGroupType(group, type).onItem().transform(s -> {
                final org.everit.json.schema.Schema schema = JsonSchemaLoader.loadSchema(amazonS3Service.get(s.getJsonSchemaS3Key()));
                schema.validate(new JSONObject(amazonS3Service.get(u.getKey())));
                return u.getKey();
            });
        }).onItem().transform(item -> {
            return RestResponse.created(URI.create(String.format("masterdata/%s/%s/%s", group, type, key)));
        });

    }


    @Operation(
            description = "Upload single json data document with new id being generated by backend",
            summary = "add entry to masterdata database and create new id for entry")
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "201",
                            description = "Successful capture of JSON object provided by the " + "client. "),
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
    @Consumes({MediaType.APPLICATION_JSON, "application/ld+json"})
    @Produces({MediaType.APPLICATION_JSON})
    public Uni<RestResponse> postData(
            @Parameter(description = "schema group", required = true, in = ParameterIn.PATH)
            @NotNull
            @RestPath
            String group,
            @Parameter(description = "type name", required = true, in = ParameterIn.PATH)
            @NotNull
            @RestPath
            String type,
            @NotNull InputStream inputStream
            ) {
        final String uuid = UUID.randomUUID().toString();
        final String key = uuid.concat(".json");
        return amazonS3Service.putAsync(key, inputStream, UploadMetadata.builder().contentType("application/ld+json").build()).chain( u -> {
            return masterDataSchemaService.getByGroupType(group, type).onItem().transform(s -> {
                final org.everit.json.schema.Schema schema = JsonSchemaLoader.loadSchema(amazonS3Service.get(s.getJsonSchemaS3Key()));
                try {
                    final String data = IoUtils.toUtf8String(amazonS3Service.get(u.getKey()));
                    final JSONObject json = new JSONObject(data);
                    schema.validate(json);
                } catch (ValidationException ve) {
                    throw new javax.validation.ValidationException(String.join("\n", ve.getAllMessages()), ve);
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe.getMessage(), ioe);
                }
                return u.getKey();
            });
        }).onItem().transform(item -> {
            return RestResponse.created(URI.create(String.format("masterdata/%s/%s/%s", group, type, uuid)));
        });
    }

    @Operation(
            description = "Get single json data document by id provided",
            summary = "get entry from masterdata database by id")
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description = "Successful return of JSON object provided by the client."),
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
    @GET
    @Path("{group}/{type}/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Uni<InputStream> getDataById(
            @Parameter(description = "schema group", required = true, in = ParameterIn.PATH)
            @NotNull
            @RestPath
            String group,
            @Parameter(description = "type name", required = true, in = ParameterIn.PATH)
            @NotNull
            @RestPath
            String type,
            @Parameter(description = "type object id", required = true, in = ParameterIn.PATH)
            @NotNull
            @RestPath
            String id) {
        final String key = id.concat(".json");
        return Uni.createFrom().item(amazonS3Service.get(key));
    }
}
