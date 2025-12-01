from typing import Annotated, Union

from fastapi import Request, HTTPException, Depends
from starlette import status

from ml_serve_core.service.user_service import UserService
from ml_serve_model import User

user_service = UserService()


async def get_current_user(request: Request) -> User:
    """
    Middleware that runs before every request.
    It checks for an authorization header, validates it,
    and adds a `user` attribute to `request.state`.
    """
    # Grab the authorization header
    auth_header = request.headers.get("Authorization")

    # If no authorization header is present, reject the request
    if not auth_header:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Authorization header is required."
        )

    # Check if header exists and follows the 'Bearer <token>' format
    if not auth_header or not auth_header.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid format of the token. The token should be in the format 'Bearer <token>'."
        )

    # Extract the token
    token = auth_header.removeprefix("Bearer ").strip()

    # Get the user from the token
    user = await user_service.get_user_from_token(token)

    # If the token is invalid, reject the request
    if not user:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Invalid or expired token."
        )

    return user


AuthorizedUser = Annotated[User, Depends(get_current_user)]
