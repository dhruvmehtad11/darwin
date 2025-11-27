package com.dream11.admin.dao.featuredao;

import com.dream11.core.dto.entity.interfaces.Entity;
import com.dream11.core.dto.featuregroup.interfaces.FeatureGroup;
import com.dream11.core.dto.featurevalue.interfaces.FeatureValue;
import io.reactivex.Completable;

public interface FeatureDao {
  Completable createEntity(Entity entity);

  Completable createFeatureGroup(FeatureGroup featureGroup, String version);

  FeatureValue getFeatures();
}
