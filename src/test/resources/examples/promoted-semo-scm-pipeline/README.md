# promoted-semo-scm-pipeline

This directory documents the SCM-backed demo for the `promoted-semo-scm-pipeline` job.

Job config:
- job name: `promoted-semo-scm-pipeline`
- SCM: `https://github.com/ap0ught/promoted-builds-plugin`
- branch: `master`
- build script: `src/test/resources/examples/promoted-semo-scm-pipeline/build.sh`
- promotion scripts: `promotions/prod.sh`, `promotions/np.sh`, `promotions/sbx.sh`

Build instructions:
- check out the plugin repo
- run `bash src/test/resources/examples/promoted-semo-scm-pipeline/build.sh`
- the build script runs the focused Maven tests for this example

Promotions:
- `prod` -> pretend prod location
- `np` -> pretend np location
- `sbx` -> pretend sbx location

The Jenkins job reads `jenkins/job-config.json` so the job config and promotion setup live in this repo.

This demo is intended to be deleted and recreated from the SCM contents.
