from pydantic import BaseModel


class CreateExperimentRequest(BaseModel):
    experiment_name: str


class UpdateExperimentRequest(BaseModel):
    experiment_name: str
