from compute_app_layer.models.request.library import InstallRequest, MvnRepository
from compute_core.dto.library_dto import LibraryDTO, LibrarySource


def test_install_request_from_library_dto():
    library_dto = LibraryDTO(
        name="test_library",
        path="/path/to/library",
        version="1.0.0",
        metadata={"repository": "maven", "exclusions": None},
        source=LibrarySource.S3,
    )
    install_request = InstallRequest.from_library_dto(library_dto)
    assert install_request.source == library_dto.source
    assert install_request.body.name == library_dto.name
    assert install_request.body.path == library_dto.path
    assert install_request.body.version == library_dto.version
    assert install_request.body.metadata.repository == MvnRepository(library_dto.metadata["repository"])
    assert install_request.body.metadata.exclusions == library_dto.metadata["exclusions"]


def test_install_request_from_library_without_metadata():
    library_dto = LibraryDTO(
        name="test_library",
        path="/path/to/library",
        version="1.0.0",
        metadata=None,
        source=LibrarySource.S3,
    )
    install_request = InstallRequest.from_library_dto(library_dto)
    assert install_request.source == library_dto.source
    assert install_request.body.name == library_dto.name
    assert install_request.body.path == library_dto.path
    assert install_request.body.version == library_dto.version
    assert install_request.body.metadata is None
