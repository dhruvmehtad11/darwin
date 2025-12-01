from typing import List, Dict

from compute_app_layer.models.request.cluster_search import Filter


def search_cluster_name(name):
    query = {"query": {"bool": {"should": [{"term": {"name.keyword": {"value": name}}}]}}}
    return query


def get_job_clusters(offset: int, limit: int):
    query = {"query": {"term": {"is_job_cluster": {"value": True}}}, "size": limit, "from": offset}
    return query


def search_query(
    key: str,
    query_fields: List[str],
    filters: Dict[str, List],
    exclude_filters: Dict[str, List],
    sort_fields: Dict[str, str],
    limit: int,
    offset: int,
):
    key = f".*{key}.*"
    should_query = []
    filter_query = []
    exclude_query = []
    sort_query = []

    for query_field in query_fields:
        regexp_query = {"regexp": {}}
        regexp_query["regexp"][query_field] = {"value": key}
        should_query.append(regexp_query)

    keyword_fields = ["user", "status"]
    for filter_field, filter_values in filters.items():
        terms_query = {"terms": {}}
        filter_field = f"{filter_field}.keyword" if filter_field in keyword_fields else filter_field
        terms_query["terms"][filter_field] = filter_values
        filter_query.append(terms_query)

    keyword_fields = ["user", "status"]
    for filter_field, filter_values in exclude_filters.items():
        terms_query = {"terms": {}}
        filter_field = f"{filter_field}.keyword" if filter_field in keyword_fields else filter_field
        terms_query["terms"][filter_field] = filter_values
        exclude_query.append(terms_query)

    for sort_field, sort_order in sort_fields.items():
        sort_field = f"{sort_field}.keyword"
        sort_query.append({sort_field: {"order": sort_order}})

    final_query = {
        "query": {
            "bool": {
                "must": [
                    {"bool": {"should": should_query}},
                    {"bool": {"filter": filter_query}},
                ],
                "must_not": exclude_query,
            }
        },
        "sort": sort_query,
        "from": offset,
        "size": limit,
    }
    return final_query


def agg_query(field: str, size: int = 10000):
    agg_field = field + ".keyword"
    query = {
        "_source": False,
        "aggs": {"distinct": {"terms": {"field": agg_field, "size": size}}},
    }
    return query


def search_query_v2(
    query: str,
    query_fields: list[str],
    filters: dict[str, Filter],
    sort_fields: dict[str, str],
    limit: int,
    offset: int,
):
    keyword_fields = {"user", "status", "runtime", "cloud_env"}
    kw = lambda f: f"{f}.keyword" if f in keyword_fields else f

    must, filter_clauses, must_not = [], [], []

    # Text search
    if query := query.strip():
        query = f".*{query}.*"
        must.append({"bool": {"should": [{"regexp": {f: {"value": query}}} for f in query_fields]}})

    # Filters
    for field, fv in filters.items():
        add_to = must_not if fv.exclude else filter_clauses
        target = "labels_flat" if field == "labels" else kw(field)

        if not fv.select_all:
            add_to.append({"terms": {target: fv.values}})
        elif fv.query:
            add_to.append({"wildcard": {target: f"*{fv.query}*"}})

    # Sort
    sort_query = [{f"{f}.keyword": {"order": o}} for f, o in sort_fields.items()]

    # Query assembly
    if must or filter_clauses or must_not:
        query_obj = {
            "bool": {k: v for k, v in [("must", must), ("filter", filter_clauses), ("must_not", must_not)] if v}
        }
    else:
        query_obj = {"match_all": {}}

    return {"query": query_obj, "sort": sort_query, "from": offset, "size": limit}


def search_labels_query(query: str):
    # Use labels_flat field for ES-side aggregation with pattern matching
    # This mimics flattened field behavior with proper pagination support
    include_pattern = f".*{query}.*"

    query_result = {
        "size": 0,  # Only aggregations, no documents
        "query": {"match_all": {}},  # Get all documents to search across all labels
        "aggs": {
            "matching_values": {
                "terms": {"field": "labels_flat", "include": include_pattern, "size": 10000, "order": {"_key": "asc"}}
            }
        },
    }
    return query_result


def search_labels_paginated_query(query: str, page_size: int, offset: int):
    search_query = search_labels_query(query)
    paginated_query = {
        "paginated": {"bucket_sort": {"sort": [{"_key": {"order": "asc"}}], "from": offset, "size": page_size}}
    }
    search_query["aggs"]["matching_values"]["aggs"] = paginated_query
    return search_query


def get_label_values_query(label_keys: list[str]):
    # Build aggregations for each label key
    aggs = {}
    for key in label_keys:
        aggs[f"{key}_values"] = {"terms": {"field": f"labels.{key}.keyword", "size": 10000}}

    query = {"size": 0, "aggs": aggs}
    return query
