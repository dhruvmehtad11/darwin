from compute_core.util.utils import urljoin


def test_urljoin_http():
    # Test case 1: Joining URLs with http
    result = urljoin("www.example.com", "api", "endpoint")
    assert result == "http://www.example.com/api/endpoint"


def test_urljoin_https():
    # Test case 2: Joining URLs with https
    result = urljoin("https://www.example.com", "api", "endpoint", https=True)
    assert result == "https://www.example.com/api/endpoint"


def test_urljoin_first_url_ending_in_slash():
    # Test case 3: Joining URLs with the first URL ending in /
    result = urljoin("www.example.com/", "api", "endpoint")
    assert result == "http://www.example.com/api/endpoint"


def test_urljoin_second_url_starting_with_slash():
    # Test case 4: Joining URLs with the second URL starting with /
    result = urljoin("www.example.com", "/api", "endpoint")
    assert result == "http://www.example.com/api/endpoint"


def test_urljoin_first_url_ending_in_slash_and_second_url_starting_with_slash():
    # Test case 5: Joining URLs with first url ending in / and second url starting in /
    result = urljoin("www.example.com/", "/api/", "/endpoint")
    assert result == "http://www.example.com/api/endpoint"


def test_urljoin_no_slash_present():
    # Test case 6: Joining two URLs with no slash present
    result = urljoin("www.example.com", "api", "endpoint")
    assert result == "http://www.example.com/api/endpoint"


def test_urljoin_http_present_from_starting():
    # Test case 7: Joining urls with http present from starting
    result = urljoin("http://www.example.com", "api", "endpoint")
    assert result == "http://www.example.com/api/endpoint"


def test_urljoin_https_present_from_starting():
    # Test case 8: Joining urls with https present from starting
    result = urljoin("https://www.example.com", "api", "endpoint")
    assert result == "https://www.example.com/api/endpoint"
