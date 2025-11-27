import re
from typing import List, Dict, Any

from .base import Validator
from .feature_store_validation import validate_feature_group_schema


class EmailValidator(Validator):
    def validate(self, value: str) -> bool:
        pattern = r"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"
        return bool(re.match(pattern, value))

    def get_error_message(self) -> str:
        return "Please enter a valid email address"


class FeatureGroupValidator(Validator):
    def validate(self, value: List[Dict[str, Any]]) -> bool:
        try:
            validate_feature_group_schema(value)
            return True
        except ValueError as e:
            print(e)
            return False

    def get_error_message(self) -> str:
        return "Please enter a valid feature group details"
