import json
from typing import Dict, Any, List
from loguru import logger
from src.dto.schema import RawEvent
from src.dto.schema.base_transformers import TransformerOutput, ProcessedEventData
from src.transformers.registry import register_transformer
from src.transformers.transformers import TransformerStrategy


@register_transformer('JSONTransformer')
class JSONTransformer(TransformerStrategy):
    def __init__(self, transformer_record):
        super().__init__(transformer_record)
        self.event_mappings = transformer_record.EventMappings
        self.entity_path = transformer_record.EntityPath
        self.entity_type = transformer_record.EntityType
        self.event_type = transformer_record.EventType
        self.matching_entity = transformer_record.MatchingEntity
        self.matching_path = transformer_record.MatchingPath
        self.matching_value = transformer_record.MatchingValue
        self.relations = transformer_record.Relations
        self.message = transformer_record.Message
        self.severity = transformer_record.Severity

    async def apply(self, raw_event: RawEvent) -> TransformerOutput:
        try:
            raw_data = json.loads(raw_event.EventData)
        except json.JSONDecodeError as e:
            logger.exception(f"Error in applying transformer for data - {raw_event}: {e}")
            return self._get_empty_transformer_output()

        if not self._is_matching_entity(raw_data):
            return self._get_empty_transformer_output()

        final_entity_id = self._navigate_path(raw_data, self.entity_path)
        final_event_data = self._process_event_data(raw_data)

        concrete_variable_dict = self.get_concrete_variable_dict(final_entity_id, self.entity_type, "")
        final_event_type = self.replace_variables_and_paths(self.event_type, concrete_variable_dict, raw_data)

        concrete_variable_dict = self.get_concrete_variable_dict(final_entity_id, self.entity_type, final_event_type)
        final_message = self.get_message(self.message, concrete_variable_dict, raw_data)

        final_severity = self.replace_variables_and_paths(self.severity, concrete_variable_dict, raw_data)

        processed_data = ProcessedEventData(
            EventType=final_event_type,
            EntityID=final_entity_id,
            EventData=final_event_data,
            Severity=final_severity,
            Message=final_message
        )
        entities, relations = self._map_relations(raw_data)

        return TransformerOutput(
            processed_events=[processed_data],
            entities=entities,
            links=relations
        )

    def get_concrete_variable_dict(self, entity_id: str, entity_type: str, event_type: str):
        return {
            "EntityID": entity_id,
            "EntityType": entity_type,
            "EventType": event_type
        }

    def get_message(self, message_config: str, variables: Dict[str, str], json_obj: Dict[str, Any]):

        if (message_config is None):
            message_config = "The event with event type {$EventType} happened for entity {$EntityID}"
        return self.replace_variables_and_paths(message_config, variables, json_obj)

    def _get_empty_transformer_output(self) -> TransformerOutput:
        return TransformerOutput(processed_events=None, entities=None, links=None)

    def _is_matching_entity(self, raw_data: Dict[str, Any]) -> bool:
        if self.matching_entity == 'value':
            logger.info(f"Matching value from json: {self._navigate_path(raw_data, self.matching_path)}")
            logger.info(f"Matching value from transformer: {self.matching_value}")
            logger.info(f"Matching value check: {self._navigate_path(raw_data, self.matching_path) == self.matching_value}")
            return self._navigate_path(raw_data, self.matching_path) == self.matching_value
        if self.matching_entity == 'key':
            return self._path_exists(raw_data, self.matching_path)
        return False

    def _process_event_data(self, raw_data: Dict[str, Any]) -> Dict[str, Any]:
        processed_data = {mapping['to']: self._navigate_path(raw_data, mapping['from'])
                          for mapping in self.event_mappings}
        return processed_data

    def _map_relations(self, raw_data: Dict[str, Any]) -> (List[Dict[str, Any]], List[Dict[str, Any]]):
        entities = [{"EntityID": self._navigate_path(raw_data, self.entity_path), "EntityType": self.entity_type}]
        relations = []

        for relation in self.relations:
            from_value = self._navigate_path(raw_data, relation['from'])
            to_value = self._navigate_path(raw_data, relation['to'])
            if from_value and to_value:
                relations.append({'SourceEntityID': from_value, 'DestinationEntityID': to_value})
                entities.append({"EntityID": from_value, "EntityType": relation["fromType"]})
                entities.append({"EntityID": to_value, "EntityType": relation["toType"]})

        return entities, relations

    def _navigate_path(self, data: dict, path: str) -> any:
        for key in path.split('.'):
            data = data.get(key, None)
            if data is None:
                return None
        return data

    def _path_exists(self, data: Dict[str, Any], path: str) -> bool:
        for key in path.split('.'):
            if isinstance(data, dict) and key in data:
                data = data[key]
            else:
                return False
        return True

    def _set_nested_path(self, data: Dict[str, Any], path: str, value: Any):
        keys = path.split('.')
        for key in keys[:-1]:
            if key not in data or not isinstance(data[key], dict):
                data[key] = {}
            data = data[key]
        data[keys[-1]] = value

    def extract_nested_brackets(self, s):
        stack = []
        results = []
        for i, char in enumerate(s):
            if char == '{':
                stack.append(i)
            elif char == '}' and stack:
                start = stack.pop()
                if not stack:
                    results.append((start, i))
        return results

    def resolve_value(self, path, variables, json_obj):
        if path.startswith('$'):
            variable_name = path[1:]
            return variables.get(variable_name, '')
        else:
            value = self._navigate_path(json_obj, path)
            if (value is None):
                return ""
            return value

    def recursive_replace(self, s, variables, json_obj):
        nested_brackets = self.extract_nested_brackets(s)
        if not nested_brackets:
            return s

        for start, end in reversed(nested_brackets):
            inner_text = s[start + 1:end]
            replacement = self.resolve_value(inner_text, variables, json_obj)
            s = s[:start] + str(replacement) + s[end + 1:]

        return self.recursive_replace(s, variables, json_obj)

    def replace_variables_and_paths(self, input_string, variables, json_obj):
        return self.recursive_replace(input_string, variables, json_obj)
