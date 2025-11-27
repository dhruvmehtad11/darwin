from compute_core.util.utils import is_valid_email


def test_is_valid_email():
    # Test case: Valid email addresses
    assert is_valid_email("user@dream11.com") == True  # correct
    assert is_valid_email("user@dreamsports.group") == True  # correct

    # Test case: Invalid email addresses
    assert is_valid_email("plainaddress") == False  # Missing '@' symbol
    assert is_valid_email("example@") == False  # Missing domain part
    assert is_valid_email("@domain.com") == False  # Missing local part
    assert is_valid_email("example@domain") == False  # Missing period in domain
    assert is_valid_email("example@domain.") == False  # Missing domain extension
    assert is_valid_email("example@domain.c") == False  # Minimal domain extension (still valid)
