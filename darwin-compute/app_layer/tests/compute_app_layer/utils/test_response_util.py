import unittest
from datetime import datetime

from fastapi import FastAPI
from fastapi.testclient import TestClient

from compute_app_layer.utils.response_util import Response


class TestResponse(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.app = FastAPI()

        @cls.app.get("/success")
        async def success():
            return Response.success_response(
                "This is a success message",
                {"key": "value", "timestamp": datetime(2023, 6, 19, 12, 0, 0)},
            )

        @cls.app.get("/created")
        async def created():
            return Response.created_response("Resource created")

        @cls.app.get("/accepted")
        async def accepted():
            return Response.accepted_response("Request accepted")

        @cls.app.get("/internal_error")
        async def internal_error():
            return Response.internal_server_error_response("Internal server error", {"error": "details"})

        @cls.app.get("/bad_request")
        async def bad_request():
            return Response.bad_request_error_response("Bad request error", {"error": "details"})

        @cls.app.get("/forbidden")
        async def forbidden():
            return Response.forbidden_error_response("Forbidden error", {"error": "details"})

        @cls.app.get("/not_found")
        async def not_found():
            return Response.not_found_error_response("Not found error", {"error": "details"})

        @cls.app.get("/conflict")
        async def conflict():
            return Response.conflict_error_response("Conflict error")

        cls.client = TestClient(cls.app)

    def test_success_response(self):
        response = self.client.get("/success")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(
            response.json(),
            {
                "status": "SUCCESS",
                "message": "This is a success message",
                "data": {"key": "value", "timestamp": "2023-06-19T12:00:00Z"},
            },
        )

    def test_created_response(self):
        response = self.client.get("/created")
        self.assertEqual(response.status_code, 201)
        self.assertEqual(response.json(), {"status": "SUCCESS", "message": "Resource created", "data": None})

    def test_accepted_response(self):
        response = self.client.get("/accepted")
        self.assertEqual(response.status_code, 202)
        self.assertEqual(response.json(), {"status": "SUCCESS", "message": "Request accepted", "data": None})

    def test_internal_server_error_response(self):
        response = self.client.get("/internal_error")
        self.assertEqual(response.status_code, 500)
        self.assertEqual(
            response.json(), {"status": "ERROR", "message": "Internal server error", "data": {"error": "details"}}
        )

    def test_bad_request_error_response(self):
        response = self.client.get("/bad_request")
        self.assertEqual(response.status_code, 400)
        self.assertEqual(
            response.json(), {"status": "ERROR", "message": "Bad request error", "data": {"error": "details"}}
        )

    def test_forbidden_error_response(self):
        response = self.client.get("/forbidden")
        self.assertEqual(response.status_code, 403)
        self.assertEqual(
            response.json(), {"status": "ERROR", "message": "Forbidden error", "data": {"error": "details"}}
        )

    def test_not_found_error_response(self):
        response = self.client.get("/not_found")
        self.assertEqual(response.status_code, 404)
        self.assertEqual(
            response.json(), {"status": "ERROR", "message": "Not found error", "data": {"error": "details"}}
        )

    def test_conflict_error_response(self):
        response = self.client.get("/conflict")
        self.assertEqual(response.status_code, 409)
        self.assertEqual(response.json(), {"status": "ERROR", "message": "Conflict error", "data": None})
