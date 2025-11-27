from dataclasses import dataclass, field
from typing import Optional

from dataclasses_json import dataclass_json, LetterCase

from darwin_fs.model.write_features import WriteFeatures


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class WriteFeaturesRequest:
  feature_group_name: str
  features: WriteFeatures
  feature_group_version: Optional[str] = None
