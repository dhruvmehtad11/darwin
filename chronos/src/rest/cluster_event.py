from loguru import logger

from fastapi import APIRouter, HTTPException

from src.constant.constants import sources_for_cluster
from src.dto.response.cluster_response import ClusterSessionsResponse, ClusterEventSource, ClusterEventSourcesResponse, \
    ClusterEventsResponse
from src.dto.schema.processed_event import (
    ClusterEventsRequest
)
from src.service.processed_events import ProcessedEventsService
from src.service.sources import SourceService



class ClusterEventRouter:
    def __init__(self):
        self.router = APIRouter()
        self.processed_event_service = ProcessedEventsService()
        self.source_service = SourceService()
        self.register_routes()

    def register_routes(self):
        self.router.post("/cluster/events", response_model=ClusterEventsResponse)(
            self.get_cluster_events)
        self.router.get("/cluster/sessions/{cluster_id}", response_model=ClusterSessionsResponse)(
            self.get_cluster_session)
        self.router.get("/cluster/sources", response_model=ClusterEventSourcesResponse)(
            self.get_cluster_sources)

    async def get_cluster_sources(self):
        try:
            all_sources = await self.source_service.get_all_sources()
            filtered_sources = [source for source in all_sources if source.SourceName in sources_for_cluster]
            mapped_sources = [
                ClusterEventSource(
                    id=source.SourceID,
                    label=source.SourceName,
                )
                for source in filtered_sources
            ]
            return ClusterEventSourcesResponse(
                data=mapped_sources
            )
        except Exception as e:
            logger.exception(f"Error getting cluster sources: {e}")
            raise HTTPException(status_code=500, detail="Error getting cluster sources")

    async def get_cluster_events(self, request: ClusterEventsRequest):
        try:
            processed_events = await self.processed_event_service.get_cluster_events(
                request.cluster_id,
                request.last_event_id,
                request.filters,
                request.page_size,
                request.offset,
            )

            return ClusterEventsResponse(
                data=processed_events,
                page_size=request.page_size,
                offset=request.offset,
            )
        except Exception as e:
            logger.exception(f"Error getting cluster events: {e}")
            raise HTTPException(status_code=500, detail="Error getting cluster events")

    async def get_cluster_session(self, cluster_id: str):
        try:
            sessions = await self.processed_event_service.get_cluster_sessions(
                cluster_id=cluster_id
            )

            return ClusterSessionsResponse(
                data=sessions
            )
        except Exception as e:
            logger.exception(f"Error getting cluster sessions: {e}")
            raise HTTPException(status_code=500, detail="Error getting cluster sessions")


cluster_event_router = ClusterEventRouter().router
