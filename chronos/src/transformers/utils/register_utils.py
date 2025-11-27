import importlib
from loguru import logger

def dynamic_import_module(module_name):
    """
    Dynamically import a module by name.
    """
    try:
        importlib.import_module(module_name)
    except ImportError as e:
        logger.exception(f"Error importing module {module_name}: {e}")


def register_transformers():
    """
    Register transformers by dynamically importing the necessary modules.
    """
    modules = [
        'src.transformers.transformers',
        'src.transformers.json_transformer',
        'src.transformers.python_transformer',
    ]

    for module in modules:
        dynamic_import_module(module)
