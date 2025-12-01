# Helm Chart for Spark History Server

[Spark History Server](https://spark.apache.org/docs/latest/monitoring.html#viewing-after-the-fact) provides a web UI
for completed and running Spark applications. The supported storage backends are Google Cloud Storage (GCS),
PersistentVolumeClaim (PVC) and Amazon S3. This chart is adapted from
the [chart](https://github.com/SnappyDataInc/spark-on-k8s/tree/master/charts/spark-hs) from SnappyData Inc.

#### Prerequisites

* Secret (Only if using GCS or S3 without IAM based authentication)

  If using GCS as storage, follow the preparatory steps below:

  Note: Use the `gcs.enableIAM` flag if running on GKE with Workload Identity or if the node's service account already
  has the required permissions. Otherwise, follow the steps below.

  Set up `gsutil` and `gcloud` on your local laptop and associate them with your Google Cloud Platform (GCP) project,
  create a bucket, create an IAM service account `sparkonk8s`, generate a JSON key file `sparkonk8s.json`, to
  grant `sparkonk8s` admin permission to bucket `gs://spark-history-server`.

  ```bash
  $ gsutil mb -c nearline gs://spark-history-server
  $ export ACCOUNT_NAME=sparkonk8s
  $ export GCP_PROJECT_ID=project-id
  $ gcloud iam service-accounts create ${ACCOUNT_NAME} --display-name "${ACCOUNT_NAME}"
  $ gcloud iam service-accounts keys create "${ACCOUNT_NAME}.json" --iam-account "${ACCOUNT_NAME}@${GCP_PROJECT_ID}.iam.gserviceaccount.com"
  $ gcloud projects add-iam-policy-binding ${GCP_PROJECT_ID} --member "serviceAccount:${ACCOUNT_NAME}@${GCP_PROJECT_ID}.iam.gserviceaccount.com" --role roles/storage.admin
  $ gsutil iam ch serviceAccount:${ACCOUNT_NAME}@${GCP_PROJECT_ID}.iam.gserviceaccount.com:objectAdmin gs://spark-history-server
  ```

  Then create a secret using the JSON key file:

  ```bash
  $ kubectl -n <history-server-namespace> create secret generic history-secrets --from-file=sparkonk8s.json
  ```

  Then install the chart to enable the history server pod to read from the GCS bucket.

  Similarly, if using S3 as storage, follow the preparatory steps below:
  ```bash
  $ aws s3 mb s3://your-spark-event-log-directory # default bucket is s3://spark-hs/
  $ aws iam list-access-keys --user-name your-user-name --output text | awk '{print $2}' >> aws-access-key
  $ echo "your-aws-secret-key" >> aws-secret-key
  ```

  Then create a secret:
  ```bash
  $ kubectl create secret generic aws-secrets --from-file=aws-access-key --from-file=aws-secret-key
  ```

* PVC (Only if using PVC)

  If you are using a PVC as the backing storage for Spark history events, then you'll need to create the PVC before
  installing the chart.

  The chart by default creates a PVC backed by an NFS volume. The NFS volume and server are installed as a child chart
  adapted from the [docum

#### Discussions of Storage Options

Because a PVC is a namespaced Kubernetes resource, the fact that it is created in the same namespace where the chart is
installed means that Spark jobs that would like to log events to the PVC also need to be deployed in the same namespace.
If this is an issue for you (e.g. you have another dedicated namespace for your Spark jobs), then use GCS instead.

In the case of GCS, a secret is also namespaced, but it's only used to enable the history server to read the remote GCS
bucket. So Spark jobs logging to GCS can run in any namespace. Unless jobs are running in the same namespace as the
history server, you'll need to create the secret separately in the job namespace before running the job.

#### Installing the Chart

To install the chart with the sample PVC setup:

```bash
$ helm install {RELEASE_NAME} spark-history-server/. -n ray
```

#### Configurations

The following tables lists the configurable parameters of the Spark History Sever chart and their default values.

The default image is built using the [Dockerfile](../../images/spark-history-server-3.3.0/Dockerfile)

| Parameter             | Description                                                                                           | Default                                             |
|-----------------------|-------------------------------------------------------------------------------------------------------|-----------------------------------------------------|
| image.repository      | The Docker image used to start the history server daemon                                              | 000375658054.dkr.ecr.us-east-1.amazonaws.com/darwin |
| image.tag             | The tag of the image                                                                                  | spark-history-server-3.3.0                          |
| service.type          | The type of history server service that exposes the UI                                                | ClusterIP                                           |
| service.port.number   | The port on which the service UI can be accessed.                                                     | 18080                                               |
| service.nodePort      | The NodePort on which the service UI can be accessed.                                                 | nil                                                 |
| service.annotations   | annotations for the service                                                                           | {}                                                  |
| pvc.enablePVC         | Whether to use PVC storage                                                                            | true                                                |
| pvc.existingClaimName | The pre-created PVC name                                                                              | nfs-pvc                                             |
| pvc.eventsDir         | The log directory when PVC is used                                                                    | /                                                   |
| gcs.enableGCS         | Whether to use GCS storage                                                                            | false                                               |
| gcs.secret            | Pre-mounted secret name for GCS connection                                                            | history-secrets                                     |
| gcs.key               | The JSON key file name                                                                                | sparkonk8s.json                                     |
| gcs.logDirectory      | The GCS log directory that starts with "gs://"                                                        | gs://spark-hs/                                      |
| s3.enableS3           | Whether to use S3 storage                                                                             | false                                               |
| s3.enableIAM          | Whether to use IAM based authentication or fall back to using AWS access key ID and secret access key | true                                                |
| s3.secret             | Pre-mounted secret name for S3 connection. Omit if using IAM based authentication                     | aws-secrets                                         |
| s3.accessKeyName      | The file name that contains the AWS access key ID. Omit if using IAM based authentication             | aws-access-key                                      |
| s3.secretKeyName      | The file name that contains the AWS secret access key. Omit if using IAM based authentication         | aws-secret-key                                      |
| s3.logDirectory       | The S3 log directory that starts with "s3a://"                                                        | s3a://spark-hs/                                     |
| s3.endpoint           | The s3 service endpoint                                                                               | default                                             |
| resources             | Resource requests and limits                                                                          | {}                                                  |

Note that only when `pvc.enablePVC` is set to `true`, the following settings take effect:

* pvc.existingClaimName
* pvc.eventsDir

By default, an NFS server and PVC are set up. Optionally they can be disabled by setting `nfs.enableExampleNFS` to
false.

Similarly, only when `gcs.enableGCS` is `true`, the following settings take effect:

* gcs.secret
* gcs.key
* gcs.logDirectory

Similarly, only when `s3.enableS3` is `true`, the following settings take effect:

* s3.enableIAM
* s3.secret
* s3.accessKeyName
* s3.secretKeyName
* s3.logDirectory
* s3.endpoint

And only when `pvc.enablePVC` and `gcs.enableGCS` are both `false`, is HDFS used, in which case the settings below are
in effect:

#### Viewing the UI

To view the UI, open `darwin.dream11.com/{kube_cluster_key}/{release_name}/` in your browser. The `kube_cluster_key` is
the key of the Kubernetes cluster where the history server is installed, and the `release_name` is the name of the Helm
release.
