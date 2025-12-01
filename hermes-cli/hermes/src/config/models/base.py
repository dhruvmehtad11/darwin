from dataclasses import dataclass
from typing import List, Optional, Any, Dict, Type, Union


@dataclass
class ConfigField:
    name: str
    type: Union[Type, Dict]
    required: bool = False
    description: str = ""
    default: Any = None
    validator: Optional["Validator"] = None
    choices: Optional[List[str]] = None
    is_list: bool = False


@dataclass
class ConfigSection:
    name: str
    fields: List[ConfigField]
    required: bool = False
    conditional_on: Optional[Dict[str, str]] = None
