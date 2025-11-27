import pyhocon


def read_config(file_path):
    config = pyhocon.ConfigFactory.parse_file(file_path)
    return config.as_plain_ordered_dict()
