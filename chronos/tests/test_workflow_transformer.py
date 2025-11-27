import pytest

from src.transformers.python_transformers.workflow_transformer import WorkflowEventsTransformer
from src.dto.schema.base_transformers import TransformerOutput, ProcessedEventData


class TestWorkflowEventsTransformer:

    def setup_method(self):
        self.transformer = WorkflowEventsTransformer()

    @pytest.mark.asyncio
    async def test_is_transformer_applicable(self, event_data):
        assert await self.transformer.is_transformer_applicable(event_data) == True

    @pytest.mark.asyncio
    async def test_transform(self, event_data):
        expected_output = TransformerOutput(processed_events=([ProcessedEventData(
            EventType='WORKFLOW_PAUSED',
            EntityID='wf_id-n9q4usu8rkyjyoagome94mhwjs',
            EventData={
                'entity': 'WORKFLOW',
                'entity_id': 'wf_id-n9q4usu8rkyjyoagome94mhwjs',
                'state': 'WORKFLOW_PAUSED',
                'metadata': {
                    'workflow_name': 'integration_test_workflow_2e762722-fbf9-4c8b-8819-73295f0cf2d3'
                },
                'timestamp': '2024-06-20T15:10:21',
                'message': 'Workflow paused',
                'severity': 'info'
            },
            Message='Workflow paused',
            Severity='info'

        )]), links=[], entities=[])
        actual_output = await self.transformer.transform(event_data)
        assert actual_output == expected_output
