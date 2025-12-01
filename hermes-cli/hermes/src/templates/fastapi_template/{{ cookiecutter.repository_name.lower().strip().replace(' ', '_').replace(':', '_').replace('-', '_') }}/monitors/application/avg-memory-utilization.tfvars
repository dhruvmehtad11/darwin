roster              = "ml-platform"
serve_name          = "{{ cookiecutter.repository_name.lower().strip().replace(' ', '_').replace(':', '_').replace('-', '_') }}"
slack_channel       = "@slack-testing"
message             = "High memory utilization for ml-serve application"
critical_threshold  = 70
runbook             = ""
