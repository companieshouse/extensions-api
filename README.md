# Extensions API

This repository contains the component code used to service requests from the extensions-web application. It is responsible for persisting extensions data in a Mongo DB instance as a user completes the extensions on-line journey as well as handling the file upload process (when supporting documentation is provided). Finally, it calls the extensions-processor-api at the end of the extensions journey, in order to carry out further back-end tasks. 


## Requirements

- [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)
- [MongoDB](https://www.mongodb.com)


## Getting Started


1. Create a `pacts` folder under `extensions-api` and download the following files from the AWS `chips-assets-dev` S3 bucket:

	- extensions-processor-api-chs-company-profile-api.json
	- extensions-processor-api-extensions-api.json
	- extensions-web-extensions-api.json
	- extensions-web-extensions-processor-api.json

	(These resources are required in order for the Pact integration tests to pass)

2. To build the project locally, run the command `make clean build`.

3. To run all the tests, run the command `make test`.

	(Note that some of the integration tests will not pass without additional configuration when run locally but they do when running on Concourse - e.g. FileTransferGatewayIntegrationTest. To ignore these failing tests run the command `make test-unit test-integration test-contract-consumer`. To configure them to run, refer to the section below.)


## Setting up Githooks

Run `make githooks` to configure your local project clone to use the hooks located in the `.githooks` directory.


## Integration Tests

The integration tests currently run off of the Waldorf environment.
Dummy data is inserted into this database from the concourse pipeline.
The first concourse integration-test task will clear down the extension_requests collection and
several dummy documents will be inserted.

If you are running integration tests locally then you must ensure that your chs-mongo db contains
the relevant dummy data.
Run the following command from within the project root folder
```
mongoimport --jsonArray --host chs-mongo:27017 --db extension_requests --collection
extension_requests --file src/test/resources/mongoTestData.json
```
This will import the relevant dummy data to your local mongo instance.
