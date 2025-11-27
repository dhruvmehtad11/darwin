from dataclasses import dataclass

from dataclasses_json import dataclass_json, LetterCase

from darwin_fs.model.feature_group import FeatureGroup


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class CreateFeatureGroupResponse:
  feature_group: FeatureGroup
  version: str
