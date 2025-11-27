package com.dream11.admin.service;

import static com.dream11.core.constant.Constants.*;

import com.dream11.admin.dao.PopulatorManagerDao;
import com.dream11.core.dto.populator.PopulatorGroupMetadata;
import com.google.inject.Inject;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class PopulatorManagerService {
  private final PopulatorManagerDao populatorManagerDao;

  public Completable putPopulatorGroupMetadata(PopulatorGroupMetadata populatorGroupMetadata) {
    return populatorManagerDao.addPopulatorMetadata(populatorGroupMetadata);
  }

  public Single<List<PopulatorGroupMetadata>> getAllPopulatorMetadata() {
    return populatorManagerDao
        .getAllPopulatorMetadata()
        .map(
            r ->
                PopulatorGroupMetadata.builder()
                    .tenantName(r.getTenantName())
                    .numWorkers(r.getNumWorkers())
                    .build())
        .toList();
  }
}
