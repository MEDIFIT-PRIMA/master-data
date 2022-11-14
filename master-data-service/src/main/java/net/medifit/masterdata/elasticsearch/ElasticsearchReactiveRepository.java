package net.medifit.masterdata.elasticsearch;

import static io.openepcis.model.core.exception.ExceptionMessages.ERROR_WHILE_PROCESSING_QUERY;
import static org.elasticsearch.client.RequestOptions.DEFAULT;

import io.openepcis.model.core.exception.PersistenceException;
import io.reactiverse.elasticsearch.client.mutiny.RestHighLevelClient;
import io.smallrye.mutiny.Uni;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class ElasticsearchReactiveRepository {
  private final RestHighLevelClient client;

  @SneakyThrows
  public Uni<SearchHit[]> query(QueryBuilder query, String index) {
    final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(query);
    final SearchRequest searchRequest = new SearchRequest(index);
    searchRequest.source(searchSourceBuilder);
    return client
        .searchAsync(searchRequest, DEFAULT)
        .map(searchResponse -> searchResponse.getHits().getHits())
        .onFailure()
        .invoke(
            e -> {
              log.error(ERROR_WHILE_PROCESSING_QUERY, e);
              throw new PersistenceException(e.getMessage());
            });
  }

  @SneakyThrows
  public Uni<SearchHit[]> query(String index, SearchSourceBuilder searchSourceBuilder) {
    final SearchRequest searchRequest = new SearchRequest(index);
    searchRequest.source(searchSourceBuilder);
    return client
        .searchAsync(searchRequest, DEFAULT)
        .map(searchResponse -> searchResponse.getHits().getHits())
        .onFailure()
        .invoke(
            e -> {
              log.error(ERROR_WHILE_PROCESSING_QUERY, e);
              throw new PersistenceException(e.getMessage());
            });
  }

  public Uni<IndexResponse> index(Map<String, ?> source, String index) {
    final IndexRequest indexRequest = new IndexRequest();
    return client.indexAsync(indexRequest.source(source).index(index), DEFAULT);
  }

  public Uni<IndexResponse> index(Map<String, ?> source, String index, String id) {
    final IndexRequest indexRequest = new IndexRequest();
    return client.indexAsync(indexRequest.source(source).index(index).id(id), DEFAULT);
  }

  public Uni<DeleteResponse> delete(String index, String id) {
    final DeleteRequest deleteRequest = new DeleteRequest();
    deleteRequest.index(index).id(id);
    return client.deleteAsync(deleteRequest, DEFAULT);
  }
}
