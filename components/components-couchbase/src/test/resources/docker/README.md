# Couchbase Docker Server

This resource folder contains documentation for building and using Couchbase Docker Image for integration tests.

## Official Couchbase Docker Images:
We're using [Couchbase's Community Edition version](https://hub.docker.com/r/couchbase/server/tags/).

Couchbase releases their servers and SDK's regularly. We have to re-build our image to test latest Couchbase SDK.

### Building Image ###
Your new built image must apply Talend Docker Image [policy](https://github.com/Talend/policies/blob/master/official/DockerPolicy.md).

#### Dockerfile

**FROM** - point to official community couchbase docker image.

**LABEL** - information about docker image.

**configure.sh** - configuration script that performs initialization of environment such as:
* starting server;
* creating cluster;
* creating bucket;
* creating user and assign access for bucket use.

To build updated docker image use this command:

```
docker build -t "{name:tag}" .
```
`{tagName} - must be properly named, as an example use following naming - talend/components-integration-test-couchbase-server:{couchbase-server-version}-TIMESTAMP`

### Running Built Image ###

To start image, execute below command:

```
docker run -t --name {name} -p 8091-8096:8091-8096 -p 11210-11211:11210-11211 {name:tag}
```
Now you can access your server at **localhost:8091**.

`Note: it's possible that your docker may use another host address, in this case replace localhost with corresponding address.`

`Note2: don't forget to stop or/and remove unused containers.`

### Pushing built image to Talend Registry ###

Please refer to [Wiki page](https://wiki.talend.com/display/rd/Push+new+Docker+image+to+the+registry).

##### Useful links #####
https://docs.couchbase.com/server/5.5/getting-started/do-a-quick-install.html