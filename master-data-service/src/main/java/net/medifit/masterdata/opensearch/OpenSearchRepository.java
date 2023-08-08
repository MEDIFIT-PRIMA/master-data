package net.medifit.masterdata.opensearch;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import net.medifit.masterdata.Repository;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.opensearch.client.opensearch.OpenSearchAsyncClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.*;
import org.opensearch.client.opensearch.indices.FlushRequest;
import org.opensearch.client.opensearch.indices.RefreshRequest;

@ApplicationScoped
@RequiredArgsConstructor
public class OpenSearchRepository implements Repository {

  private final OpenSearchAsyncClient asyncClient;

  private final OpenSearchClient client;
  private final ManagedExecutor executor;

  @Override
  public <T> Multi<T> search(String index, Class<T> cls, Query query) {
    try {
      CompletableFuture<SearchResponse<T>> future =
          asyncClient.search(s -> s.index(index).query(query), cls);
      return Multi.createFrom()
          .emitter(
              em -> {
                Uni.createFrom()
                    .completionStage(future)
                    .runSubscriptionOn(executor)
                    .subscribe()
                    .with(
                        r -> {
                          r.hits().hits().stream().forEach(item -> em.emit(item.source()));
                          em.complete();
                        },
                        t -> {
                          em.fail(t);
                        });
              });
      // SearchResponse<T> searchResponse = client.search(s -> s.index(index).query(query), cls);
      // return Multi.createFrom().items(searchResponse.hits().hits().stream()).map(h ->
      // h.source());
    } catch (Throwable e) {
      return Multi.createFrom().failure(e);
    }
  }

  @Override
  public <T> Multi<T> search(Class<T> cls, SearchRequest searchRequest) {
    try {
      CompletableFuture<SearchResponse<T>> future = asyncClient.search(searchRequest, cls);
      return Multi.createFrom()
          .emitter(
              em -> {
                Uni.createFrom()
                    .completionStage(future)
                    .runSubscriptionOn(executor)
                    .subscribe()
                    .with(
                        r -> {
                          r.hits().hits().stream().forEach(item -> em.emit(item.source()));
                          em.complete();
                        },
                        t -> {
                          em.fail(t);
                        });
              });
      // SearchResponse<T> searchResponse = client.search(searchRequest, cls);
      // return Multi.createFrom().items(searchResponse.hits().hits().stream()).map(h ->
      // h.source());
    } catch (Throwable e) {
      return Multi.createFrom().failure(e);
    }
  }

  @Override
  public <T> Uni<IndexResponse> index(String index, String id, T value) {
    try {
      final IndexRequest<T> indexRequest =
          new IndexRequest.Builder<T>().index(index).id(id).document(value).build();
      return Uni.createFrom().completionStage(asyncClient.index(indexRequest));
    } catch (Throwable e) {
      return Uni.createFrom().failure(e);
    }
  }

  @Override
  public Uni<DeleteResponse> delete(String index, String id) {
    try {
      return Uni.createFrom()
          .completionStage(
              asyncClient.delete(new DeleteRequest.Builder().index(index).id(id).build()));
    } catch (Throwable e) {
      return Uni.createFrom().failure(e);
    }
  }

  @Override
  public void refresh(String index) throws IOException {
    client.indices().refresh(new RefreshRequest.Builder().index(index).build());
  }

    @Override
    public void refresh() throws IOException {
        client.indices().refresh();
    }
}
