:warning: This is a design document, and will be replaced by a self-documenting page served by the REST service.

The following endpoints have been implemented:

|   | URL | Description |
|---|-----|-------------|
| GET |  [/v0/definitions/{definitionType}](#list-definitions) | List definitions |
| GET |  [/v0/definitions/components](#list-component-definitions) | List component definitions |
| GET |  [/v0/properties/{definitionName}?formName=MAIN](#get-properties) | Get properties |
| POST |  [/v0/properties/serialize](#serialize-to-persistable) | convert uispecs properties into persistable json |
| POST |  [/v0/properties/uispecs?formName=MAIN](#get-properties-from-existing-data) | Get properties from existing data |
| GET |  [/v0/properties/{definitionName}/icon/{type}](#get-properties-icon) | Get properties icon |
| GET |  [/v0/properties/{definitionName}/connectors](#get-properties-connectors) | Get properties connectors |
| POST |  [/v0/properties/trigger/{trigger}/{propName}?formName=MAIN](#trigger-on-a-single-property) | Trigger on a single property |
| POST |  [/v0/properties/validate](#validate-component-properties) | Validate properties |
| POST |  [/v0/properties/dataset?formName=MAIN](#get-dataset-properties) | Get dataset properties |
| POST |  [/v0/runtimes/check](#check-datastore-connection) | Check datastore connection |
| POST |  [/v0/runtimes/schema](#get-dataset-schema) | Get dataset schema |
| POST |  [/v0/runtimes/data?limit=100](#get-dataset-data) | Get dataset data |
| PUT |  [/v0/runtimes/data](#write-dataset-data) | write dataset data |


:warning: _Definition names must be unique_
# whats new in v0 ;)
1. all routes are prefixed with v0.
2. a New route (/properties/serialize) has been added to be called before persisting any Properties. This will provide a way to encrypt sensitive data and offer a migration path when deserialized.
3. All the POST routes taking a definition name as parameter have had this parameter removed to avoid duplication. 
4. Almost all POST routes taking [ui-specs](#ui-spec-properties-format) also take [JsonIo](#json-io-properties-format) properties.
5. ui specs and json-io input and output payload have specific content types that you must use when querying the service see [here](#ui-spec-properties-format) and below.
6. some routes have changed their url for more clarity
   * `/properties/{definitionName}?formName=MAIN` became `v0/properties/uispecs?formName=MAIN`
   * `/properties/{definitionName}/{trigger}/{propName}?formName=MAIN]` became `/v0/properties/trigger/{trigger}/{propName}?formName=MAIN`
   * `/runtimes/{datasetDefinitionName}` became `/v0/runtimes/check` 


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

Returns the [ui specs](#ui-spec-format)

# Serialize to persistable

## Signature

```
POST /properties/serialize
```

This converts ui-spec properties into a json form to be persisted that can be migrated and encrypted 

Parameters:

- `request body` : the form properties with its dependencies in the form of [ui-spec properties](#ui-spec-properties-format)

Returns the [json io](#json-io-properties-format)


# Get properties from existing data

## Signature

```
POST /properties/uispec?formName=MAIN
```

Parameters:

- `formName` : Optional name of the wanted form. Accepted values are `Main`, `CitizenUser`. If not specified, the main form will be requested.
- `request body` : the form properties with its dependencies in the form of [ui-spec properties](#ui-spec-properties-format) or [jsonio properties](#json-io-properties-format)


Returns the [ui specs](#ui-spec-format)

# Get properties icon

In progress...

# Get properties connectors

In progress...

# Trigger on a single property

## Signature

```
POST /properties/trigger/{trigger}/{propName}?formName=XXX
```

Parameters:

- `trigger` : can be one of [validate, beforeActivate, beforeRender, after]
- `propName` : the property name
- `request body` : the form properties with its dependencies in the form of [ui-spec properties](#ui-spec-properties-format)
- `formName` : Optional name of the wanted form. Accepted values are `Main`, `CitizenUser`. If not specified, the main form will be requested.

Returns the [ui specs](#ui-spec-format)

# Validate component properties

## Signature

```
POST /properties/validate
```

Parameters:

- `request body` : the form properties with its dependencies in the form of [ui-spec properties](#ui-spec-properties-format) or [jsonio properties](#json-io-properties-format)


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

# Get dataset properties

## Signature

```
POST /properties/dataset?formName=XXX
```

Parameters:

- `request body` : the data store properties with its dependencies in the form of [ui-spec properties](#ui-spec-properties-format) or [jsonio properties](#json-io-properties-format)
- `formName` : Optional name of the wanted form. Accepted values are `Main`, `CitizenUser`. If not specified, the main form will be requested.

Returns:

- the dataset [ui specs](#ui-spec-format)

# Check datastore connection

## Signature

```
POST /runtimes/check
```

Parameters:

- `request body` : the form properties with its dependencies in the form of [ui-spec properties](#ui-spec-properties-format) or [jsonio properties](#json-io-properties-format)

Returns:

- all properties validated : HTTP 200 OK with the details of checks results
- error with properties : HTTP 400 with the details of checks results

The response is the same as [Validate component properties](Validate-component-properties)



# Get dataset schema

## Signature

```
POST /runtimes/schema
```

_TODO Marc/Geoffroy: We can replace this endpoint with custom buttons on the UI form for a Dataset (leaving the implementation to the component developer). Should we?_

Parameters:

- `datasetDefinitionName` the dataset definition name
- `request body` : the data set properties with its dependencies in the form of [ui-spec properties](#ui-spec-properties-format) or [jsonio properties](#json-io-properties-format)

Returns:

- Avro Schema

# Get dataset data

## Signature

```
POST /runtimes/data?from=1000&limit=5000
```

Parameters:

- `request body` : the data set properties with its dependencies in the form of [ui-spec properties](#ui-spec-properties-format) or [jsonio properties](#json-io-properties-format)
- `from` (optional) where to start in the dataset
- `limit` (optional) how many rows should be returned
- `content-type header` to specify the response type:

  - `application/json` for a json response,
  - `avro/binary` for avro response

Returns:

- either json or avro response

# Write dataset data

## Signature

```
PUT /runtimes/data
```

Parameters:

- `request body` : see [org.talend.components.service.rest.impl.DatasetWritePayload](../../src/main/java/org/talend/components/service/rest/impl/DatasetWritePayload.java)




# UI-spec Properties format

When getting uispec  from TComp API, the structure is always in the form:

```javascript
{
    "properties": {...},
    "jsonSchema":{...},
    "uiSchema": {...}
    "dependencies":[
        {properties}
    ]
}
```
The **dependencies**, **jsonSchema**, **uiSchema** attributes are optional.

ui-spec properties payload is associated with the following content header :`application/uispec+json;charset=UTF-8";`

# Json-IO Properties format

When posting jsonio properties to TComp API, the structure is always in the form:

```javascript
{
        "properties":{...json-io payload...},
        "dependencies":[
            {...json-io payload...},
            {}
        ]
}
```
The **dependencies** attribute is optional.

jsonio properties payload are associated with the following content header :`application/jsonio+json;charset=UTF-8";`


**Note**, Avro _schema_ value should be represented as a JSON string value with escaped double quotes.
E.g.

```
"schema":"{\"type\":\"record\",\"name\":\"empty\",\"fields\":[]}",
```
