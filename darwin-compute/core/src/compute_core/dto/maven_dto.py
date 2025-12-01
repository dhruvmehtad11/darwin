from dataclasses import dataclass


@dataclass
class MavenArtifacts:
    group_id: str
    artifact_id: str
    versions: list[str]


@dataclass
class MavenLibrary:
    result_size: int
    packages: list[dict]
