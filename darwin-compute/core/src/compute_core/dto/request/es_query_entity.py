import json
from dataclasses import dataclass


@dataclass
class ESSearchQuery:
    query: dict

    def get_query(self) -> any:
        return json.loads(json.dumps(self.query))
