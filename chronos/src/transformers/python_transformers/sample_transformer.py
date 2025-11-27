from src.dto.schema import RawEvent
from src.dto.schema.base_transformers import TransformerOutput, ProcessedEventData, EntityData, RelationData
from src.transformers.base_python_transformer import BasePythonTransformer


class MyTransformerClass(BasePythonTransformer):
    async def is_transformer_applicable(self, event_data: RawEvent) -> bool:
        return True

    async def transform(self, event_data: RawEvent) -> TransformerOutput:

        processed_data = ProcessedEventData(
            EventType="PodEvent",
            EntityID="EntityId",
            EventData=event_data
        )
        sample_entity = EntityData(
            EntityID="EntityId",
            EntityType="Pod"
        )

        sample_relation = RelationData(
            SourceEntityID="SEntityId",
            DestinationEntityID="DEntityId"
        )

        return TransformerOutput(
            processed_events=[processed_data],
            entities=[sample_entity],
            links=[sample_relation]
        )
