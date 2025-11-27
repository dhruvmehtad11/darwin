package darwincatalog.service;

import static darwincatalog.util.Common.mapToEntity;

import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.AssetTagRelationEntity;
import darwincatalog.exception.TagNotFoundException;
import darwincatalog.mapper.TagMapper;
import darwincatalog.repository.AssetTagRelationRepository;
import java.util.List;
import org.openapitools.model.PostTagsRequest;
import org.springframework.stereotype.Service;

@Service
public class TagService {

  private final ValidatorService validatorService;
  private final TagMapper tagMapper;
  private final AssetTagRelationRepository assetTagRelationRepository;

  public TagService(
      ValidatorService validatorService,
      TagMapper tagMapper,
      AssetTagRelationRepository assetTagRelationRepository) {
    this.validatorService = validatorService;
    this.tagMapper = tagMapper;
    this.assetTagRelationRepository = assetTagRelationRepository;
  }

  public void postTags(PostTagsRequest postTagsRequest) {
    String assetName = postTagsRequest.getAssetFqdn();
    AssetEntity assetEntity = validatorService.verifyAssetExists(assetName);
    List<String> tags = postTagsRequest.getTags();
    persistTags(assetEntity.getId(), tags);
  }

  public void persistTags(Long assetId, List<String> tags) {
    List<AssetTagRelationEntity> assetTagRelationEntities = mapToEntity(tags, tagMapper);
    assetTagRelationEntities.forEach(e -> e.setAssetId(assetId));
    assetTagRelationRepository.saveAll(assetTagRelationEntities);
  }

  public void deleteTag(String assetName, String tagName) {
    AssetEntity assetEntity = validatorService.verifyAssetExists(assetName);
    AssetTagRelationEntity entity =
        assetTagRelationRepository.findAllByAssetId(assetEntity.getId()).stream()
            .filter(e -> e.getTag().equals(tagName))
            .findFirst()
            .orElseThrow(
                () ->
                    new TagNotFoundException(
                        String.format("Tag with name %s does not exists", tagName)));
    assetTagRelationRepository.deleteByTagId(entity.getId());
  }
}
