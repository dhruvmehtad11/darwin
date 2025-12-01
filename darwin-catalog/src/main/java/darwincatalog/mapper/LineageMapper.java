package darwincatalog.mapper;

import darwincatalog.entity.AssetLineageEntity;
import darwincatalog.entity.FieldLineageEntity;
import darwincatalog.repository.AssetRepository;
import darwincatalog.repository.FieldLineageRepository;
import darwincatalog.util.Common;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.Asset;
import org.openapitools.model.AssetRelation;
import org.openapitools.model.Lineage;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LineageMapper {
  private final AssetRepository assetRepository;
  private final AssetMapper assetMapper;
  private final FieldLineageRepository fieldLineageRepository;

  // todo move to external cache if this eats up too much memory
  private final Map<Long, Asset> assetCacheMap = new HashMap<>();
  private final Map<Long, List<FieldLineageEntity>> fieldLineageCacheMap = new HashMap<>();

  public LineageMapper(
      AssetRepository assetRepository,
      AssetMapper assetMapper,
      FieldLineageRepository fieldLineageRepository) {
    this.assetRepository = assetRepository;
    this.assetMapper = assetMapper;
    this.fieldLineageRepository = fieldLineageRepository;
  }

  public Lineage toLineageDto(List<AssetLineageEntity> assetLineageEntityList) {
    Lineage lineage = new Lineage();
    Map<String, Asset> assetsInfo = new HashMap<>();

    assetLineageEntityList.forEach(
        assetLineageEntity -> {
          long fromAssetId = assetLineageEntity.getFromAssetId();
          long toAssetId = assetLineageEntity.getToAssetId();

          Asset fromAsset = getLineageAssetDtoCached(fromAssetId);
          Asset toAsset = getLineageAssetDtoCached(toAssetId);

          Map<String, List<String>> fieldsMaps = new HashMap<>();
          getFieldLineageCache(assetLineageEntity.getId())
              .forEach(
                  (fieldLineageEntity) -> Common.constructFieldMap(fieldLineageEntity, fieldsMaps));

          AssetRelation assetRelation =
              new AssetRelation()
                  .to(toAsset.getFqdn())
                  .from(fromAsset.getFqdn())
                  .fieldsMap(fieldsMaps);
          lineage.addGraphItem(assetRelation);
          assetsInfo.put(fromAsset.getFqdn(), fromAsset);
          assetsInfo.put(toAsset.getFqdn(), toAsset);
        });

    lineage.setAssetsInfo(assetsInfo);
    return lineage;
  }

  private Asset getLineageAssetDtoCached(long assetId) {
    Asset result = assetCacheMap.get(assetId);
    if (result == null) {
      result = assetMapper.toLineageAssetDto(assetRepository.findById(assetId).get());
      assetCacheMap.put(assetId, result);
    }
    return result;
  }

  private List<FieldLineageEntity> getFieldLineageCache(long assetLineageId) {
    List<FieldLineageEntity> result = fieldLineageCacheMap.get(assetLineageId);
    if (result == null) {
      result = fieldLineageRepository.findAllByAssetLineageId(assetLineageId);
      fieldLineageCacheMap.put(assetLineageId, result);
    }
    return result;
  }
}
