package com.dream11.core.util;

import com.dream11.common.util.CompletableFutureUtils;
import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CacheUtils {
    public static <T> Observable<T> getAllFromCache(AsyncCache<?, T> cache, Function<Void, Observable<T>> fallbackFunction) {
        List<Single<T>> li =
                cache.asMap().values().stream()
                        .map(CompletableFutureUtils::toSingle)
                        .collect(Collectors.toList());

        if (li.isEmpty()) return fallbackFunction.apply(null);

        return Single.zip(
                        li,
                        r -> Arrays.stream(r)
                                .map(x -> (T) x)
                                .collect(Collectors.toList()))
                .flattenAsObservable(r -> r);
    }
}
