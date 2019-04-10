# Extensions API pipeline

[This](pipeline.yml) is the pipeline configuration file for extensions api.
There are several jobs in the pipeline:

* unit-testing - performs the unit tests.
* release - calculates the release version and creates a new git release.
* statler - deploys to statler.
* waldorf - deploys to waldorf.

As the project and confidence in the pipeline grows, new jobs will be assigned (contract tests / pre-prod deployment etc).

The pipeline is running out of the sandbox account but is still performing deployments and releases. Deploying pipeline changes requires first logging into either the sandbox or platform account.
_NB. The sandbox account will not be able to do github releases_
```
# SANDBOX
fly -t aws-sandbox-development login -c https://ci.sandbox.aws.chdev.org -n development
```
```
# PLATFORM
fly -t sandbox login -c https://ci.platform.aws.chdev.org -n development
```
_NB. `ci.sandbox` can no longer perform releases in github_
A link will be returned for you to enter into your web browser and complete the login.

The development target already contains the credentials required to successfully run the pipeline, you **DO NOT** need to supply your own credentials.

To deploy a new pipeline after a change:
```
# SANDBOX
fly -t aws-sandbox-development sp -p extensions-api -c pipeline.yml
```
```
# PLATFORM
fly -t sandbox sp -p extensions-api -c pipeline.yml
```
This is assuming you are running the fly command from this directory.

_NB. Changing the pipeline yml and committing the code will not automatically update the pipeline in concourse, you must do this yourself, this is until confidence in the pipeline is at a point where it can be put into the concourse pipeline repository (possibly after the new CHS deployer has been made)_

## Exposing the pipeline
The pipeline can be available for display in the scrum by running the following command to expose it:
```
fly -t aws-sandbox-development expose-pipeline -p extensions-api
```

You can view the pipeline as an anonymous user, but not its logs. Viewing the logs of an exposed pipeline will still require authorization, this will require that you log in using your ldap credentials through the web screen `ci.sandbox.aws.chdev.org`

## CI deployment

This pipeline deploys into Statler and Waldorf by directly using the marathon api instead of the chs-deployer, this allows concourse to automate the deployments rather than us doing it the manual chs-deployer way. A put request is sent to the marathon api using [this](../marathon.json) json file. The chs-deployer is not required for deployment into CI.

The labels within the json file are identical to the [routes.yaml](../routes.yaml), rather than having the deployer construct a marathon.json, we can simply create our own and put it into version control.

## Concourse worker

The concourse workers (for the sandbox account) are deployed into the management vpc and live within the CIDR block `10.50.16.0/21` thus for concourse to call the marathon api this CIDR block must be added to the mesos-master EC2 instance for your environment.

## Out of space errors

Occasionally after a failed task, subsequent task reruns will also fail with an out of space error.
Fixing this requires destroying the pipeline and remaking it.
```
fly -t aws-sandbox-development destroy-pipeline -p extensions-api
```

## Releases
After unit and integration testing comes the draft release. A draft release will be created in github and the packaged zip uploaded. If all jobs after the draft-release pass then a full release is attempted. All jobs between the draft release and release are split into serial groups. This is to ensure that a new commit to master does not pollute a running release job. A Serial group can only run 1 job at a time.
