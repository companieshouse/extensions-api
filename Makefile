artifact_name       := extensions-api

.PHONY: all
all: build

.PHONY: githooks
githooks:
	git config core.hooksPath .githooks

.PHONY: clean
clean:
	mvn clean
	rm -f ./$(artifact_name).jar
	rm -f ./$(artifact_name)-*.zip
	rm -rf ./build-*
	rm -f ./build.log

.PHONY: build
build:
	mvn compile

.PHONY: test
test: clean
	mvn verify

.PHONY: test-unit
test-unit: clean
	mvn test -Dgroups="uk.gov.companieshouse.extensions.api.groups.Unit"

.PHONY: test-integration
test-integration: clean
	mvn verify -Dgroups="uk.gov.companieshouse.extensions.api.groups.Integration"

.PHONY: test-concourse-integration
test-concourse-integration: clean
	mvn verify -Dgroups="uk.gov.companieshouse.extensions.api.groups.ConcourseIntegration"

.PHONY: test-contract-consumer
test-contract-consumer: clean
	mvn verify -Dgroups="uk.gov.companieshouse.extensions.api.groups.ContractConsumer"

.PHONY: test-contract-provider
test-contract-provider: clean
	mvn verify -Dgroups="uk.gov.companieshouse.extensions.api.groups.ContractProvider"

.PHONY: dev
dev: clean
	mvn package -DskipTests=true
	cp target/$(artifact_name)-unversioned.jar $(artifact_name).jar

.PHONY: package
package:
ifndef version
	$(error No version given. Aborting)
endif
	$(info Packaging version: $(version))
	mvn versions:set -DnewVersion=$(version) -DgenerateBackupPoms=false
	mvn package -DskipTests=true
	$(eval tmpdir:=$(shell mktemp -d build-XXXXXXXXXX))
	cp ./start.sh $(tmpdir)
	cp ./routes.yaml $(tmpdir)
	cp ./target/$(artifact_name)-$(version).jar $(tmpdir)/$(artifact_name).jar
	cd $(tmpdir); zip -r ../$(artifact_name)-$(version).zip *
	rm -rf $(tmpdir)

.PHONY: dist
dist: clean build package

.PHONY: marathon
marathon:
	sed 's/{{release}}/$(VERSION)/g;s/{{environment}}/$(ENVIRONMENT)/g' marathon.json > marathon-deploy.json

.PHONY: sonar
sonar:
	mvn sonar:sonar
