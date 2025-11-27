import json
from enum import Enum

import requests
from typeguard import typechecked


class AttachmentType(Enum):
    SUCCESS = "#00FF00"
    ERROR = "#FF0000"
    INFO = "#4792f5"
    WARNING = "#FFA500"


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
    def section(content: dict, title: str = None):
        """
        Creates a section block
        :params content: Dict containing keys and values to add as content to attachment
        """
        block = {"type": "section"}
        if title:
            block["text"] = {"type": "mrkdwn", "text": f"*{title}*"}
        block["fields"] = [{"type": "mrkdwn", "text": f"*{key}:*\n{value}"} for key, value in content.items()]
        return block

    @staticmethod
    def rich_text_section(content: dict[str, dict]):
        """
        Creates a rich text section block
        :params content: Text to be added as content to attachment
        """
        block = {"type": "rich_text", "elements": []}
        for key, value in content.items():
            block["elements"].append(
                {
                    "type": "rich_text_section",
                    "elements": [{"type": "text", "text": key, "style": {"bold": True}}],
                }
            )
            sections = []
            for k, v in value.items():
                sections.extend(
                    [
                        {"type": "text", "text": f"{k}:", "style": {"bold": True}},
                        {"type": "text", "text": f"{v}\t"},
                    ]
                )
            block["elements"].append({"type": "rich_text_preformatted", "elements": sections})
        return block


@typechecked
class Attachment:
    """
    Collection of Blocks
    """

    def __init__(self, msg_type: AttachmentType = AttachmentType.SUCCESS):
        self.attachment = [{"color": msg_type.value, "blocks": []}]

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
        attachment = Attachment(msg_type=AttachmentType.ERROR)
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
