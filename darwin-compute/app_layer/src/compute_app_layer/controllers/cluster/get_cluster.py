from loguru import logger

from compute_app_layer.utils.node_mapper import head_node_mapper, worker_nodes_mapper
from compute_app_layer.utils.response_handlers import ResponseHandler
from compute_app_layer.utils.response_util import Response
from compute_core.compute import Compute
from compute_core.service.ray_cluster import RayClusterService
from compute_model.cluster_status import ClusterStatus, UIClusterStatusMapping


async def get_cluster_controller(compute: Compute, cluster_id: str):
    with ResponseHandler() as handler:
        cluster_status_response = compute.get_cluster_metadata(cluster_id)
        logger.debug(f"Cluster status from MySQL for cluster {cluster_id}: {cluster_status_response}")
        cluster_details_response = compute.get_cluster(cluster_id).to_dict()
        logger.debug(f"Cluster details from ES for cluster {cluster_id} : {cluster_details_response}")
        instance_roles = compute.get_instance_role()

        instance_role = None
        for role in instance_roles:
            if cluster_details_response["advance_config"]["instance_role"].isdigit():
                if role["instance_role_id"] == cluster_details_response["advance_config"]["instance_role"]:
                    instance_role = role
                    break
            else:
                if role["display_name"] == cluster_details_response["advance_config"]["instance_role"]:
                    instance_role = role
                    break
        cluster_details_response["advance_config"]["instance_role"] = instance_role
        logger.debug(
            "Cluster instance role: %s",
            cluster_details_response["advance_config"]["instance_role"],
        )

        spark_config = cluster_details_response["advance_config"]["spark_config"]
        cluster_details_response["advance_config"]["spark_config"] = {
            key.replace("\\.", "."): value for key, value in spark_config.items()
        }

        availability_zones = compute.get_az()
        availability_zone = None
        for az in availability_zones:
            if az["az_id"] == cluster_details_response["advance_config"]["availability_zone"]:
                availability_zone = az
                break
        cluster_details_response["advance_config"]["availability_zone"] = availability_zone
        logger.debug(
            f"Cluster availability zone: %s",
            cluster_details_response["advance_config"]["availability_zone"],
        )

        cluster_details_response["advance_config"]["init_script"] = "\n".join(
            cluster_details_response["advance_config"]["init_script"]
        )

        dashboards = compute.get_dashboards(cluster_id)
        logger.debug(f"Cluster dashboards: {dashboards}")

        head_node_config = head_node_mapper(cluster_details_response["head_node"])
        logger.debug(f"Cluster Head node config: {head_node_config}")
        worker_node_configs = worker_nodes_mapper(cluster_details_response["worker_group"])
        logger.debug(f"Cluster Worker node configs: {worker_node_configs}")

        is_job_cluster = (
            True
            if "is_job_cluster" in cluster_details_response and cluster_details_response["is_job_cluster"]
            else False
        )
        logger.debug(f"Cluster {cluster_id} is job cluster: {is_job_cluster}")

        response = {
            "cluster_id": cluster_details_response["cluster_id"],
            "name": cluster_details_response["name"],
            "tags": cluster_details_response["tags"],
            "labels": cluster_details_response["labels"],
            "runtime": cluster_details_response["runtime"],
            "auto_termination_policies": cluster_details_response["auto_termination_policies"],
            "inactive_time": cluster_details_response["terminate_after_minutes"],
            "status": UIClusterStatusMapping.get_status(ClusterStatus[cluster_status_response["status"]]),
            "user": cluster_details_response["user"],
            "head_node_config": head_node_config,
            "worker_node_configs": worker_node_configs,
            "advance_config": cluster_details_response["advance_config"],
            "dashboards": {"status": "SUCCESS", "data": dashboards},
            "created_on": cluster_details_response["created_on"],
            "is_job_cluster": is_job_cluster,
        }
        handler.response = Response.success_response("Cluster Details Fetched Successfully", response)

    return handler.response


async def get_running_clusters_controller(compute: Compute, running_time: int):
    try:
        logger.debug("Fetching clusters running for more than %s minutes", running_time)
        resp = compute.get_all_clusters_running_for_threshold_time(running_time)
        data = {"clusters": resp, "running_time": running_time}
        return Response.success_response(message="Clusters Fetched Successfully", data=data)
    except Exception as e:
        logger.exception(f"Failed to fetch clusters running for more than {running_time} minutes, Error: {e}")
        return Response.internal_server_error_response(
            f"Failed to fetch clusters running for more than {running_time} minutes",
            {"error": str(e)},
        )


async def get_head_node_ip_controller(compute: Compute, cluster_id: str):
    try:
        ray_dashboard_url = compute.get_internal_dashboards(cluster_id).get("ray_dashboard_url")
        logger.debug(f"Ray dashboard url: {ray_dashboard_url} for cluster_id: {cluster_id}")
        resp = RayClusterService(ray_dashboard_url).get_summary()
        logger.debug(f"Ray cluster summary response: {resp} for cluster_id: {cluster_id}")

        # Find the head node by checking isHeadNode flag in raylet data
        head_node_ip = None
        for node in resp:
            if node["raylet"]["isHeadNode"]:
                head_node_ip = node["ip"]
                break

        if not head_node_ip:
            raise Exception("Head node not found in cluster")

        return Response.success_response(
            message="Head node IP fetched successfully", data={"head_node_ip": head_node_ip}
        )
    except Exception as e:
        logger.error(f"Failed to fetch head node IP for cluster_id: {cluster_id}, Error: {e.__str__()}")
        return Response.internal_server_error_response(message=e.__str__(), data=None)
