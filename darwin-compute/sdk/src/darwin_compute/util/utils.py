import yaml


def read_yaml(file_path: str):
    with open(file_path, "r", encoding="utf-8") as file:
        data = yaml.safe_load(file)
        return data
