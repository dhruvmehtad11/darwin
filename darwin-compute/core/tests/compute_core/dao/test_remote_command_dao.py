import unittest
from unittest.mock import MagicMock

from compute_core.dao.remote_command_dao import RemoteCommandDao
from compute_core.dto.remote_command_dto import PodCommandExecutionStatusDto


class TestRemoteCommandDao(unittest.TestCase):
    def setUp(self):
        self.dao = RemoteCommandDao()

        self.fake_connection = MagicMock()
        self.dao.get_write_connection = MagicMock(return_value=self.fake_connection)
        self.dao.get_read_connection = MagicMock(return_value=self.fake_connection)

    def test_insert_pod_command_execution_status(self):
        request = PodCommandExecutionStatusDto(
            cluster_run_id="run_id-test",
            execution_id="execution_id-test",
            pod_name="id-test-pod-name",
            status="started",
        )
        self.fake_connection.cursor.lastrowid = 1
        resp = self.dao.insert_pod_command_execution_status(request)
        self.assertEqual(resp, 1)
