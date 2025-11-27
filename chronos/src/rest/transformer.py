from typing import List, Union
from loguru import logger
from fastapi import APIRouter, HTTPException

from src.dto.schema.transformer import (
    TransformerUpdate,
    JSONTransformerResponse, JSONTransformerCreate,
    PythonTransformerResponse,
    PythonTransformerCreate
)
from src.service.transformers import TransformerService


class TransformerRouter:
    def __init__(self):
        self.router = APIRouter()
        self.transformer_service = TransformerService()
        self.register_routes()

    def register_routes(self):
        self.router.post("/transformers/", response_model=Union[JSONTransformerResponse, PythonTransformerResponse])(
            self.create_transformer)
        self.router.get("/transformers/{transformer_id}",
                        response_model=Union[JSONTransformerResponse, PythonTransformerResponse])(self.read_transformer)
        self.router.put("/transformers/{transformer_id}",
                        response_model=Union[JSONTransformerResponse, PythonTransformerResponse])(
            self.update_transformer)
        self.router.delete("/transformers/{transformer_id}",
                           response_model=Union[JSONTransformerResponse, PythonTransformerResponse])(
            self.delete_transformer)
        self.router.get("/transformers/",
                        response_model=List[Union[JSONTransformerResponse, PythonTransformerResponse]])(
            self.read_transformers)
        self.router.get("/transformers/source/{source_id}",
                        response_model=List[Union[JSONTransformerResponse, PythonTransformerResponse]])(
            self.get_transformers_by_source)

    async def create_transformer(self, transformer: Union[JSONTransformerCreate, PythonTransformerCreate]):
        return await self.transformer_service.create_transformer(transformer)

    async def read_transformer(self, transformer_id: int):
        transformer = await self.transformer_service.get_transformer(transformer_id)
        if not transformer:
            raise HTTPException(status_code=404, detail="Transformer not found")
        return transformer

    async def update_transformer(self, transformer_id: int, transformer: TransformerUpdate):
        return await self.transformer_service.update_transformer(transformer_id, transformer)

    async def delete_transformer(self, transformer_id: int):
        return await self.transformer_service.delete_transformer(transformer_id)

    async def read_transformers(self):
        return await self.transformer_service.get_all_transformers()

    async def get_transformers_by_source(self, source_id: int):
        try:
            return await self.transformer_service.get_all_transformers_by_source(source_id)
        except Exception as e:
            logger.exception(f"Error in getting transformers for source_id - {source_id} : {e}")
            raise HTTPException(status_code=500, detail=str(e))


transformer_router = TransformerRouter().router
