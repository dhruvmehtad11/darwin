from typing import Optional, Any
from pydantic import BaseModel, Field


class Filter(BaseModel):
    """
    Represents a single filter entry for the search request.
    """

    select_all: bool = Field(default=False, description="Whether to select all values for this filter")
    values: Optional[list] = Field(
        default_factory=list, description="List of values to filter by if select_all is False"
    )
    query: Optional[str] = Field(default="", description="Query string to use if select_all is True")
    exclude: bool = Field(default=False, description="Whether to exclude all values for this filter")


class SearchClusterRequest(BaseModel):
    query: str = ""
    filters: dict[str, Filter] = Field(default_factory=dict)
    sort_by: str
    sort_order: str
    page_size: int
    offset: int

    def __init__(self, **data: Any):
        filters = data.get("filters")
        if not filters:
            # No filters provided or filters is empty, set default
            data["filters"] = {"is_job_cluster": Filter(values=[False])}
        else:
            # If "is_job_cluster" not in filters, add default
            if "is_job_cluster" not in filters:
                filters = dict(filters)  # make a copy to avoid mutating input
                filters["is_job_cluster"] = Filter(values=[False])
                data["filters"] = filters
            # else: user provided "is_job_cluster", so use as is
        super().__init__(**data)
