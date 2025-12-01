from datetime import datetime

HEALTHCHECK_QUERY = "SELECT 1;"

ADD_EVENT_QUERY = "INSERT INTO Events (source, entity_id, event_type, timestamp, details) VALUES ( %s, %s, %s, %s, %s );"

ADD_RAW_EVENT_QUERY = "INSERT INTO RawEvents (source, timestamp, details) VALUES ( %s, %s, %s );"

GET_EVENT_QUERY = "SELECT * FROM Events WHERE event_id = %s;"

ADD_RELATION_QUERY = "INSERT INTO EventEntityRelation (from_entity_id, to_entity_id) VALUES (%s, %s);"

GET_RELATION_QUERY = "SELECT * FROM EventEntityRelation where relation_id = %s;"

GET_RELATION_BY_FROM_ID_QUERY = "SELECT * FROM EventEntityRelation where from_entity_id = %s;"


def get_search_events_query(eventType: str, entityId: str, startTime: datetime, endTime: datetime,
                            pageSize: int, offset: int):
    if eventType is not None and entityId is not None:
        query = "SELECT * FROM Events WHERE event_type = %s AND entity_id = %s AND timestamp >= FROM_UNIXTIME(%s) AND timestamp <= FROM_UNIXTIME(%s) LIMIT %s OFFSET %s;"
        params = (eventType, entityId, startTime.timestamp(), endTime.timestamp(), pageSize, offset)
        return query, params

    if eventType is not None:
        query = "SELECT * FROM Events WHERE event_type = %s AND timestamp >= FROM_UNIXTIME(%s) AND timestamp <= FROM_UNIXTIME(%s) LIMIT %s OFFSET %s;"
        params = (eventType, startTime.timestamp(), endTime.timestamp(), pageSize, offset)
        return query, params

    if entityId is not None:
        query = "SELECT * FROM Events WHERE entity_id = %s AND timestamp >= FROM_UNIXTIME(%s) AND timestamp <= FROM_UNIXTIME(%s) LIMIT %s OFFSET %s;"
        params = (entityId, startTime.timestamp(), endTime.timestamp(), pageSize, offset)
        return query, params

    raise ValueError("At least one of eventType or entityId must be provided.")
