from typing import Union

from tortoise.transactions import atomic

from src.dto.schema.transformer import (
    Transformer, TransformerUpdate,
    JSONTransformerResponse, JSONTransformerCreate,
    PythonTransformerResponse,
    PythonTransformerCreate
)
from src.models.models import JSONTransformer, Transformer, Source
from src.models.models import PythonTransformer


class TransformerService:
    @atomic("default")
    async def create_transformer(self, transformer: Union[JSONTransformerCreate, PythonTransformerCreate]):
        source = await Source.get(SourceID=transformer.SourceID)

        transformer_obj = await Transformer.create(
            TransformerName=transformer.TransformerName,
            TransformerType=transformer.TransformerType,
            Description=transformer.Description,
            Source=source,
            ProcessType=transformer.ProcessType
        )

        if isinstance(transformer, JSONTransformerCreate):
            json_transformer_obj = await JSONTransformer.create(
                Transformer=transformer_obj,
                MatchingEntity=transformer.MatchingEntity,
                MatchingPath=transformer.MatchingPath,
                MatchingValue=transformer.MatchingValue,
                EventMappings=transformer.EventMappings,
                EntityPath=transformer.EntityPath,
                EntityType=transformer.EntityType,
                EventType=transformer.EventType,
                Relations=transformer.Relations,
                Message=transformer.Message,
                Severity=transformer.Severity

            )
            return JSONTransformerResponse(
                TransformerID=transformer_obj.TransformerID,
                TransformerName=transformer_obj.TransformerName,
                TransformerType=transformer_obj.TransformerType,
                Description=transformer_obj.Description,
                SourceID=transformer_obj.Source_id,
                ProcessType=transformer_obj.ProcessType,
                MatchingEntity=json_transformer_obj.MatchingEntity,
                MatchingPath=json_transformer_obj.MatchingPath,
                MatchingValue=json_transformer_obj.MatchingValue,
                EventMappings=json_transformer_obj.EventMappings,
                EntityPath=json_transformer_obj.EntityPath,
                EntityType=json_transformer_obj.EntityType,
                EventType=json_transformer_obj.EventType,
                Relations=json_transformer_obj.Relations,
                Message=json_transformer_obj.Message,
                Severity=json_transformer_obj.Severity
            )

        elif isinstance(transformer, PythonTransformerCreate):
            python_transformer_obj = await PythonTransformer.create(
                Transformer=transformer_obj,
                ScriptPath=transformer.ScriptPath,
                ClassName=transformer.ClassName
            )
            return PythonTransformerResponse(
                TransformerID=transformer_obj.TransformerID,
                TransformerName=transformer_obj.TransformerName,
                TransformerType=transformer_obj.TransformerType,
                Description=transformer_obj.Description,
                SourceID=transformer_obj.Source_id,
                ProcessType=transformer_obj.ProcessType,
                ScriptPath=python_transformer_obj.ScriptPath,
                ClassName=python_transformer_obj.ClassName
            )

        raise ValueError("Unsupported transformer type")

    async def get_transformer(self, transformer_id: int):
        transformer = await JSONTransformer.get_or_none(Transformer__TransformerID=transformer_id).prefetch_related(
            'Transformer')
        if transformer:
            return JSONTransformerResponse(
                TransformerID=transformer.Transformer.TransformerID,
                TransformerName=transformer.Transformer.TransformerName,
                TransformerType=transformer.Transformer.TransformerType,
                Description=transformer.Transformer.Description,
                SourceID=transformer.Transformer.Source_id,
                ProcessType=transformer.Transformer.ProcessType,
                MatchingEntity=transformer.MatchingEntity,
                MatchingPath=transformer.MatchingPath,
                MatchingValue=transformer.MatchingValue,
                EventMappings=transformer.EventMappings,
                EntityPath=transformer.EntityPath,
                EntityType=transformer.EntityType,
                EventType=transformer.EventType,
                Relations=transformer.Relations,
                Messsage=transformer.Message,
                Severity=transformer.Severity
            )
        transformer = await PythonTransformer.get_or_none(Transformer__TransformerID=transformer_id).prefetch_related(
            'Transformer')
        if transformer:
            return PythonTransformerResponse(
                TransformerID=transformer.Transformer.TransformerID,
                TransformerName=transformer.Transformer.TransformerName,
                TransformerType=transformer.Transformer.TransformerType,
                Description=transformer.Transformer.Description,
                SourceID=transformer.Transformer.Source_id,
                ProcessType=transformer.Transformer.ProcessType,
                ScriptPath=transformer.ScriptPath,
                ClassName=transformer.ClassName
            )
        return None

    async def update_transformer(self, transformer_id: int, transformer: TransformerUpdate):
        existing_transformer = await JSONTransformer.get_or_none(
            Transformer__TransformerID=transformer_id).prefetch_related('Transformer')
        if existing_transformer:
            await existing_transformer.update_from_dict(transformer.dict())
            await existing_transformer.save()
            return JSONTransformerResponse(
                TransformerID=existing_transformer.Transformer.TransformerID,
                TransformerName=existing_transformer.Transformer.TransformerName,
                TransformerType=existing_transformer.Transformer.TransformerType,
                Description=existing_transformer.Transformer.Description,
                SourceID=existing_transformer.Transformer.Source_id,
                ProcessType=existing_transformer.Transformer.ProcessType,
                MatchingEntity=existing_transformer.MatchingEntity,
                MatchingPath=existing_transformer.MatchingPath,
                MatchingValue=existing_transformer.MatchingValue,
                EventMappings=existing_transformer.EventMappings,
                EntityPath=existing_transformer.EntityPath,
                EntityType=existing_transformer.EntityType,
                EventType=existing_transformer.EventType,
                Relations=existing_transformer.Relations,
                Message=existing_transformer.Message,
                Severity=existing_transformer.Severity
            )
        existing_transformer = await PythonTransformer.get_or_none(
            Transformer__TransformerID=transformer_id).prefetch_related('Transformer')
        if existing_transformer:
            await existing_transformer.update_from_dict(transformer.dict())
            await existing_transformer.save()
            return PythonTransformerResponse(
                TransformerID=existing_transformer.Transformer.TransformerID,
                TransformerName=existing_transformer.Transformer.TransformerName,
                TransformerType=existing_transformer.Transformer.TransformerType,
                Description=existing_transformer.Transformer.Description,
                SourceID=existing_transformer.Transformer.Source_id,
                ProcessType=existing_transformer.Transformer.ProcessType,
                ScriptPath=existing_transformer.ScriptPath,
                ClassName=existing_transformer.ClassName
            )
        raise ValueError("Transformer not found")

    async def delete_transformer(self, transformer_id: int):
        transformer = await JSONTransformer.get_or_none(Transformer__TransformerID=transformer_id).prefetch_related(
            'Transformer')
        if transformer:
            await transformer.delete()
            return JSONTransformerResponse(
                TransformerID=transformer.Transformer.TransformerID,
                TransformerName=transformer.Transformer.TransformerName,
                TransformerType=transformer.Transformer.TransformerType,
                Description=transformer.Transformer.Description,
                SourceID=transformer.Transformer.Source_id,
                ProcessType=transformer.Transformer.ProcessType,
                MatchingEntity=transformer.MatchingEntity,
                MatchingPath=transformer.MatchingPath,
                MatchingValue=transformer.MatchingValue,
                EventMappings=transformer.EventMappings,
                EntityPath=transformer.EntityPath,
                EntityType=transformer.EntityType,
                EventType=transformer.EventType,
                Relations=transformer.Relations,
                Message=transformer.Message,
                Severity=transformer.Severity
            )
        transformer = await PythonTransformer.get_or_none(Transformer__TransformerID=transformer_id).prefetch_related(
            'Transformer')
        if transformer:
            await transformer.delete()
            return PythonTransformerResponse(
                TransformerID=transformer.Transformer.TransformerID,
                TransformerName=transformer.Transformer.TransformerName,
                TransformerType=transformer.Transformer.TransformerType,
                Description=transformer.Transformer.Description,
                SourceID=transformer.Transformer.Source_id,
                ProcessType=transformer.Transformer.ProcessType,
                ScriptPath=transformer.ScriptPath,
                ClassName=transformer.ClassName
            )
        raise ValueError("Transformer not found")

    async def get_all_transformers(self):
        json_transformers = await JSONTransformer.all().prefetch_related('Transformer')
        python_transformers = await PythonTransformer.all().prefetch_related('Transformer')
        return [
            JSONTransformerResponse(
                TransformerID=transformer.Transformer.TransformerID,
                TransformerName=transformer.Transformer.TransformerName,
                TransformerType=transformer.Transformer.TransformerType,
                Description=transformer.Transformer.Description,
                SourceID=transformer.Transformer.Source_id,
                ProcessType=transformer.Transformer.ProcessType,
                MatchingEntity=transformer.MatchingEntity,
                MatchingPath=transformer.MatchingPath,
                MatchingValue=transformer.MatchingValue,
                EventMappings=transformer.EventMappings,
                EntityPath=transformer.EntityPath,
                EntityType=transformer.EntityType,
                EventType=transformer.EventType,
                Relations=transformer.Relations,
                Message=transformer.Message,
                Severity=transformer.Severity
            ) for transformer in json_transformers
        ] + [
            PythonTransformerResponse(
                TransformerID=transformer.Transformer.TransformerID,
                TransformerName=transformer.Transformer.TransformerName,
                TransformerType=transformer.Transformer.TransformerType,
                Description=transformer.Transformer.Description,
                SourceID=transformer.Transformer.Source_id,
                ProcessType=transformer.Transformer.ProcessType,
                ScriptPath=transformer.ScriptPath,
                ClassName=transformer.ClassName
            ) for transformer in python_transformers
        ]

    async def get_all_transformers_by_source(self, source_id: int):
        json_transformers = await JSONTransformer.filter(Transformer__Source_id=source_id).prefetch_related(
            'Transformer')
        python_transformers = await PythonTransformer.filter(Transformer__Source_id=source_id).prefetch_related(
            'Transformer')
        return [
            JSONTransformerResponse(
                TransformerID=transformer.Transformer.TransformerID,
                TransformerName=transformer.Transformer.TransformerName,
                TransformerType=transformer.Transformer.TransformerType,
                Description=transformer.Transformer.Description,
                SourceID=transformer.Transformer.Source_id,
                ProcessType=transformer.Transformer.ProcessType,
                MatchingEntity=transformer.MatchingEntity,
                MatchingPath=transformer.MatchingPath,
                MatchingValue=transformer.MatchingValue,
                EventMappings=transformer.EventMappings,
                EntityPath=transformer.EntityPath,
                EntityType=transformer.EntityType,
                EventType=transformer.EventType,
                Relations=transformer.Relations,
                Message=transformer.Message,
                Severity=transformer.Severity
            ) for transformer in json_transformers
        ] + [
            PythonTransformerResponse(
                TransformerID=transformer.Transformer.TransformerID,
                TransformerName=transformer.Transformer.TransformerName,
                TransformerType=transformer.Transformer.TransformerType,
                Description=transformer.Transformer.Description,
                SourceID=transformer.Transformer.Source_id,
                ProcessType=transformer.Transformer.ProcessType,
                ScriptPath=transformer.ScriptPath,
                ClassName=transformer.ClassName
            ) for transformer in python_transformers
        ]
