package com.dream11.app.dao.featuredao;

import com.dream11.core.dto.entity.interfaces.Entity;
import com.dream11.core.dto.featuregroup.interfaces.FeatureGroup;
import com.dream11.core.dto.featurevalue.interfaces.FeatureValue;
import io.reactivex.Completable;

public interface FeatureDao {
  FeatureValue getFeatures();
}
