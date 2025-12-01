import unittest

from compute_core.util.utils import format_maven_response


class TestMavenUtilities(unittest.TestCase):

    def setUp(self):
        # Set up the test data for both functions
        self.maven_response = {
            "response": {
                "docs": [
                    {"g": "com.example", "a": "artifact1", "latestVersion": "1.0.0"},
                    {"g": "com.example", "a": "artifact2", "latestVersion": "2.0.0"},
                    {"g": "org.example", "a": "artifact3", "latestVersion": "1.1.0"},
                ],
                "numFound": 3,
            }
        }

    def test_format_maven_response(self):
        # Now test formatting the sorted response
        formatted_resp = format_maven_response(
            self.maven_response["response"]["docs"], self.maven_response["response"]["numFound"]
        )

        self.assertEqual(formatted_resp.result_size, 3)
        self.assertEqual(len(formatted_resp.packages), 3)
        self.assertEqual(
            formatted_resp.packages[0], {"group_id": "com.example", "artifact_id": "artifact1", "version": "1.0.0"}
        )
        self.assertEqual(
            formatted_resp.packages[1], {"group_id": "com.example", "artifact_id": "artifact2", "version": "2.0.0"}
        )
        self.assertEqual(
            formatted_resp.packages[2], {"group_id": "org.example", "artifact_id": "artifact3", "version": "1.1.0"}
        )
