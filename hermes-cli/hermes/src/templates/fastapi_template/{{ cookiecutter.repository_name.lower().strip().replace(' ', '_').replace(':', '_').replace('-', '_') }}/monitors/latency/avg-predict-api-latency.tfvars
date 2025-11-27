roster              = "ml-platform"
serve_name          = "{{ cookiecutter.repository_name.lower().strip().replace(' ', '_').replace(':', '_').replace('-', '_') }}"
slack_channel       = "@slack-testing"
message             = "High latency for predict API of ml-serve application"
# critical threshold is in seconds
critical_threshold  = 0.010
runbook             = ""
