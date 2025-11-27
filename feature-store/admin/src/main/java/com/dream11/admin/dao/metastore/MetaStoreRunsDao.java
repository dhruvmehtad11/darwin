package com.dream11.admin.dao.metastore;

import com.dream11.core.util.MysqlUtils;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.google.inject.Inject;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.dream11.core.constant.query.MysqlQuery.getFeatureGroupRunsQuery;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MetaStoreRunsDao {
    private final MysqlClient d11MysqlClient;

    public Single<List<JsonObject>> getRunsForFg(String name, String version) {
        return d11MysqlClient
                .getSlaveMysqlClient()
                .preparedQuery(getFeatureGroupRunsQuery)
                .rxExecute(Tuple.of(name, version))
                .map(MysqlUtils::rowSetToJsonList);
    }
}
