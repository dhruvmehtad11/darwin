from ml_serve_model import User


class UserService:
    async def get_user_from_token(self, token: str) -> User:
        return await User.filter(token=token).first()
