package com.dream11.admin.dao.metastore;

import static com.dream11.core.constant.query.MysqlQuery.addFeatureGroupRunQuery;
import static com.dream11.core.constant.query.MysqlQuery.getFeatureGroupRunsQuery;

import com.dream11.admin.dto.fctapplayer.response.RunDataResponse;
import com.dream11.core.dto.request.FeatureGroupRunDataRequest;
import com.dream11.core.dto.response.FeatureGroupRunDataResponse;
import com.dream11.core.util.MysqlUtils;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.sqlclient.Tuple;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MetaStoreRunsV2Dao {
  private final MysqlClient d11MysqlClient;
  private final ObjectMapper objectMapper;

  public Completable createMetaStoreRun(FeatureGroupRunDataRequest featureGroupRunDataRequest) {
    return d11MysqlClient
        .getMasterMysqlClient()
        .preparedQuery(addFeatureGroupRunQuery)
        .rxExecute(
            Tuple.tuple(
                List.of(
                    featureGroupRunDataRequest.getName(),
                    featureGroupRunDataRequest.getVersion(),
                    featureGroupRunDataRequest.getRunId(),
                    featureGroupRunDataRequest.getTimeTaken(),
                    featureGroupRunDataRequest.getCount(),
                    new JsonArray(
                        featureGroupRunDataRequest.getSampleData()), // works because sample data is
                    // List<Map<String, Object>>
                    featureGroupRunDataRequest.getErrorMessage(),
                    featureGroupRunDataRequest.getStatus())))
        .ignoreElement()
        .onErrorResumeNext(
            e -> {
              log.error(String.format("error creating meta store run: %s", e.getMessage()), e);
              return Completable.error(e);
            });
  }

  public Observable<FeatureGroupRunDataResponse> getMetaStoreRuns(String name, String version) {
    return d11MysqlClient
        .getSlaveMysqlClient()
        .preparedQuery(getFeatureGroupRunsQuery)
        .rxExecute(
            Tuple.tuple(List.of(name, version)))
        .flattenAsObservable(MysqlUtils::rowSetToJsonList)
        .map(r -> objectMapper.readValue(r.toString(), FeatureGroupRunDataResponse.class));
  }
}
