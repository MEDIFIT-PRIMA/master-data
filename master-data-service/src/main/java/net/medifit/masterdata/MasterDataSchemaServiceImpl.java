package net.medifit.masterdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openepcis.model.core.exception.PersistenceException;
import io.openepcis.model.core.exception.ResourceNotFoundException;
import io.openepcis.s3.AmazonS3Service;
import io.openepcis.s3.UploadMetadata;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.medifit.masterdata.elasticsearch.ElasticsearchReactiveRepository;
import net.medifit.masterdata.schema.JsonSchemaLoader;
import net.medifit.masterdata.schema.MasterDataSchema;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.everit.json.schema.Schema;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MasterDataSchemaServiceImpl implements MasterDataSchemaService {

  private final ElasticsearchReactiveRepository elasticsearchReactiveRepository;

  private final ObjectMapper objectMapper;

  private final AmazonS3Service amazonS3Service;

  private final ManagedExecutor managedExecutor;

  @ConfigProperty(
      name = "app.elasticsearch.index.master-data-schema.name",
      defaultValue = "master-data-schema")
    @Getter
    String schemaIndexName = "master-data-schema";

    @ConfigProperty(
            name = "app.elasticsearch.index.master-data-schema.name",
            defaultValue = "master-data-schema")
    @Getter
    String s3KeyPrefix = "master-data-schema";

  @Override
  public Uni<MasterDataSchema> getById(String id) {
    return elasticsearchReactiveRepository
        .query(QueryBuilders.idsQuery().addIds(id), schemaIndexName)
        .onItem()
        .transform(
            i -> {
              final Optional<SearchHit> hit = Arrays.stream(i).findFirst();
              if (hit.isPresent()) {
                try {
                  return objectMapper.readValue(
                      hit.get().getSourceAsString(), MasterDataSchema.class);
                } catch (JsonProcessingException e) {
                  throw new RuntimeException(e);
                }
              }
              throw new ResourceNotFoundException("MasterDataSchema for id {}");
            });
  }

  @Override
  public Uni<MasterDataSchema> getByGroupType(String group, String type) {
    return elasticsearchReactiveRepository
        .query(
            QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("group.keyword", group))
                .must(QueryBuilders.termQuery("type.keyword", type)),
            schemaIndexName)
        .onItem()
        .transform(
            i -> {
              final Optional<SearchHit> hit = Arrays.stream(i).findFirst();
              if (hit.isPresent()) {
                try {
                  return objectMapper.readValue(
                      hit.get().getSourceAsString(), MasterDataSchema.class);
                } catch (JsonProcessingException e) {
                  throw new RuntimeException(e);
                }
              }
              throw new ResourceNotFoundException("MasterDataSchema for id {}");
            });
  }

  @Override
  public Uni<MasterDataSchema> storeJsonSchema(
      String group, String type, InputStream schemaStream, String defaultPrefix) {
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
              final Map<String, ?> map = objectMapper.convertValue(schema, Map.class);
              return elasticsearchReactiveRepository
                  .index(map, schemaIndexName, id)
                  .onItem()
                  .transform(
                      r -> {
                        log.info(r.status().toString());
                        return schema;
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
                final Map<String, ?> map = objectMapper.convertValue(schema, Map.class);
                return elasticsearchReactiveRepository
                    .index(map, schemaIndexName, id)
                    .onItem()
                    .transform(
                        r -> {
                          log.info(r.status().toString());
                          return schema;
                        });
              });
    } catch (IOException e) {
      throw new PersistenceException(e.getMessage(), e);
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
              return elasticsearchReactiveRepository
                  .delete(schemaIndexName, uuid)
                  .onItem()
                  .transform(i -> Boolean.TRUE);
            })
        .replaceIfNullWith(Boolean.FALSE);
  }

  @Override
  public Multi<MasterDataSchema> list() {
    final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    return Multi.createFrom()
        .emitter(
            em -> {
              searchSourceBuilder.query(QueryBuilders.matchAllQuery());
              searchSourceBuilder.size(1000);
              elasticsearchReactiveRepository
                  .query(schemaIndexName, searchSourceBuilder)
                  .runSubscriptionOn(managedExecutor)
                  .subscribe()
                  .with(
                      hits -> {
                        try {
                          Stream.of(hits)
                              .forEach(
                                  hit -> {
                                    try {
                                      em.emit(
                                          objectMapper.readValue(
                                              hit.getSourceAsString(), MasterDataSchema.class));
                                    } catch (IOException e) {
                                      throw new PersistenceException(e.getMessage(), e);
                                    }
                                  });
                        } finally {
                          em.complete();
                        }
                      },
                      throwable -> {
                        em.fail(throwable);
                      });
            });
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
