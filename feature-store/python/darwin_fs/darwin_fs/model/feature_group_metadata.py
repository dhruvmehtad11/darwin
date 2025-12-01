from dataclasses import dataclass, field
from typing import List

from dataclasses_json import dataclass_json, LetterCase

from darwin_fs.model.feature_group import FeatureGroup
from darwin_fs.constant.constants import FeatureGroupType, State
from darwin_fs.model.tenant_config import TenantConfig


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class FeatureGroupMetadata:
  name: str
  version_enabled: bool
  version: str
  feature_group_type: FeatureGroupType
  feature_group: FeatureGroup
  owner: str
  state: State
  tags: List[str]
  tenant: TenantConfig
  description: str
  created_at: int
  updated_at: int