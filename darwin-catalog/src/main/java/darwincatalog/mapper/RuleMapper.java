package darwincatalog.mapper;

import darwincatalog.entity.RuleEntity;
import org.mapstruct.Mapper;
import org.openapitools.model.Rule;

@Mapper(componentModel = "spring")
public interface RuleMapper extends EntityResponseMapper<RuleEntity, Rule> {}
