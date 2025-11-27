from src.consumers.configs.config_constants import CONFIG_MAP


def get_sqs_config(env: str):
    """Get SQS configuration for the specified environment"""
    
    base_config = CONFIG_MAP.get(env, CONFIG_MAP["local"])
    
    sqs_configs = {
        "writer_configs": {
            "region": base_config.get("sqs_region"),
        },
        "consumer_configs": {
            "region": base_config.get("sqs_region"),
            "queue_name": base_config.get("raw_event_queue"),
            "visibility_timeout": 30,
            "max_messages": 10
        }
    }

    if env == "local":
        sqs_configs["writer_configs"]["endpoint_url"] = base_config.get("sqs_endpoint_url")
        sqs_configs["consumer_configs"]["endpoint_url"] = base_config.get("sqs_endpoint_url")

    return sqs_configs
