from dataclasses import dataclass

from dataclasses_json import LetterCase, dataclass_json

from darwin_fs.model.feature_group_version import FeatureGroupVersion


@dataclass_json(letter_case=LetterCase.CAMEL, undefined="EXCLUDE")
@dataclass
class VersionMetadata:
  version: FeatureGroupVersion
