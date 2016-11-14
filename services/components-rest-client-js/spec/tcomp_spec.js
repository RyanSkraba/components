const fetchMock = require('fetch-mock');
const TComp = require('../src/tcomp');

describe('Components', function () {
	const tcomp = TComp();

	function checkArgs(method) {
		it('should throw an error if no argument is passed', function (done) {
			tcomp[method]().catch(function (err) {
				expect(err).toBeDefined();
				done();
			});
		});
	}

	afterEach(function () {
		fetchMock.restore();
	});

	describe('Datastores', function () {
		it('should return a list of datastores', function (done) {
			fetchMock.get('/api/components/names', {
				'@items': ['testComponents'],
			});
			tcomp.Datastores().then(function (d) {
				expect(d).toBeDefined();
				expect(d).toEqual(['testComponents']);
				done();
			}).catch(function (err) {
				expect(err).toBeNull();
				done();
			});
		});

		it('should throw an error', function (done) {
			fetchMock.get('/api/components/names', {});

			tcomp.Datastores().catch(function (err) {
				expect(err).toBeDefined();
				done();
			});
		});
	});

	describe('DatastoreProperties', function () {

		checkArgs('DatastoreProperties');

		it('should throw an error if no argument is passed', function (done) {
			tcomp.DatastoreProperties().catch(function (err) {
				expect(err).toBeDefined();
				done();
			});
		});

		it('should return datastore properties', function (done) {
			const expected = {
				jsonSchema: {},
				properties: {},
				uiSchema: {},
			};
			fetchMock.get('/api/components/properties/test', expected);

			tcomp.DatastoreProperties('test').then(function (properties) {
				expect(properties).toBeDefined();
				expect(properties).toEqual(expected);
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});
	});

	describe('ValidateDatastoreProperties', function () {
		checkArgs('ValidateDatastoreProperties');

		it('should handle successfull validation', function (done) {
			fetchMock.post('/api/components/properties/test/validate', {
				status: 204,
			});

			tcomp.ValidateDatastoreProperties('test', {}).then(function (status) {
				expect(status).toBeUndefined();
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});

		it('should handle validation error', function (done) {
			const expected = {
				jsonSchema: {},
				properties: {},
				uiSchema: {},
			};
			fetchMock.post('/api/components/properties/test/validate', {
				status: 404,
				body: expected,
			});

			tcomp.ValidateDatastoreProperties('test', {}).then(function (resp) {
				expect(resp).toEqual(expected);
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});
	});

	describe('CheckDataStoreConnection', function () {
		checkArgs('CheckDataStoreConnection');

		it('should handle successfull connection', function (done) {
			fetchMock.post('/api/runtimes/datastore/test', {
				status: 204,
			});

			tcomp.CheckDataStoreConnection('test', {}).then(function (status) {
				expect(status).toBeUndefined();
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});
		it('should handle connection error', function (done) {
			const expected = {
				jsonSchema: {},
				properties: {},
				uiSchema: {},
			};
			fetchMock.post('/api/runtimes/datastore/test', {
				status: 404,
				body: expected,
			});

			tcomp.CheckDataStoreConnection('test', {}).then(function (resp) {
				expect(resp).toEqual(expected);
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});
	});

	describe('DatasetProperties', function () {
		checkArgs('DatasetProperties');

		it('should get the dataset properties', function (done) {
			const expected = {
				'datastore-ui-specs': {},
				'dataset-ui-specs': {},
			};
			fetchMock.post('/api/definitions/datastore/test/dataset', expected);
			tcomp.DatasetProperties('test', {}, {}).then(function (resp) {
				expect(resp).toEqual(expected);
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});
	});

	describe('ValidateDatasetProperties', function () {
		checkArgs('ValidateDatasetProperties');

		it('should handle successfull connection', function (done) {
			fetchMock.post('/api/components/properties/test/validate', {
				status: 204,
			});

			tcomp.ValidateDatasetProperties('test', {}, {}).then(function (status) {
				expect(status).toBeUndefined();
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});

		it('should handle connection error', function (done) {
			const expected = {
				'datastore-ui-specs': {},
				'dataset-ui-specs': {},
			};
			fetchMock.post('/api/components/properties/test/validate', {
				status: 404,
				body: expected,
			});

			tcomp.ValidateDatasetProperties('test', {}, {}).then(function (resp) {
				expect(resp).toEqual(expected);
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});
	});

	describe('CheckDatasetConnection', function () {
		checkArgs('CheckDatasetConnection');

		it('should handle successfull connection', function (done) {
			fetchMock.post('/api/runtimes/dataset/test', {
				status: 204,
			});

			tcomp.CheckDatasetConnection('test', {}, {}).then(function (status) {
				expect(status).toBeUndefined();
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});

		it('should handle connection error', function (done) {
			const expected = {
				'datastore-form-specs': {},
				'dataset-form-specs': {},
			};
			fetchMock.post('/api/runtimes/dataset/test', {
				status: 404,
				body: expected,
			});

			tcomp.CheckDatasetConnection('test', {}, {}).then(function (resp) {
				expect(resp).toEqual(expected);
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});
	});

	describe('DatasetSchema', function () {
		checkArgs('DatasetSchema');

		it('should return a schema', function (done) {
			fetchMock.post('/api/definitions/dataset/test/schema', {});
			tcomp.DatasetSchema('test', {}, {}).then(function (resp) {
				expect(resp).toEqual({});
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});
	});

	describe('DatasetContent', function () {
		checkArgs('DatasetContent');

		it('should return the content', function (done) {
			fetchMock.post('/api/runtimes/dataset/test/data', {});
			tcomp.DatasetContent('test', {}, {}).then(function (resp) {
				expect(resp).toEqual({});
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});
	});

	describe('Transformers', function () {
		it('should return the transformers', function (done) {
			fetchMock.get('/api/components/names', {
				'@items': {},
			});
			tcomp.Transformers().then(function (resp) {
				expect(resp).toEqual({});
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});
	});

	describe('ValidateProperties', function () {
		checkArgs('ValidateProperties');

		it('should validate a property', function (done) {
			fetchMock.post('/api/properties/test/validate', {});
			tcomp.ValidateProperties('test', {}).then(function (resp) {
				expect(resp).toEqual({});
				done();
			}).catch(function (err) {
				expect(err).toBe(null);
				done();
			});
		});
	});
});
