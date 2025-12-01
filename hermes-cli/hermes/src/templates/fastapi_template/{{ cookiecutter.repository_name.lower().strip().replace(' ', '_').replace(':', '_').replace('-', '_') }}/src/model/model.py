from typing import Any
from .model_loader.model_loader_interface import ModelLoaderInterface


class Model:
    def __init__(self, model_loader: ModelLoaderInterface):
        self.model: Any = model_loader.load_model()

    async def predict(self, input_data: Any) -> Any:
        return await self.inference(input_data)

    async def inference(self, input_data: Any) -> Any:
        # TODO: Implement the inference logic here using custom predict function of the model
        pass
