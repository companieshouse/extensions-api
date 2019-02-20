artifact_name       := extensions-api
final_name          := extensions-api
commit              := $(shell git rev-parse --short HEAD)
tag                 := $(shell git tag -l 'v*-rc*' --points-at HEAD)
version             := $(shell if [[ -n "$(tag)" ]]; then echo $(tag) | sed 's/^v//'; else echo $(commit); fi)

.PHONY: all
all: dist

.PHONY: clean
clean:
	mvn clean
	rm -f ./$(final_name).jar
	rm -f ./$(final_name)-*.zip
	rm -rf ./build-*

.PHONY: build
build:
	mvn package -DskipTests=true
	cp ./target/$(artifact_name).jar ./$(final_name).jar

.PHONY: test
test: test-unit

.PHONY: test-unit
test-unit: clean
	mvn test

.PHONY: package
package:
	@test -s ./$(final_name).jar || { echo "ERROR: Service JAR not found"; exit 1; }
	$(eval tmpdir:=$(shell mktemp -d build-XXXXXXXXXX))
	cp ./start.sh $(tmpdir)
	cp ./$(artifact_name).jar $(tmpdir)/$(final_name).jar
	cd $(tmpdir); zip -r ../$(final_name)-$(version).zip *
	rm -rf $(tmpdir)

.PHONY: dist
dist: clean build package

.PHONY: sonar
sonar:
	mvn sonar:sonar
