from compute_app_layer.utils.response_util import Response
from compute_model.gpu_node import GPU_NODES_SPEC


async def get_gpu_pods_controller():
    return Response.success_response(message="GPU Pods Specification fetched successfully", data=GPU_NODES_SPEC)
