from compute_core.util.utils import default_events


def test_default_events():
    all_event = ["test", "test1"]
    default_event = {"test"}
    resp = default_events(all_event, default_event)
    expected = [{"event": "test", "default": True}, {"event": "test1", "default": False}]
    assert expected == resp
