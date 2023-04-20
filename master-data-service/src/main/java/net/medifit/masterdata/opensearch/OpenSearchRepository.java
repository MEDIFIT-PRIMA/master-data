package net.medifit.masterdata.opensearch;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import net.medifit.masterdata.Repository;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.*;

@ApplicationScoped
@RequiredArgsConstructor
public class OpenSearchRepository implements Repository {

  private final OpenSearchClient client;

  @Override
  public <T> Multi<T> search(String index, Class<T> cls, Query query) {
    try {
      SearchResponse<T> searchResponse = client.search(s -> s.index(index).query(query), cls);
      return Multi.createFrom().items(searchResponse.hits().hits().stream()).map(h -> h.source());
    } catch (Throwable e) {
      return Multi.createFrom().failure(e);
    }
  }

  @Override
  public <T> Multi<T> search(Class<T> cls, SearchRequest searchRequest) {
    try {
      SearchResponse<T> searchResponse = client.search(searchRequest, cls);
      return Multi.createFrom().items(searchResponse.hits().hits().stream()).map(h -> h.source());
    } catch (Throwable e) {
      return Multi.createFrom().failure(e);
    }
  }

  @Override
  public <T> Uni<IndexResponse> index(String index, String id, T value) {
    try {
      final IndexRequest<T> indexRequest =
          new IndexRequest.Builder<T>().index(index).id(id).document(value).build();
      return Uni.createFrom().item(client.index(indexRequest));
    } catch (Throwable e) {
      return Uni.createFrom().failure(e);
    }
  }

  @Override
  public Uni<DeleteResponse> delete(String index, String id) {
    try {
      return Uni.createFrom()
          .item(client.delete(new DeleteRequest.Builder().index(index).id(id).build()));
    } catch (Throwable e) {
      return Uni.createFrom().failure(e);
    }
  }
}
