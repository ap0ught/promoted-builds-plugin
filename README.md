
Promoted Builds Plugin
======================

[![Join the chat at https://gitter.im/jenkinsci/promoted-builds-plugin](https://badges.gitter.im/jenkinsci/promoted-builds-plugin.svg)](https://gitter.im/jenkinsci/promoted-builds-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/promoted-builds.svg)](https://plugins.jenkins.io/promoted-builds)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/promoted-builds-plugin.svg?label=changelog)](https://github.com/jenkinsci/promoted-builds-plugin/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/promoted-builds.svg?color=blue)](https://plugins.jenkins.io/promoted-builds)

This plugin allows you to distinguish good builds from bad builds by
introducing the notion of 'promotion'.Put simply, a promoted build is a
successful build that passed additional criteria (such as more comprehensive
tests that are set up as downstream jobs.) The typical situation in which you
use promotion is where you have multiple 'test' jobs hooked up as downstream
jobs of a 'build' job. You'll then configure the build job so that the build
gets promoted when all the test jobs passed successfully. This allows you to
keep the build job run fast (so that developers get faster feedback when a  
build fails), and you can still distinguish builds that are good from builds
that compiled but had runtime problems.

Another variation of this usage is to manually promote builds (based on
instinct or something else that runs outside Jenkins.) Promoted builds will
get a star in the build history view, and it can be then picked up by other
teams, deployed to the staging area, etc., as those builds have passed
additional quality criteria. In more complicated scenarios, one can set up
multiple levels of promotions. This fits nicely in an environment where there
are multiple stages of testings (for example, QA testing, acceptance testing,
staging, and production.)

<a name="PromotedBuildsPlugin-PromotionAction"></a>
# Promotion Action

When a build is promoted, you can have Jenkins perform some actions (such as
running a shell script, triggering other jobs, etc. — or in Jenkins lingo, you
can run build steps.) This is useful for example to copy the promoted build to
somewhere else, deploy it to your QA server. You can also define it as a
separate job and then have the promotion action trigger that job.

> **Do not rely on files in the workspace**
The promotion action uses the workspace of the job as the current directory
(and as such the execution of the promotion action is mutually exclusive from
any on-going builds of the job.) But by the time promotion runs, this
workspace can contain files from builds that are totally unrelated from the
build being promoted.

To access the artifacts, use the [Copy Artifact Plugin][1] and choose
the permalink.


<a name="PromotedBuildsPlugin-Usage"></a>
# Usage

To use this plugin, look for the "Promote builds when..." checkbox, on the
Job-configuration page. Define one or a series of promotion processes for
the job.

How might you use promoted builds in your environment? Here are a few
use cases.

Artifact storage -- you may not want to push an artifact to your main artifact
repository on each build. With build promotions, you can push only when an
artifact meets certain criteria. For example, you might want to push it only
after an integration test is run.

Manual Promotions - You can choose a group of people who can run a promotion
manually. This gives a way of having a "sign off" within the build system. For
example, a developer might validate a build and approve it for QA testing only
when a work product is completed entirely. Then another promotion can be added
for the QA hand off to production.

Aggregation of artifacts - If you have a software release that consists of
several not directly related artifacts that are in separate jobs, you might
want to aggregate all the artifacts of a proven quality to a distribution
location. To do this, you can create a new job, adding a "Copy artifacts from
another job" (available through Copy Artifact plugin") for each item you want
to aggregate. To get a certain promotion, select "Use permalink" in the copy
artifact step, then your promoted build should show up in the
list of items to copy.

<a name="PromotedBuildsPlugin-Notes"></a>
# Notes

<a name="PromotedBuildsPlugin-OnDownstreamPromotionConditions"></a>
### On Downstream Promotion Conditions

One of the possible criteria for promoting a build is "When the following
downstream projects build successfully", which basically says if all the
specified jobs successfully built (say build BD of job JD), the build in the
upstream will be promoted (say build BU of job JU.)

This mechanism crucially relies on a "link" between BD and BU, for BU isn't
always the last successful build. We say "BD qualifies BU" if there's this
link, and the qualification is established by one of the following
means:

1.  If BD records fingerprints and one of the fingerprints match some files
that are produced by BU (which is determined from the fingerprint records of
BU), then BD qualifies BU. Intuitively speaking, this indicates that BD uses
artifacts from BU, and thus BD helped verify BU's quality.
2.  If BU triggers BD through a build trigger, then BD qualifies BU. This is
somewhat weak and potentially incorrect, as there's no machine-readable
guarantee that BD actually used anything from BU, but nonetheless this
condition is considered as qualification for those who don't
configure fingerprints.

Note that in the case #1 above, JU and JD doesn't necessarily have to have any
triggering relationship. All it takes is for BD to use some fingerprinted
artifacts from BU, and records those fingerprints in BD. It doesn't matter how
those artifacts are retrieved either — it could be via
[Copy Artifact Plugin][1], it could be through a maven repository, etc. This
also means that you can have a single test job (perhaps parameterized), that
can promote a large number of different upstream jobs.

<a name="PromotedBuildsPlugin-AvailableEnvironmentVariables"></a>
### Available Environment Variables

The following environment variables are added for use in scripts, etc.
These were retrieved from github [here][2].

*   `PROMOTED_URL` - URL of the job being promoted
    *   ex: [http://jenkins/job/job_name_being_promoted/77/](http://jenkins/job/job_name_being_promoted/77/)
*   `PROMOTED_JOB_NAME` - Promoted job name
    *   ex: job_name_being_promoted
*   `PROMOTED_NUMBER` - Build number of the promoted job
    *   ex: 77
*   `PROMOTED_ID` - ID of the build being promoted
    *   ex: 2012-04-12_17-13-03
*   `PROMOTED_USER_NAME` - the user who triggered the promotion
*   `PROMOTED_JOB_FULL_NAME` - the full name of the promoted job

## Declarative Pipeline support

In Declarative Pipeline, you can configure promoted builds using the `properties` directive in your `Jenkinsfile`. The plugin integrates with Jenkins Pipeline by exposing job properties that can be configured at the pipeline level.

**Note:** Promotion processes are configured at the **job level**, not within individual pipeline stages. Once configured, builds can be promoted either automatically (based on conditions) or manually through the Jenkins UI.

### Basic Syntax

```groovy
pipeline {
    agent any
    
    properties([
        pipelineTriggers([]),
        [$class: 'JobPropertyImpl', 
            activeProcessNames: ['Development', 'Production'] as Set
        ]
    ])
    
    stages {
        stage('Build') {
            steps {
                echo 'Building...'
            }
        }
    }
}
```

### Configuring Promotions via Jenkins UI

While the pipeline defines which promotion processes are active, the actual promotion process configuration (conditions, actions, etc.) must be configured through the Jenkins UI:

1. Open your Pipeline job configuration page
2. Look for the "Promote builds when..." section
3. Add and configure promotion processes with names matching those in your `activeProcessNames` list
4. Define promotion conditions (manual, downstream success, etc.)
5. Define promotion actions (shell scripts, downstream jobs, etc.)

### Using Promoted Build Variables

When a promotion runs, the following environment variables are available in the promotion process actions:

*   `PROMOTED_URL` - URL of the job being promoted
*   `PROMOTED_JOB_NAME` - Promoted job name
*   `PROMOTED_NUMBER` - Build number of the promoted job
*   `PROMOTED_ID` - ID of the build being promoted
*   `PROMOTED_USER_NAME` - The user who triggered the promotion (for manual promotions)
*   `PROMOTED_JOB_FULL_NAME` - The full name of the promoted job

### Example: Manual Promotion

```groovy
// Jenkinsfile
pipeline {
    agent any
    
    properties([
        [$class: 'JobPropertyImpl', 
            activeProcessNames: ['QA-Approved', 'Production-Ready'] as Set
        ]
    ])
    
    stages {
        stage('Build') {
            steps {
                echo 'Building application...'
                // Your build steps
            }
        }
        stage('Test') {
            steps {
                echo 'Running tests...'
                // Your test steps
            }
        }
    }
    
    post {
        success {
            echo 'Build successful. Ready for promotion.'
        }
    }
}
```

See [example-declarative-pipeline.groovy](src/test/resources/example-declarative-pipeline.groovy) for a complete example.

After the pipeline runs successfully:
1. Navigate to the build in Jenkins UI
2. Click "Promote" if a manual promotion condition is configured
3. Select the promotion level (e.g., "QA-Approved")
4. The promotion process will execute its configured actions

### Limitations

- Promotion process configuration (conditions, icons, actions) cannot be fully defined in the Jenkinsfile itself; it must be configured through the Jenkins UI
- For complete programmatic configuration, consider using [Job DSL](#job-dsl-support) instead
- The `properties` directive only specifies which promotion processes are active

### Accessing Promoted Builds from Pipeline

You can use the promoted build parameter to select a specific promoted build in downstream jobs:

```groovy
pipeline {
    agent any
    
    parameters {
        promotedBuild(
            name: 'PROMOTED_BUILD',
            project: 'upstream-job',
            promotion: 'Production-Ready',
            description: 'Select a production-ready build to deploy'
        )
    }
    
    stages {
        stage('Deploy') {
            steps {
                echo "Deploying build from: ${params.PROMOTED_BUILD}"
                // Your deployment steps using the promoted build
            }
        }
    }
}
```

See [example-promoted-build-parameter.groovy](src/test/resources/example-promoted-build-parameter.groovy) for a complete example.

## Job DSL support

```groovy  
freeStyleJob(String jobname) {
  properties{
    promotions {
      promotion {
        name(String promotionName)
        icon(String iconName)
        conditions {
          selfPromotion(boolean evenIfUnstable = true)
          parameterizedSelfPromotion(boolean evenIfUnstable = true, String parameterName, String parameterValue)
          releaseBuild()
          downstream(boolean evenIfUnstable = true, String jobs)
          upstream(String promotionNames)
          manual(String user){
            parameters{
              textParam(String parameterName, String defaultValue, String description)
          }
        }
        wrappers {
          /* build wrappers, e.g. credentialsBinding */
        }
        actions {
          shell(String command)
        }
      }
    }
  }
}
```

See [WrapperContext](https://jenkinsci.github.io/job-dsl-plugin/#path/job-wrappers) and [StepContext](https://jenkinsci.github.io/job-dsl-plugin/#path/job-steps) in the API Viewer for full documentation about the possible wrappers and actions.

### Example

```groovy
freeStyleJob('test-job') {
  properties{
    promotions {
      promotion {
        name('Development')
        conditions {
          manual('testuser')
        }
        wrappers {
          timestamps()
        }
        actions {
          shell('echo hello;')
        }
      }
    }
  }
}
```

[1]: https://wiki.jenkins-ci.org/display/JENKINS/Copy+Artifact+Plugin
[2]: https://github.com/jenkinsci/promoted-builds-plugin/blob/master/src/main/java/hudson/plugins/promoted_builds/Promotion.java

## Undergoing project based on this Plugin

* `Artifact Promotion Plugin For Jenkins Pipeline` - Under GSoC 2019.
   Join with the Project through the following Chat Link:
    * [promoted-builds-plugin](https://gitter.im/jenkinsci/promoted-builds-plugin)
