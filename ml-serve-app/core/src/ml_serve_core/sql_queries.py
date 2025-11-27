GET_UNPICKED_ARTIFACTS_QUERY = """
SELECT * FROM artifact_builder_job
WHERE status='PENDING'
AND last_picked_at <= NOW() - INTERVAL '10' second
ORDER BY status_last_updated_at
LIMIT 1
FOR
UPDATE SKIP LOCKED;
"""