# Extensions API


## Integration Tests
The integration tests currently run off of the Waldorf environment.
Dummy data is inserted into this database from the concourse pipeline.
The extension_requests collection is cleared down as the first task and a single
extension request is inserted. This contains the basic data of a full request to assert against.
