roster              = "ml-platform"
serve_name          = "{{ cookiecutter.repository_name.lower().strip().replace(' ', '_').replace(':', '_').replace('-', '_') }}"
slack_channel       = "@slack-testing"
message             = "High alb error rate for ml-serve application"
critical_threshold  = 5
runbook             = ""
