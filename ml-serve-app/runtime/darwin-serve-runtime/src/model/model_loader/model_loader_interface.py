from abc import ABC


class ModelLoaderInterface(ABC):
    def __init__(self):
        pass

    def load_model(self):
        pass

    def reload_model(self):
        pass
