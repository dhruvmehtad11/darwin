import re

# Constants for GitHub URL parsing
GITHUB_ORG_PATTERN = r'github\.com/([^/]+)/'
GIT_FC_TOKEN='fc-token'
GIT_TOKEN='dream11-token'

# Organization to token mapping
ORG_TOKEN_MAPPING = {
    'ds-fancode': GIT_FC_TOKEN,
    'dream11': GIT_TOKEN,
    # Add more organizations as needed
}


def _extract_github_organization(repo_url: str) -> str:
    """
    Extract organization name from GitHub URL.

    :param repo_url: The repository URL
    :return: Organization name or default organization
    """
    match = re.search(GITHUB_ORG_PATTERN, repo_url)
    if match:
        return match.group(1)
    return 'dream11'  # Default organization


def _get_git_token_for_organization(org_name: str) -> str:
    """
    Get the appropriate git token for an organization.

    :param org_name: The organization name
    :return: The appropriate git token
    """
    return ORG_TOKEN_MAPPING.get(org_name, GIT_TOKEN)


if __name__ == "__main__":
    rst1 = _extract_github_organization('https://github.com/ds-fancode/')
    print('rst1', rst1)
    rst2 = _get_git_token_for_organization(rst1)
    print('rst2', rst2)
    print("Hello, World!")
