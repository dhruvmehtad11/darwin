import asyncio

from typeguard import typechecked

from compute_app_layer.models.response.runtime_v2 import RuntimesByClass, RuntimesByType
from compute_app_layer.models.runtime_v2 import (
    RuntimeClassEnum,
    RuntimeTypeEnum,
    RuntimeV2Request,
    GetRuntimesRequest,
    RuntimeV2Details,
    RuntimeComponent,
)
from compute_core.constant.constants import RuntimeV2ValidTypes
from compute_core.dao.runtime_dao import RuntimeV2Dao
from compute_core.constant.config import Config
from compute_core.util.utils import retry_with_exponential_backoff


@typechecked
class RuntimeV2:
    def __init__(self, env: str = None):
        self._config = Config(env)
        env = self._config.env
        self.runtime_v2_dao = RuntimeV2Dao(env)

    @retry_with_exponential_backoff(retries=3, delay=0.1, backoff=2.0)
    async def _get_runtime_details_with_retries(self, runtime_id: int):
        return await self._get_runtime_details(runtime_id)

    async def _get_runtime_details(self, runtime_id: int):
        runtime_aw = self.runtime_v2_dao.get_runtime_by_id(runtime_id)
        components_aw = self.runtime_v2_dao.get_runtime_components(runtime_id)

        runtime, components = await asyncio.gather(runtime_aw, components_aw)

        if not runtime:
            raise ValueError(f"Runtime with id: {runtime_id} not found")
        if runtime["type"] is not None:
            runtime["type"] = RuntimeTypeEnum(runtime["type"])
        runtime["class"] = RuntimeClassEnum(runtime["class"])

        component_list = [RuntimeComponent(**comp) for comp in components] if components else None
        runtime["components"] = component_list

        return RuntimeV2Details(**runtime)

    async def _get_runtimes_by_type(self, runtime_class: RuntimeClassEnum, request: GetRuntimesRequest):
        valid_types = RuntimeV2ValidTypes.CPU if runtime_class == RuntimeClassEnum.CPU else RuntimeV2ValidTypes.GPU
        if request.type:
            valid_types = [t for t in valid_types if t == request.type]

        result = []
        for runtime_type in valid_types:
            # Get runtimes of the specified type
            runtimes = self.runtime_v2_dao.get_runtimes_by_type(
                runtime_class=runtime_class,
                search_query=request.search_query,
                offset=request.offset,
                page_size=request.page_size,
                type=runtime_type,
                is_deleted=request.is_deleted,
            )
            # Get components for each runtime
            for runtime in runtimes:
                runtime["components"] = await self.runtime_v2_dao.get_runtime_components(runtime["id"])
            # Get total count for the type
            count = self.runtime_v2_dao.get_count_by_class_and_type(
                runtime_class=runtime_class,
                search_query=request.search_query,
                type=runtime_type,
                is_deleted=request.is_deleted,
            )

            result.append(
                RuntimesByType(type=runtime_type, count=count, runtime_list=[RuntimeV2Details(**r) for r in runtimes])
            )
        return result

    async def _get_custom_runtimes(self, runtime_class: RuntimeClassEnum, request: GetRuntimesRequest, user: str):
        valid_types = RuntimeV2ValidTypes.CUSTOM
        if request.type:
            valid_types = [t for t in valid_types if t == request.type]

        result = []
        for runtime_type in valid_types:
            # Get runtimes of the specified type
            runtimes = self.runtime_v2_dao.get_custom_runtimes(
                runtime_class=runtime_class,
                created_by=user,
                search_query=request.search_query,
                offset=request.offset,
                page_size=request.page_size,
                type=runtime_type,
                is_deleted=request.is_deleted,
            )
            # Get components for each runtime
            for runtime in runtimes:
                runtime["components"] = await self.runtime_v2_dao.get_runtime_components(runtime["id"])
            # Get total count for the type
            count = self.runtime_v2_dao.get_count_by_type_for_custom_class(
                created_by=user,
                runtime_type=runtime_type,
                search_query=request.search_query,
                is_deleted=request.is_deleted,
            )

            result.append(
                RuntimesByType(type=runtime_type, count=count, runtime_list=[RuntimeV2Details(**r) for r in runtimes])
            )
        return result

    async def get_all_runtimes(self, request: GetRuntimesRequest, user: str):
        # Get all classes if not specified
        valid_classes = [request.class_] if request.class_ else list(RuntimeClassEnum)
        result = []

        for runtime_class in valid_classes:
            if runtime_class != RuntimeClassEnum.CUSTOM:
                # Get default runtime for the class
                res = self.runtime_v2_dao.get_default_runtime_details_by_class(runtime_class)
                default_runtime = await self._get_runtime_details(res["runtime_id"]) if res else None

                # Get runtimes grouped by type
                runtimes_by_type = await self._get_runtimes_by_type(runtime_class, request)
            else:
                default_runtime = None
                runtimes_by_type = await self._get_custom_runtimes(runtime_class, request, user)

            # Get total count for the class
            total_count = self.runtime_v2_dao.get_total_count_by_class(
                runtime_class=runtime_class, is_deleted=request.is_deleted, search_query=request.search_query
            )

            result.append(
                RuntimesByClass(
                    **{"class": runtime_class},
                    default_runtime=default_runtime,
                    total_count=total_count,
                    runtimes=runtimes_by_type,
                )
            )
        return result

    async def get_runtime_details(self, runtime: str):
        # Check if runtime exists
        result = self.runtime_v2_dao.get_runtime_by_name(runtime)
        if not result:
            raise ValueError(f"Runtime: {runtime} not found")
        if result["is_deleted"]:
            raise ValueError(f"Runtime: {runtime} is deleted")

        return await self._get_runtime_details(result["id"])

    async def soft_delete_runtime(self, runtime: str):
        # Check if runtime exists and is not already deleted
        result = self.runtime_v2_dao.get_runtime_by_name(runtime)
        if not result:
            raise ValueError(f"Runtime: {runtime} not found. Verify runtime name and try again.")
        if result["is_deleted"]:
            raise ValueError(f"Runtime: {runtime} is already deleted.")

        # Check if runtime is set as default
        if self.runtime_v2_dao.check_if_default_runtime(result["id"]):
            raise ValueError(f"Cannot delete runtime: {runtime} as it is set as the default")

        # Soft delete the runtime
        self.runtime_v2_dao.soft_delete_runtime(runtime)

    async def update_runtime(self, request: RuntimeV2Request):
        # Check if runtime exists
        runtime = self.runtime_v2_dao.get_runtime_by_name(request.runtime)
        if not runtime:
            raise ValueError(f"Runtime: {request.runtime} not found")
        if runtime["is_deleted"]:
            raise ValueError(f"Runtime: {request.runtime} is deleted")

        # Update runtime
        self.runtime_v2_dao.update_runtime(runtime["id"], request)
        # Update components
        if request.components:
            # Delete existing components
            self.runtime_v2_dao.delete_components(runtime["id"])
            # Insert new components
            for component in request.components:
                self.runtime_v2_dao.insert_component(runtime["id"], component)

        # Handle default runtime setting
        if request.set_as_default:
            self.runtime_v2_dao.set_as_default_runtime(runtime["id"], request.class_)

        # Get complete runtime details including components
        return await self._get_runtime_details_with_retries(runtime["id"])

    async def create_runtime(self, request: RuntimeV2Request):
        # Check if runtime exists
        runtime = self.runtime_v2_dao.get_runtime_by_name(request.runtime)

        if runtime:
            if not runtime["is_deleted"]:
                raise ValueError(f"Runtime {request.runtime} already exists")
            # Update the soft-deleted runtime
            runtime_id = runtime["id"]
            self.runtime_v2_dao.update_soft_deleted_runtime(runtime_id, request)
        else:
            # Create new runtime
            runtime_id = self.runtime_v2_dao.create_runtime(request)
            # Update components
            if request.components:
                for component in request.components:
                    self.runtime_v2_dao.insert_component(runtime_id, component)
            # Handle default runtime setting
            if request.set_as_default:
                self.runtime_v2_dao.set_as_default_runtime(runtime_id, request.class_)

        # Get complete runtime details including components
        return await self._get_runtime_details_with_retries(runtime_id)
