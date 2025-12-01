# Darwin Compute Script

## Compute Script

Status Poller, Auto Termination and Jupyter Pods Management Scripts for Darwin Compute.

Status Poller also includes cluster timeout job.

* [Scheduler Documentation](https://dream11.atlassian.net/wiki/spaces/DSSPL/pages/3022880861/Darwin+scheduling-eligible+Jobs+Refinement)
* [Status Poller Documentation](https://dream11.atlassian.net/wiki/spaces/DSSPL/pages/3250225331/Status+Poller+V2)
* [Auto Termination Documentation](https://dream11.atlassian.net/wiki/spaces/DSSPL/pages/2883289437/Auto+Termination+of+Darwin+Clusters)
* [Cluster Timeout Job Documentation](https://dream11.atlassian.net/wiki/spaces/DSSPL/pages/3485499395/Logic+for+stopping+cluster+after+creation+timeout+threshold+is+reached)

## Darwin Compute CRON Jobs

### Environment Variables

| Environment Variable         | Description                   |
|------------------------------|-------------------------------|
| ENV                          | Environment (stag, uat, prod) |
| TEAM_SUFFIX                  | Odin Team Suffix              |
| VPC_SUFFIX                   | Odin VPC Suffix               |
| VAULT_SERVICE_SLACK_TOKEN    | Slack Token for Alerts        |
| VAULT_SERVICE_SLACK_USERNAME | Slack Username for Alerts     |

### K8S Cluster Reconciliation

K8S reconciliation job which directly pulls clusters from k8s and check if it is available in Darwin DB or not.
If not available, it will throw a slack alert in _**darwin-cost-alerts**_ channel.

### Long Running Clusters Monitoring

Long Running Clusters Monitoring job checks which clusters are running for more than 24 hours
and throws a slack alert in _**darwin-cost-alerts**_ channel.
