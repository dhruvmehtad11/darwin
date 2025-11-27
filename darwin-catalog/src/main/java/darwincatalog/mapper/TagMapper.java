package darwincatalog.mapper;

import darwincatalog.entity.AssetTagRelationEntity;
import org.springframework.stereotype.Component;

@Component
public class TagMapper implements EntityResponseMapper<AssetTagRelationEntity, String> {
  @Override
  public AssetTagRelationEntity toEntity(String s) {
    return AssetTagRelationEntity.builder().tag(s).build();
  }

  @Override
  public String toDto(AssetTagRelationEntity assetTagRelationEntity) {
    return assetTagRelationEntity.getTag();
  }
}
