from confluent_kafka import admin
from typeguard import typechecked


@typechecked
class KafkaAdmin:
    def __init__(self, bootstrap_server: str):
        self.admin_client = admin.AdminClient({"bootstrap.servers": bootstrap_server})

    def healthcheck(self, consumer_group_name: str):
        consumer_groups = self.admin_client.list_consumer_groups().result()
        consumer_groups = consumer_groups.valid

        for consumer_group in consumer_groups:
            if consumer_group.group_id == consumer_group_name:
                if consumer_group.state.name in ["STABLE", "PREPARING_REBALANCING"]:
                    return {"status": "OK", "consumer_group": consumer_group_name, "state": consumer_group.state.name}
                else:
                    return {
                        "status": "NOT OK",
                        "consumer_group": consumer_group_name,
                        "state": consumer_group.state.name,
                    }
        return {"status": "NOT OK", "consumer_group": None, "state": "No Consumers Found"}
