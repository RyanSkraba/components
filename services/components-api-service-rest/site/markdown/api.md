:warning: This is a design document, and will be replaced by a self-documenting page served by the REST service.

The following endpoints have been implemented:

|   | URL | Description |
|---|-----|-------------|
| GET |  [/definitions/{definitionType}](#list-definitions) | List definitions |
| GET |  [/definitions/components](#list-component-definitions) | List component definitions |
| GET |  [/properties/{definitionName}?formName=MAIN](#get-properties) | Get properties |
| POST |  [/properties/{definitionName}?formName=MAIN](#get-properties-from-existing-data) | Get properties from existing data |
| GET |  [/properties/{definitionName}/icon/{type}](#get-properties-icon) | Get properties icon |
| GET |  [/properties/{definitionName}/connectors](#get-properties-connectors) | Get properties connectors |
| POST |  [/properties/{definitionName}/{trigger}/{propName}?formName=MAIN](#trigger-on-a-single-property) | Trigger on a single property |
| POST |  [/properties/{definitionName}/validate](#validate-component-properties) | Validate component properties |
| POST |  [/runtimes/{datastoreDefinitionName}](#check-datastore-connection) | Check datastore connection |
| POST |  [/properties/{datastoreDefinitionName}/dataset?formName=MAIN](#get-dataset-properties) | Get dataset properties |
| POST |  [/properties/{datasetDefinitionName}](#validate-dataset-properties) | Validate dataset properties |
| POST |  [/runtimes/{datasetDefinitionName}](#check-datastore-connection) | Check dataset connection |
| POST |  [/runtimes/{datasetDefinitionName}/schema](#get-dataset-schema) | Get dataset schema |
| POST |  [/runtimes/{datasetDefinitionName}/data?limit=100](#get-dataset-data) | Get dataset data |


:warning: _Definition names must be unique_

**Q: How do we get the component properties for a given dataset+datastore?**

# List definitions

## Signature

```
GET /definitions/{definitionType}
```

Parameters:

- `definitionType` : accepted values: `datastores`, `components`

Returns: { [ {definition={name, label, iconURL, "datastore", inputCompDefinitionName, outputCompDefinitionName}}, // for datastores {definition={name, label, iconURL, "component", typologies}} // for components ] }

# List component definitions

## Signature

```
GET /definitions/components?typology={typologyType}
```

_RyS:Is typology mandatory? How do we distinguish this from Get component properties._

Parameters:

- `typologyType` : accepted values are `transformer`, `source`, `sink`, `configuration`

Returns

```
{
    [
        {name, label, iconURL, typologies}, ...
    ]
}
```

# Get properties

## Signature

```
GET /properties/{definitionName}?formName=MAIN
```

Parameters:

- `formName` : Optional name of the wanted form. Accepted values are `Main`, `CitizenUser`. If not specified, the main form will be requested.

Returns the ui specs:

```
{
    {jsonSchema: ...},
    {uiSchema: ...},
    {properties: ...}
}
```
# Get properties from existing data

## Signature

```
POST /properties/{definitionName}?formName=MAIN
```

Parameters:

- `formName` : Optional name of the wanted form. Accepted values are `Main`, `CitizenUser`. If not specified, the main form will be requested.
- `request body` : the form properties with its dependencies in the form:

  ```javascript
  {
        "properties":{"@definitionName":"my definition"},
        "dependencies":[
            {"@definitionName":"dependency definition"},
            {}
        ]
    }
  ```

Returns the ui specs:

```
{
    {jsonSchema: ...},
    {uiSchema: ...},
    {properties: ...}
}
```

# Get properties icon

In progress...

# Get properties connectors

In progress...

# Trigger on a single property

**TODO** Seb, do we need all these triggers ?

## Signature

```
POST /properties/{definitionName}/validate/{propName}?formName=XXX
POST /properties/{definitionName}/beforeActivate/{propName}?formName=XXX
POST /properties/{definitionName}/beforeRender/{propName}?formName=XXX
POST /properties/{definitionName}/after/{propName}?formName=XXX
```

Parameters:

- `definitionName` : the definition name
- `propName` : the property name
- `request body` : the form properties with its dependencies in the form:

  ```javascript
  {
        "properties":{"@definitionName":"my definition"},
        "dependencies":[
            {"@definitionName":"dependency definition"},
            {}
        ]
    }
  ```

- `formName` : Optional name of the wanted form. Accepted values are `Main`, `CitizenUser`. If not specified, the main form will be requested.

Returns ui specs

```
{
    {json-schema: ...},
    {ui-schema: ...},
    {form-data (properties): ...}
}
```

# Validate component properties

## Signature

```
POST /properties/{definitionName}/validate
```

Parameters:

- `definitionName` the definition name
- `request body` : the form properties with its dependencies in the form:

  ```javascript
  {
        "properties":{"@definitionName":"my definition"},
        "dependencies":[
            {"@definitionName":"dependency definition"},
            {}
        ]
    }
  ```

Returns:

- all properties validated : HTTP 200 OK with report
- error with properties : HTTP 400 with the check reports:

The result status is contained in the response "status" field and the checks details are listed in "results".

Example of validation error:

```javascript
{
  "status":ERROR,
  "results": [
    {"status": ERROR, "message":"the error message"},
    {"status": ERROR, "message":"another error message"}
  ]
}
```

_TODO Geoffroy / Sébastien : define proper way to raise errors (per fields, per form, how-to raise different levels (warnings / error...))_

_TODO Vincent manage error stacktrace to ease debugging et print understandable messages back to user_

# Check datastore connection

## Signature

```
POST /runtimes/{datastoreDefinitionName}
```

Parameters:

- `datastoreDefinitionName` the data store definition name
- `request body` : the data store properties with its dependencies in the [Properties format](#properties-format)

Returns:

- all properties validated : HTTP 200 OK with the details of checks results
- error with properties : HTTP 400 with the details of checks results

The response is the same as [Validate component properties](Validate-component-properties)

# Get dataset properties

## Signature

```
POST /properties/{datastoreDefinitionName}/dataset?fornName=XXX
```

Parameters:

- `datastoreDefinitionName` : the data store definition name
- `request body` : the data store properties with its dependencies in the [Properties format](#properties-format)
- `formName` : Optional name of the wanted form. Accepted values are `Main`, `CitizenUser`. If not specified, the main form will be requested.

Returns:

- the dataset ui specs

# Validate dataset properties

This is to validate the properties, not to validate the connection to the dataset.

## Signature

```
POST /properties/{datasetDefinitionName}
```

Parameters:

- `datasetDefinitionName` the data set definition name
- `request body` : the data set properties with its dependencies (i.e. data store properties) in the [Properties format](#properties-format)

- all properties validated : HTTP 200 OK

- error with properties : HTTP 400 with the ui specs containing errors

_TODO Ryan/Geoffroy define proper / secure way to handle sensitive form fields (credentials...) ?_

# Check dataset connection

## Signature

```
POST /runtimes/{datasetDefinitionName}
```

Parameters:

- `request body` : the data set properties with its dependencies in the [Properties format](#properties-format)

Returns:

- all properties validated : HTTP 200 OK with the details of checks results
- error with properties : HTTP 400 with the details of checks results

The response is the same as [Validate component properties](Validate-component-properties)

# Get dataset schema

## Signature

```
POST /runtimes/{datasetDefinitionName}/schema
```

_TODO Marc/Geoffroy: We can replace this endpoint with custom buttons on the UI form for a Dataset (leaving the implementation to the component developer). Should we?_

Parameters:

- `datasetDefinitionName` the dataset definition name
- `request body` : the data set properties with its dependencies in the [Properties format](#properties-format)

Returns:

- Avro Schema

# Get dataset data

## Signature

```
POST /runtimes/datasets/{datasetdefinitionName}/data?from=1000&limit=5000
```

Parameters:

- `datasetdefinitionName` the dataset definition name
- `request body` : the data set properties with its dependencies in the form see [Properties format](#properties-format)
- `from` (optional) where to start in the dataset
- `limit` (optional) how many rows should be returned
- `content-type header` to specify the response type:

  - `application/json` for a json response,
  - `avro/binary` for avro response

Returns:

- either json or avro response

# TODO

raise a specific error when we need to migrate properties

# Properties format

When posting properties to TComp API, the structure is always in the form:

```javascript
{
        "properties":{"@definitionName":"my definition"},
        "dependencies":[
            {"@definitionName":"dependency definition"},
            {}
        ]
    }
```

For example for a data set, the properties field contains the data set properties JSon object itself when the dependencies field will contains a singleton list containing the data store properties JSon object.

**Note**, Avro _schema_ value should be represented as a JSON string value with escaped double quotes.
E.g.
```
"schema":"{\"type\":\"record\",\"name\":\"empty\",\"fields\":[]}",
```
