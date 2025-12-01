import json

import requests
from typeguard import typechecked


@typechecked
class Block:
    @staticmethod
    def header(title: str):
        """
        Creates a header block
        """
        return {
            "type": "header",
            "text": {"type": "plain_text", "text": title, "emoji": True},
        }

    @staticmethod
    def divider():
        """
        Creates a divider line
        """
        return {"type": "divider"}

    @staticmethod
    def section(content: dict):
        """
        Creates a section block
        :params content: Dict containing keys and values to add as content to attachment
        """
        block = {"type": "section", "fields": []}
        for key, value in content.items():
            block["fields"].append({"type": "mrkdwn", "text": f"*{key}:*\n{value}"})
        return block


@typechecked
class Attachment:
    """
    Collection of Blocks
    """

    def __init__(self, error: bool = False):
        self.attachment = [{"color": "#FF0000" if error else "#00FF00", "blocks": []}]

    def add_block(self, block):
        """
        Adds a block to an attachment
        """
        self.attachment[0]["blocks"].append(block)

    def get_attachment(self):
        """
        Returns attachment
        """
        return self.attachment

    @staticmethod
    def error(err):
        """
        For directly creating an error attachment
        :params err: Error message
        """
        attachment = Attachment(error=True)
        attachment.add_block(Block.header("ERROR"))
        attachment.add_block(Block.divider())
        params = {"Message": err}
        attachment.add_block(Block.section(params))
        attachment.add_block(Block.divider())
        return attachment.get_attachment()

    @staticmethod
    def message(title: str, content: dict):
        """
        For directly creating a message attachment
        :params title: title of attachment
        :params content: Dict containing keys and values to add as content to attachment
        """
        attachment = Attachment()
        attachment.add_block(Block.header(title))
        attachment.add_block(Block.divider())
        params = {}
        for key, value in content.items():
            params[key] = value
            if params.__len__ == 2:
                attachment.add_block(Block.section(params))
                params = {}
        if params:
            attachment.add_block(Block.section(params))
        attachment.add_block(Block.divider())
        return attachment.get_attachment()


@typechecked
class SlackAlert:
    def __init__(self, token: str, username: str, channel: str):
        self.token = token
        self.username = username
        self.channel = channel
        self.thread_id = None

    def post_message(self, text: str = None, attachments=None):
        """
        Posts message in the Slack channel
        :params text: Optional.
        :params attachment: Optional.
        Either one or both of the parameters can be present
        """
        response = requests.post(
            "https://slack.com/api/chat.postMessage",
            {
                "token": self.token,
                "username": self.username,
                "channel": self.channel,
                "thread_ts": self.thread_id,
                "text": text,
                "attachments": json.dumps(attachments) if attachments else None,
            },
        ).json()
        if not response["ok"]:
            raise Exception(response["error"])
        if not self.thread_id:
            self.thread_id = response["ts"]
        return self.thread_id

    def post_image(self, file_path: str, title: str = None):
        """
        Posts image in the Slack channel
        :params file_path: Path to the image file.
        :params title: Optional.
        """
        if not title:
            title = file_path.split("/")[-1]
        response = requests.post(
            url="https://slack.com/api/files.upload",
            data={
                "filename": title,
                "token": self.token,
                "channels": [f"#{self.channel}"],
                "thread_ts": self.thread_id,
            },
            files={"file": (title, open(file_path, "rb"), "png")},
        ).json()
        if not response["ok"]:
            raise Exception(response["error"])
        return self.thread_id

    def new_conversation(self):
        """
        Creates a new thread_id
        """
        self.thread_id = None
