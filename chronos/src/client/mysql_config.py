from pydantic import BaseModel


class MysqlConfig(BaseModel):
    masterHost: str
    slaveHost: str
    database: str
    username: str
    password: str
