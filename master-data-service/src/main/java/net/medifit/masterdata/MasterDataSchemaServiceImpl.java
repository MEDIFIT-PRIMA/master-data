package net.medifit.masterdata;

import com.fasterxml.jackson.core.TreeNode;
import io.openepcis.s3.AmazonS3Service;
import io.openepcis.s3.UploadMetadata;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.medifit.masterdata.schema.JsonSchemaLoader;
import net.medifit.masterdata.schema.MasterDataSchema;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.everit.json.schema.Schema;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;
import org.opensearch.client.opensearch.core.SearchRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MasterDataSchemaServiceImpl implements MasterDataSchemaService {

    private final Repository repository;
    private final AmazonS3Service amazonS3Service;

    @ConfigProperty(
            name = "app.opensearch.index.master-data-schema.name",
            defaultValue = "master-data-schema")
    @Getter
    String schemaIndexName = "master-data-schema";

    @ConfigProperty(
            name = "app.opensearch.index.master-data-schema.name",
            defaultValue = "master-data-schema")
    @Getter
    String s3KeyPrefix = "master-data-schema";

    @Override
    public Uni<MasterDataSchema> getById(String id) {
        return repository
                .search(
                        schemaIndexName,
                        MasterDataSchema.class,
                        QueryBuilders.ids().values(id).build()._toQuery())
                .toUni();
    }

    @Override
    public Uni<MasterDataSchema> getByGroupType(String group, String type) {
        return repository
                .search(
                        schemaIndexName,
                        MasterDataSchema.class,
                        QueryBuilders.bool()
                                .must(
                                        QueryBuilders.term()
                                                .field("group.keyword")
                                                .value(FieldValue.of(group))
                                                .build()
                                                ._toQuery(),
                                        QueryBuilders.term()
                                                .field("type.keyword")
                                                .value(FieldValue.of(type))
                                                .build()
                                                ._toQuery())
                                .build()
                                ._toQuery())
                .toUni();
    }


    @Override
    public Uni<MasterDataSchema> storeJsonSchema(
            String group, String type, InputStream schemaStream, String defaultPrefix) {
        Uni<MasterDataSchema> existingSchema = getByGroupType(group, type)
                .chain(s -> s == null ? Uni.createFrom().nullItem() : getById(s.getUuid()));

        return existingSchema.onItem().transformToUni(result -> {

            if (result != null) {
                return storeJsonSchema(group, type, result.getUuid(), schemaStream, defaultPrefix);

            } else {
                final String id = UUID.randomUUID().toString() + ".json";
                return amazonS3Service
                        .putAsync(
                                id, schemaStream, UploadMetadata.builder().contentType("application/json").build())
                        .onItem()
                        .transform(
                                i -> {
                                    return id;
                                })
                        .chain(
                                key -> {
                                    final Schema schema = JsonSchemaLoader.loadSchema(amazonS3Service.get(key), null);
                                    return Uni.createFrom().item(key);
                                })
                        .chain(
                                key -> {
                                    final MasterDataSchema schema =
                                            MasterDataSchema.builder()
                                                    .uuid(id)
                                                    .group(group)
                                                    .type(type)
                                                    .jsonSchemaS3Key(key)
                                                    .defaultPrefix(defaultPrefix)
                                                    .createdAt(Instant.now().atOffset(ZoneOffset.UTC))
                                                    .build();
                                    return repository
                                            .index(schemaIndexName, id, schema)
                                            .onItem()
                                            .transform(
                                                    r -> {
                                                        log.info(r.result().toString());
                                                        return schema;
                                                    });
                                });
            }
        });

    }


    @Override
    public Uni<MasterDataSchema> storeJsonSchema(
            String group, String type, String uuid, InputStream schemaStream, String defaultPrefix) {
        return repository
                .search(schemaIndexName, MasterDataSchema.class, QueryBuilders.bool()
                        .must(QueryBuilders.term()
                                .field("uuid.keyword").value(FieldValue.of(uuid)).build()._toQuery())
                        .build()
                        ._toQuery()).toUni()
                .onItem()
                .ifNotNull()
                .transformToUni(
                        existingSchema -> {
                            // Upload the updated schema to S3 using the existing schema's key
                            return amazonS3Service
                                    .putAsync(
                                            existingSchema.getJsonSchemaS3Key(),
                                            schemaStream,
                                            UploadMetadata.builder().contentType("application/json").build())
                                    .onItem()
                                    .ignore().andContinueWithNull()
                                    .chain(
                                            i -> {
                                                // Update the modification timestamp and other parameters

                                                existingSchema.setUpdatedAt(Instant.now().atOffset(ZoneOffset.UTC));

                                                // Save the updated schema in the repository
                                                return repository
                                                        .index(schemaIndexName, uuid, existingSchema)
                                                        .onItem()
                                                        .transform(
                                                                r -> {
                                                                    log.info(r.result().toString());
                                                                    return existingSchema;
                                                                });
                                            });
                        });
    }

    @Override
    public Uni<MasterDataSchema> storeJsonSchema(
            String group, String type, URL url, String defaultPrefix) {
        final String id = UUID.randomUUID().toString();
        try {
            return amazonS3Service
                    .putAsync(
                            id + ".json",
                            url.openStream(),
                            UploadMetadata.builder().contentType("application/json").build())
                    .onItem()
                    .transform(
                            i -> {
                                return id;
                            })
                    .chain(
                            key -> {
                                final MasterDataSchema schema =
                                        MasterDataSchema.builder()
                                                .uuid(id)
                                                .group(group)
                                                .type(type)
                                                .jsonSchemaUrl(url.toString())
                                                .jsonSchemaS3Key(key + ".json")
                                                .defaultPrefix(defaultPrefix)
                                                .createdAt(Instant.now().atOffset(ZoneOffset.UTC))
                                                .build();
                                return repository
                                        .index(schemaIndexName, id, schema)
                                        .onItem()
                                        .transform(
                                                r -> {
                                                    log.info(r.result().toString());
                                                    return schema;
                                                });
                            });
        } catch (IOException e) {
            return Uni.createFrom().failure(e);
        }
    }

    @Override
    public Uni<Boolean> deleteJsonSchema(String id) {
        return getById(id)
                .onItem()
                .transform(
                        s -> {
                            amazonS3Service.delete(s.getJsonSchemaS3Key());
                            return s.getUuid();
                        })
                .chain(
                        uuid -> {
                            return repository.delete(schemaIndexName, uuid).onItem().transform(i -> Boolean.TRUE);
                        })
                .replaceIfNullWith(Boolean.FALSE);
    }

    @Override
    public Multi<MasterDataSchema> list() {
        final SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder();
        searchRequestBuilder.query(QueryBuilders.matchAll().build()._toQuery());
        searchRequestBuilder.size(1000);
        searchRequestBuilder.index(schemaIndexName);
        return repository.search(MasterDataSchema.class, searchRequestBuilder.build());
    }

    @Override
    public Uni<String> storeJsonData(String group, String type, InputStream schemaStream) {
        return null;
    }

    @Override
    public Uni<TreeNode> getJsonData(String group, String type, String id) {
        return null;
    }
}
