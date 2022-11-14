package net.medifit.masterdata.elasticsearch;

import static net.medifit.masterdata.elasticsearch.ExceptionMessages.ERROR_WHILE_PROCESSING_QUERY;
import static org.elasticsearch.client.RequestOptions.DEFAULT;

import io.openepcis.model.core.exception.PersistenceException;
import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class ElasticsearchRepository {
  private final RestHighLevelClient client;

  protected SearchHit[] query(QueryBuilder query, String index) {
    final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(query);
    final SearchRequest searchRequest = new SearchRequest(index);

    searchRequest.source(searchSourceBuilder);
    try {
      final SearchResponse response = client.search(searchRequest, DEFAULT);
      return response.getHits().getHits();
    } catch (IOException e) {
      log.error(ERROR_WHILE_PROCESSING_QUERY, e);
      throw new PersistenceException(e.getMessage());
    }
  }

  protected SearchHit[] query(String index, SearchSourceBuilder searchSourceBuilder) {
    final SearchRequest searchRequest = new SearchRequest(index);
    searchRequest.source(searchSourceBuilder);
    try {
      final SearchResponse response = client.search(searchRequest, DEFAULT);
      return response.getHits().getHits();
    } catch (IOException e) {
      log.error(ERROR_WHILE_PROCESSING_QUERY, e);
      throw new PersistenceException(e.getMessage());
    }
  }

  protected SearchHit[] queryWithScroll(QueryBuilder query, String index) {
    final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(query);
    final SearchRequest searchRequest = new SearchRequest(index);

    searchRequest.source(searchSourceBuilder);
    searchSourceBuilder.size(1000);
    searchRequest.scroll(TimeValue.timeValueMinutes(1L));
    try {
      final SearchResponse response = client.search(searchRequest, DEFAULT);
      return response.getHits().getHits();
    } catch (IOException e) {
      log.error(ERROR_WHILE_PROCESSING_QUERY, e);
      throw new PersistenceException(e.getMessage());
    }
  }
}
