import importlib

from src.dto.schema import RawEvent
from src.dto.schema.base_transformers import TransformerOutput
from src.transformers.base_python_transformer import BasePythonTransformer
from src.transformers.registry import register_transformer
from src.transformers.transformers import TransformerStrategy


@register_transformer('PythonTransformer')
class PythonTransformer(TransformerStrategy):
    def __init__(self, transformer_record):
        super().__init__(transformer_record)
        self.script_path = transformer_record.ScriptPath
        self.class_name = transformer_record.ClassName
        self.function_name = "transform"
        self.eligible_function_name = "is_transformer_applicable"

    async def apply(self, raw_event: RawEvent) -> TransformerOutput:
        spec = importlib.util.spec_from_file_location("transformer_script", self.script_path)
        transformer_module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(transformer_module)
        transformer_class = getattr(transformer_module, self.class_name)
        transformer_instance = transformer_class()

        # Ensure the class extends BasePythonTransformer
        if not isinstance(transformer_instance, BasePythonTransformer):
            raise TypeError(f"{self.class_name} must extend BasePythonTransformer")

        # Call the transform function
        transform_function = getattr(transformer_instance, self.function_name)
        is_transformation_eligible_func = getattr(transformer_instance, self.eligible_function_name)

        if not await is_transformation_eligible_func(raw_event):
            return None
        return await transform_function(raw_event)
