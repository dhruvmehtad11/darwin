from src.transformers.python_transformers.enums.events import PodEvent, ComputeEvent, AwsEvent, SparkEvent


def get_ui_events():
    enums = [PodEvent, ComputeEvent, AwsEvent, SparkEvent]
    enum_values = []

    for enum in enums:
        for e in enum:
            if e.value[2]:
                enum_values.append(e.name)

    return enum_values
