package com.dream11.admin.dao.metastore;

import static com.dream11.core.constant.query.MysqlQuery.*;
import static com.dream11.core.error.ServiceError.*;
import static com.dream11.core.util.JsonConversionUtils.*;

import com.dream11.core.dto.metastore.VersionMetadata;
import com.dream11.core.util.MysqlUtils;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.sqlclient.Transaction;
import io.vertx.reactivex.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MetaStoreVersionDao {
    private final MysqlClient d11MysqlClient;
    private final ObjectMapper objectMapper;

    public Single<Transaction> getMysqlTransaction() {
        return d11MysqlClient.getMasterMysqlClient().rxBegin();
    }

    public Completable updateFeatureGroupVersion(String name, String version) {
        return getMysqlTransaction()
                .flatMapCompletable(
                        tx ->
                                tx.preparedQuery(updateFeatureGroupLatestVersion)
                                        .rxExecute(Tuple.of(name, version, version))
                                        .ignoreElement()
                                        .andThen(tx.rxCommit()))
                .onErrorResumeNext(
                        e -> {
                            e.printStackTrace();
                            return Completable.error(e);
                        });
    }

    public Observable<VersionMetadata> getAllFeatureGroupVersion() {
        return d11MysqlClient
                .getSlaveMysqlClient()
                .query(getAllFeatureGroupLatestVersion)
                .rxExecute()
                .map(MysqlUtils::rowSetToJsonList)
                .flattenAsObservable(r -> r)
                .flatMap(
                        r ->
                                convertJsonObservable(
                                        r,
                                        VersionMetadata.class,
                                        objectMapper,
                                        ENTITY_REGISTRATION_INVALID_REQUEST_EXCEPTION))
                .onErrorResumeNext(
                        e -> {
                            e.printStackTrace();
                            return Observable.error(e);
                        });
    }

    public Maybe<VersionMetadata> getLatestFeatureGroupVersion(String name) {
        return d11MysqlClient
                .getSlaveMysqlClient()
                .preparedQuery(getFeatureGroupLatestVersion)
                .rxExecute(Tuple.of(name))
                .map(MysqlUtils::rowSetToJsonList)
                .filter(r -> !r.isEmpty())
                .map(r -> r.get(0))
                .flatMap(
                        r ->
                                convertJsonMaybe(
                                        r,
                                        VersionMetadata.class,
                                        objectMapper,
                                        ENTITY_REGISTRATION_INVALID_REQUEST_EXCEPTION))
                .onErrorResumeNext(
                        e -> {
                            e.printStackTrace();
                            return Maybe.error(e);
                        });
    }
}