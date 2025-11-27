class TransformerRegistry:
    def __init__(self):
        self._registry = {}
        self._cache = {}

    def register(self, transformer_type, transformer_cls):
        """Registers a transformer class with the given type."""
        self._registry[transformer_type] = transformer_cls

    async def get_transformer(self, transformer_record):
        """Retrieves a transformer instance from the cache or creates a new one from the provided transformer record."""
        transformer_type = transformer_record.TransformerType
        transformer_id = transformer_record.TransformerID

        if transformer_type in self._cache and transformer_id in self._cache[transformer_type]:
            return self._cache[transformer_type][transformer_id]

        transformer_class = self._registry.get(transformer_type)
        if transformer_class:
            transformer_instance = transformer_class(transformer_record)
            if transformer_type not in self._cache:
                self._cache[transformer_type] = {}
            self._cache[transformer_type][transformer_id] = transformer_instance
            return transformer_instance
        raise ValueError(f"Transformer {transformer_type} with ID {transformer_id} not found")

    async def clear_cache(self):
        """Clears the cache."""
        self._cache = {}


# Singleton instance of TransformerRegistry
transformer_registry = TransformerRegistry()


def register_transformer(transformer_type):
    """Decorator to register a transformer class."""

    def decorator(cls):
        transformer_registry.register(transformer_type, cls)
        return cls

    return decorator
