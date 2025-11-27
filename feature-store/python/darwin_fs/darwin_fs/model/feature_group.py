from dataclasses import dataclass
from typing import List, Optional

from dataclasses_json import dataclass_json, LetterCase

from darwin_fs.constant.constants import FeatureGroupType
from darwin_fs.model.feature_column import FeatureColumn


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class FeatureGroup:
  feature_group_name: str
  entity_name: str
  features: List[FeatureColumn]
  feature_group_type: Optional[FeatureGroupType] = FeatureGroupType.ONLINE
  version_enabled: Optional[bool] = True
