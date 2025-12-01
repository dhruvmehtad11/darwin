import asyncio

from loguru import logger

from compute_app_layer.models.request.cluster_search import Filter
from compute_core.dao.async_es_dao import AsyncEsDao
from compute_core.dao.queries.es_queries import (
    search_labels_query,
    search_labels_paginated_query,
    get_label_values_query,
    search_query_v2,
)
from compute_core.util.utils import process_filters_v2


async def labels_search(query: str, page_size: int, offset: int) -> (int, dict):
    # Use two queries: one for count, one for paginated results
    count_query = search_labels_query(query)
    search_query = search_labels_paginated_query(query, page_size, offset)

    async with AsyncEsDao() as dao:
        logger.debug("Executing Elasticsearch queries for labels search")

        # Run both queries concurrently
        count_response, search_response = await asyncio.gather(
            dao.search_raw(count_query), dao.search_raw(search_query)
        )
        logger.debug(f"Elasticsearch queries completed for labels search for query: '{query}'")

        # Process paginated results
        matches: list[str] = []
        if "aggregations" in search_response and "matching_values" in search_response["aggregations"]:
            for bucket in search_response["aggregations"]["matching_values"]["buckets"]:
                matches.append(bucket["key"])
            logger.debug(f"Found {len(matches)} label matches for current page")

        # Get total count from count query
        total_matches = 0
        if "aggregations" in count_response and "matching_values" in count_response["aggregations"]:
            total_matches = len(count_response["aggregations"]["matching_values"]["buckets"])

        return total_matches, matches


async def get_labels_values(keys: list[str]) -> dict:
    query = get_label_values_query(keys)
    # Execute query
    async with AsyncEsDao() as dao:
        logger.debug("Executing Elasticsearch query for label values")
        response = await dao.search_raw(query)
        logger.debug("Elasticsearch query completed for label values")

    # Process results
    result = {}
    for key in keys:
        agg_key = f"{key}_values"
        if agg_key in response["aggregations"]:
            result[key] = [bucket["key"] for bucket in response["aggregations"][agg_key]["buckets"]]
        else:
            result[key] = []

    return result


async def search_cluster(
    query: str, filters: dict[str, Filter], sort_by: str, sort_order: str, limit: int, offset: int
) -> (list, int):
    query_fields = ["name"]
    sort_fields = {sort_by: sort_order}
    es_query = search_query_v2(
        query=query,
        query_fields=query_fields,
        filters=process_filters_v2(filters),
        sort_fields=sort_fields,
        limit=limit,
        offset=offset,
    )

    # Execute query
    async with AsyncEsDao() as dao:
        logger.debug(f"Executing Elasticsearch query for cluster search v2 with {es_query}")
        response = await dao.search_raw(es_query)
        logger.debug("Elasticsearch query completed for cluster search v2")

    search_cluster_res = [x["_source"] for x in response["hits"]["hits"]]
    result_size = response["hits"]["total"]["value"]

    return search_cluster_res, result_size
