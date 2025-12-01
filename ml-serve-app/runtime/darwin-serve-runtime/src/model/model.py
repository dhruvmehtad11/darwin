from typing import Any
from .model_loader.model_loader_interface import ModelLoaderInterface


class Model:
    def __init__(self, model_loader: ModelLoaderInterface):
        self._model_loader: ModelLoaderInterface = model_loader
        self.model: Any | None = None

    def _ensure_model_loaded(self) -> None:
        if self.model is None:
            self.model = self._model_loader.load_model()

    async def predict(self, input_data: Any) -> Any:
        self._ensure_model_loaded()
        return await self.inference(input_data)

    async def inference(
        self,
        input_data: Any,
    ) -> dict:
        """
        Inference for a LightGBM ranking model with features provided
        strictly as a list-of-lists (no NumPy/Pandas).

        Args:
          X: list of lists, shape (n_rows, n_features).
          top_k: if set, also return the indices of the top_k rows by score (desc).
          return_global_order: if True, include indices that sort all rows by score (desc).

        Returns:
          {
            "scores": List[float],
            "global_order": Optional[List[int]],  # indices sorted by score desc
            "top_k_indices": Optional[List[int]]  # first top_k of global_order
          }
        """
        self._ensure_model_loaded()
        prediction = self.model.predict(input_data)
        # Ensure JSON-serializable response
        try:
            scores = prediction.tolist()
        except AttributeError:
            scores = prediction
        return {"scores": scores}




