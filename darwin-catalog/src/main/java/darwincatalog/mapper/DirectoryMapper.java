package darwincatalog.mapper;

import darwincatalog.entity.AssetDirectoryEntity;
import org.mapstruct.Mapper;
import org.openapitools.model.SearchEntry;

@Mapper(componentModel = "spring")
public interface DirectoryMapper extends EntityResponseMapper<AssetDirectoryEntity, SearchEntry> {}
