# Extensions API


## Integration Tests
The integration tests currently run off of the Waldorf environment.
Dummy data is inserted into this database from the concourse pipeline.
Before the Integration test job, the extension_requests collection is cleared down and a single
extension request is inserted. This contains the basic data of a full request to assert against.
