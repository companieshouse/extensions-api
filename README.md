# Extensions API


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
