package com.dream11.app.jobs;

import com.dream11.app.service.CassandraMetaStoreService;
import com.google.inject.Inject;
import io.reactivex.Completable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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