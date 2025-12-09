import json

SERIALIZER_OPTIONS = {
    'STRING_SER': lambda k: k.encode('utf-8') if k is not None else k,
    'JSON_SER': lambda v: json.dumps(v).encode('utf-8') if v is not None else v
}


def get_serializer(name: str):
    ser = SERIALIZER_OPTIONS.get(name)
    if ser is None:
        raise Exception(f'No Serializer found with name {name}')
    return ser
