#!/bin/bash
# Background Couchbase init
/entrypoint.sh couchbase-server &

# Check couchbase init
check_is_initialized() {
  curl $COUCHBASE_BASE_HOST:8091 > /dev/null
  echo $?
}

# Wait init. We can't make next steps without correct init.
until [[ $(check_is_initialized) = 0 ]]; do
  sleep 1
done

# Setup Cluster
couchbase-cli cluster-init -c $COUCHBASE_BASE_HOST --cluster-username $COUCHBASE_CLUSTER --cluster-password $COUCHBASE_CLUSTER_PASSWORD \
  --cluster-name $COUCHBASE_CLUSTER --cluster-ramsize $COUCHBASE_CLUSTER_RAM_SIZE --services data --index-storage-setting default

# Create Bucket
couchbase-cli bucket-create -c $COUCHBASE_BASE_HOST --username $COUCHBASE_CLUSTER --password $COUCHBASE_CLUSTER_PASSWORD --bucket-type couchbase --bucket-ramsize $COUCHBASE_BUCKET_RAM_SIZE --bucket $COUCHBASE_BUCKET --enable-flush=1

# Create User
couchbase-cli user-manage -c $COUCHBASE_BASE_HOST -u $COUCHBASE_CLUSTER -p $COUCHBASE_CLUSTER_PASSWORD --set --rbac-username $COUCHBASE_USER --rbac-password $COUCHBASE_USER_PASSWORD --rbac-name "sysadmin" --roles admin --auth-domain local

# Make conatianer runnable until we stop it.
wait
