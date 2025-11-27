from typing import List


def head_node_mapper(head_node: dict):
    if head_node["node_type"] == "gpu":
        return {
            "head_node_cores": head_node["node"]["cores"],
            "head_node_memory": head_node["node"]["memory"],
            "node_type": head_node["node_type"],
            "node_capacity_type": None,
            "gpu_pod": {
                "name": head_node["node"]["name"],
                "cores": head_node["node"]["cores"],
                "memory": head_node["node"]["memory"],
                "gpu_count": head_node["node"]["gpu_count"],
                "g_ram_memory": head_node["node"]["g_ram_memory"],
                "g_ram_type": head_node["node"]["g_ram_type"],
            },
        }
    else:
        return {
            "head_node_cores": head_node["node"]["cores"],
            "head_node_memory": head_node["node"]["memory"],
            "node_type": head_node["node_type"],
            "node_capacity_type": head_node["node"]["node_capacity_type"],
            "gpu_pod": None,
        }


def worker_node_mapper(node: dict):
    if node["node_type"] == "gpu":
        return {
            "cores": node["node"]["cores"],
            "memory": node["node"]["memory"],
            "min_pods": node["min_pods"],
            "max_pods": node["max_pods"],
            "disk_setting": None,
            "node_type": node["node_type"],
            "node_capacity_type": None,
            "gpu_pod": {
                "name": node["node"]["name"],
                "cores": node["node"]["cores"],
                "memory": node["node"]["memory"],
                "gpu_count": node["node"]["gpu_count"],
                "g_ram_memory": node["node"]["g_ram_memory"],
                "g_ram_type": node["node"]["g_ram_type"],
            },
        }
    else:
        return {
            "cores": node["node"]["cores"],
            "memory": node["node"]["memory"],
            "min_pods": node["min_pods"],
            "max_pods": node["max_pods"],
            "disk_setting": node["node"]["disk"],
            "node_type": node["node_type"],
            "node_capacity_type": node["node"]["node_capacity_type"],
            "gpu_pod": None,
        }


def worker_nodes_mapper(worker_nodes: List[dict]):
    return [worker_node_mapper(node) for node in worker_nodes]
