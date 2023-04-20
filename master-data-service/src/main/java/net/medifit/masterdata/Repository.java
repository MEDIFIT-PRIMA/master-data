package net.medifit.masterdata;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.DeleteResponse;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchRequest;

public interface Repository {
  <T> Multi<T> search(String index, Class<T> cls, Query query);

  <T> Multi<T> search(Class<T> cls, SearchRequest searchRequest);

  <T> Uni<IndexResponse> index(String index, String id, T value);

  Uni<DeleteResponse> delete(String index, String id);
}
