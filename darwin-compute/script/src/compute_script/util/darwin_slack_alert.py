from loguru import logger

from compute_script.constant.config import Config
from compute_script.constant.constants import SLACK_TOKEN, SLACK_USERNAME
from compute_script.util.format_time_to_ist import format_time_to_ist
from compute_script.util.slack_alert import (
    SlackAlert,
    Attachment,
    Block,
    AttachmentType,
)


class DarwinSlackAlert:
    def __init__(self, env: str = None):
        self._config = Config(env)
        self._env = self._config.env
        self._slack = SlackAlert(SLACK_TOKEN, SLACK_USERNAME, self._config.get_slack_channel)

    def error(self, err):
        try:
            self._slack.new_conversation()
            logger.debug(f"Posting Error Message: {err}")
            self._slack.post_message(attachments=Attachment.error(str(err)))
        except Exception as err:
            logger.error(f"Error Posting Error Message - {err}")
            self._slack.post_message(text=f"Attachment Creation Error in Error Message in {self._env}")

    def cluster_termination(self, cluster_id, cluster_name, last_usage_time):
        try:
            self._slack.new_conversation()
            params = {
                "Cluster ID": cluster_id,
                "Cluster Name": cluster_name,
                "Last Usage": format_time_to_ist(last_usage_time),
            }
            logger.debug(f"Posting Cluster Termination Message for {cluster_id}: {params}")
            self._slack.post_message(attachments=Attachment.message(f"CLUSTER TERMINATED ENV:{self._env}", params))
        except Exception as err:
            logger.exception(f"Error Posting Cluster Termination Message for {cluster_id}: {err}")
            self.error(f"Failed to send cluster termination slack alert for {cluster_id}:{cluster_name} - {err}")

    def cluster_timeout(self, cluster_id: str, cluster_name: str):
        try:
            self._slack.new_conversation()
            params = {
                "Cluster ID": cluster_id,
                "Cluster Name": cluster_name,
            }
            logger.debug(f"Posting Cluster Timeout Message for {cluster_id}: {params}")
            self._slack.post_message(attachments=Attachment.message(f"CLUSTER TIMED OUT ENV:{self._env}", params))
        except Exception as err:
            logger.exception(f"Error Posting Cluster Timeout Message for {cluster_id}: {err}")
            self.error(f"Failed to send cluster Timeout slack alert for {cluster_id}:{cluster_name} - {err}")

    def cluster_error(self, cluster_id: str, cluster_name: str, err, tb):
        try:
            self._slack.new_conversation()
            attachment = Attachment(msg_type=AttachmentType.ERROR)
            attachment.add_block(Block.header(f"CLUSTER ERROR ENV:{self._env}"))
            attachment.add_block(Block.divider())
            params = {
                "Cluster ID": cluster_id,
                "Cluster Name": cluster_name,
                "Message": err,
                "StackTrace": tb,
            }
            attachment.add_block(Block.section(params))
            attachment.add_block(Block.divider())
            logger.debug(f"Posting Cluster Error Message for {cluster_id}: {params}")
            self._slack.post_message(attachments=attachment.get_attachment())
        except Exception as err:
            logger.exception(f"Error Posting Cluster Error Message for {cluster_id}: {err}")
            self.error(f"Failed to send cluster error slack alert for {cluster_id}:{cluster_name} - {err}")

    def run_job_error(self, err, tb):
        try:
            self._slack.new_conversation()
            attachment = Attachment(msg_type=AttachmentType.ERROR)
            attachment.add_block(Block.header(f"RUN JOB ERROR ENV:{self._env}"))
            attachment.add_block(Block.divider())
            params = {"Message": err, "StackTrace": tb}
            attachment.add_block(Block.section(params))
            attachment.add_block(Block.divider())
            logger.debug(f"Posting Run Job Error Message - {params}")
            self._slack.post_message(attachments=attachment.get_attachment())
        except Exception as err:
            logger.error(f"Run Job Error Slack Alert - {err}")
            self.error(f"Failed to send run job error slack alert - {err}")

    def cluster_dead_to_inactive(self, cluster_id: str, cluster_name: str):
        try:
            self._slack.new_conversation()
            attachment = Attachment(msg_type=AttachmentType.INFO)
            attachment.add_block(Block.header(f"Dead Cluster Marked Inactive: {self._env}"))
            attachment.add_block(Block.divider())
            params = {"Cluster ID": cluster_id, "Cluster Name": cluster_name}
            attachment.add_block(Block.section(params))
            attachment.add_block(Block.divider())
            logger.debug(f"Posting Dead Cluster Marked Inactive Message for {cluster_id}: {params}")
            self._slack.post_message(attachments=attachment.get_attachment())
        except Exception as err:
            logger.exception(f"Error Posting Dead Cluster Marked Inactive Message for {cluster_id}: {err}")
            self.error(
                f"Failed to send dead cluster marked inactive slack alert for {cluster_id}:{cluster_name} - {err}"
            )

    def custom_metrics_error(self, metric_name: str, err, tb):
        try:
            self._slack.new_conversation()
            logger.debug(f"Posting Custom Metrics Increment Error Message: {err}")
            attachment = Attachment(msg_type=AttachmentType.ERROR)
            attachment.add_block(Block.header(f"CUSTOM METRICS INCREMENT ERROR ENV: {self._env}"))
            attachment.add_block(Block.divider())
            params = {"Metric Name": metric_name, "Message": err, "StackTrace": tb}
            attachment.add_block(Block.section(params))
            attachment.add_block(Block.divider())
            self._slack.post_message(attachments=attachment.get_attachment())
        except Exception as err:
            logger.exception(f"Error Posting Custom Metric Increment Error Message - {err}")
            self.error(f"Failed to send custom metric increment error slack alert - {err}")
