package darwincatalog.service;

import static darwincatalog.util.Common.mapToDto;
import static darwincatalog.util.Constants.HIERARCHY_SEPARATOR;

import darwincatalog.entity.AssetDirectoryEntity;
import darwincatalog.exception.InvalidSqlException;
import darwincatalog.mapper.DirectoryMapper;
import darwincatalog.repository.AssetDirectoryRepository;
import darwincatalog.repository.AssetRepository;
import darwincatalog.service.work.AddDirectoryWork;
import darwincatalog.service.work.DeleteDirectoryWork;
import darwincatalog.service.work.WorkExecutor;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.jdbc.Work;
import org.openapitools.model.PaginatedList;
import org.openapitools.model.SearchEntry;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SearchService {
  private final AssetDirectoryRepository assetDirectoryRepository;
  private final DirectoryMapper directoryMapper;
  private final WorkExecutor workExecutor;
  private final AssetRepository assetRepository;

  public SearchService(
      AssetDirectoryRepository assetDirectoryRepository,
      DirectoryMapper directoryMapper,
      WorkExecutor workExecutor,
      AssetRepository assetRepository) {
    this.assetDirectoryRepository = assetDirectoryRepository;
    this.directoryMapper = directoryMapper;
    this.workExecutor = workExecutor;
    this.assetRepository = assetRepository;
  }

  public PaginatedList search(
      String nameRegex, String prefixRegex, Integer depth, Integer offset, Integer pageSize) {
    if (depth == null || depth < 0) {
      depth = -1;
    }
    List<AssetDirectoryEntity> assetDirectoryEntityPage =
        assetDirectoryRepository.findByNameAndPrefixMatchesRegex(
            nameRegex, prefixRegex, depth, pageSize, offset);
    long totalCount =
        assetDirectoryRepository.countByAssetNameAndAssetPrefixMatchesRegex(
            nameRegex, prefixRegex, depth);
    log.info(
        "handled search request with name regex: {}, refix regex: {} -> Total count: {}",
        nameRegex,
        prefixRegex,
        totalCount);
    List<SearchEntry> data = mapToDto(assetDirectoryEntityPage, directoryMapper);
    return new PaginatedList().data(data).offset(offset).pageSize(pageSize).total(totalCount);
  }

  public void insert(String fqdn) {
    List<AssetDirectoryEntity> assetDirectoryEntities = getPairs(fqdn);
    if (!assetExists(assetDirectoryEntities)) {
      Work work = new AddDirectoryWork(assetDirectoryEntities);
      workExecutor.execute(work);
      log.debug("asset directory updated with {}", fqdn);
    }
  }

  public void delete(String fqdn) {
    List<AssetDirectoryEntity> assetDirectoryEntities = getPairs(fqdn);
    if (assetExists(assetDirectoryEntities)) {
      log.info(
          "starting deletion of asset {}, after tokenising into {}", fqdn, assetDirectoryEntities);
      Work work = new DeleteDirectoryWork(assetDirectoryEntities);
      try {
        workExecutor.execute(work);
        log.info("asset directory deleted for {}", fqdn);
      } catch (Exception ex) {
        log.warn("failed while deleting from asset_directoy. ", ex);
        throw new InvalidSqlException();
      }
    }
  }

  private boolean assetExists(List<AssetDirectoryEntity> assetDirectoryEntities) {
    AssetDirectoryEntity terminalEntry =
        assetDirectoryEntities.get(assetDirectoryEntities.size() - 1);

    String name = terminalEntry.getAssetName();
    String prefix = terminalEntry.getAssetPrefix();
    int depth = terminalEntry.getDepth();
    return assetDirectoryRepository.existsByAssetNameAndAssetPrefixAndDepth(name, prefix, depth);
  }

  private List<AssetDirectoryEntity> getPairs(String s) {
    List<AssetDirectoryEntity> result = new ArrayList<>();
    if (s == null || s.isEmpty()) return result;
    int depth = 0;
    String[] parts = s.split(HIERARCHY_SEPARATOR);
    StringBuilder prefixBuilder = new StringBuilder();
    boolean isTerminal;
    for (int i = 0; i < parts.length; i++) {
      String name = parts[i];
      String prefix = (i == 0) ? "root" : prefixBuilder.toString();
      isTerminal = (i == parts.length - 1);
      result.add(
          AssetDirectoryEntity.builder()
              .assetPrefix(prefix)
              .assetName(name)
              .depth(depth++)
              .isTerminal(isTerminal)
              .build());

      if (i > 0) prefixBuilder.append(HIERARCHY_SEPARATOR);
      prefixBuilder.append(name);
    }
    return result;
  }
}
