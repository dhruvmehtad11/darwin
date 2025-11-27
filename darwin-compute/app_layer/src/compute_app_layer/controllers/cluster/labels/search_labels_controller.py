from loguru import logger

from compute_app_layer.utils.response_util import Response
from compute_core.dao.async_cluster_dao import labels_search


def _validate_search_params(query: str, page_size: int, offset: int) -> str:
    """
    Validate search parameters for labels controller.

    Args:
        query: Search query string (can be empty to browse all labels)
        page_size: Number of results per page
        offset: Starting position for pagination

    Returns:
        str: Cleaned and validated query string (empty string if browsing all)

    Raises:
        ValueError: If input parameters are invalid
    """
    # Allow empty queries for "browse all labels" functionality
    if query is None:
        raise ValueError("Query cannot be None")

    # Clean the query (allow empty string after trimming)
    cleaned_query = query.strip() if query else ""

    if page_size <= 0:
        raise ValueError("Page size must be positive")
    if offset < 0:
        raise ValueError("Offset cannot be negative")

    # Apply different limits based on query type
    if cleaned_query == "":
        # Browsing all labels - stricter limits
        if page_size > 50:
            raise ValueError("Page size cannot exceed 50 when browsing all labels")
        if offset > 1000:
            raise ValueError("Offset cannot exceed 1000 when browsing all labels")
    else:
        # Searching specific labels - more generous limits
        if page_size > 1000:
            raise ValueError("Page size cannot exceed 1000")

    return cleaned_query


async def search_labels_controller(query: str, page_size: int, offset: int):
    try:
        # Validate input parameters
        validated_query = _validate_search_params(query, page_size, offset)

        logger.debug(f"Searching labels with query: '{validated_query}', page_size: {page_size}, offset: {offset}")

        # Get data from Elasticsearch
        total_matches, matches = await labels_search(validated_query, page_size, offset)

        total_pages = (total_matches + page_size - 1) // page_size if total_matches > 0 else 0
        current_page = (offset // page_size) + 1

        logger.debug(
            f"Search results: {len(matches)} matches, {total_matches} total, page {current_page}/{total_pages}"
        )

        data = {
            "query": validated_query,
            "matches": matches,
            "pagination": {
                "total_matches": total_matches,
                "page_size": page_size,
                "current_page": current_page,
                "total_pages": total_pages,
                "has_next": offset + page_size < total_matches,
                "has_previous": offset > 0,
            },
        }

        return Response.success_response("Labels search completed successfully", data)

    except ValueError as e:
        logger.error(f"Invalid parameters for labels search: {e}")
        return Response.bad_request_error_response(str(e), {"query": query, "page_size": page_size, "offset": offset})

    except Exception as e:
        logger.exception(f"Failed to search labels with query '{query}': {e}")
        return Response.internal_server_error_response("Failed to search labels", {"error": str(e), "query": query})
