# example-app

This repository is the source of truth for the local Jenkins demo job.

Job config:
- job name: `promoted-semo-scm-pipeline`
- SCM: `file:///var/jenkins_home/scm/example-app.git`
- branch: `main`
- build script: `build.sh`
- promotion scripts: `promotions/prod.sh`, `promotions/np.sh`, `promotions/sbx.sh`

Build instructions:
- run `bash build.sh`
- it writes `build.txt`

Promotions:
- `prod` -> pretend prod location
- `np` -> pretend np location
- `sbx` -> pretend sbx location

The Jenkins job reads `jenkins/job-config.json` so the job config and promotion setup live in this repo.
