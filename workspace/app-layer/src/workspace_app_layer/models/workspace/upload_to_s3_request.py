from pydantic import BaseModel


class UploadToS3Request(BaseModel):
    source_path: str
    s3_bucket: str
    destination_path: str
