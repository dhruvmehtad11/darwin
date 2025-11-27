package com.dream11.admin.jobs;

import com.dream11.admin.service.CassandraMetaStoreService;
import com.google.inject.Inject;
import io.reactivex.Completable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class StartupJob {
    private final CassandraMetaStoreService cassandraMetaStoreService;

    public Completable handle() {
        List<Completable> startupList =
                List.of(
                        cassandraMetaStoreService.loadCassandraEntityCache(),
                        cassandraMetaStoreService.loadCassandraFeatureGroupCache(),
                        cassandraMetaStoreService.loadCassandraFeatureGroupVersionCache());
        return Completable.merge(startupList)
                .onErrorResumeNext(
                        e -> {
                            e.printStackTrace();
                            return Completable.complete();
                        });
    }
}