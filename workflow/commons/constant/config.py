from commons.constant.constants import CONFIGS_MAP


class Config(object):
    def __init__(self, env):
        self.env = env
        self._config = CONFIGS_MAP[self.env]

    @property
    def get_elastic_url(self):
        return self._config['elasticsearch.url']

    @property
    def get_artifact_url(self):
        return self._config['jfrog.url']

    @property
    def monitoring_kafka_bootstrap_server(self):
        return self._config['monitoring.kafka.bootstrap_server']
