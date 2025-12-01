from fastapi.responses import JSONResponse
from fastapi.responses import Response


class BaseResponse:
    data: dict

    def __init__(self, data: dict):
        self.data = data

    def get_json_response(self, code: int = 200) -> Response:
        if self.data is None or len(self.data) == 0:
            return Response(status_code=204)
        return JSONResponse({"data": self.data}, code)

    @staticmethod
    def get_no_content_response() -> Response:
        return Response(status_code=204)
