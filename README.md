# Using Batching in Cloud Pub/Sub Java client API.

This example provides guidance on how to use [Pub/Sub's Java client API](https://cloud.google.com/pubsub/docs/reference/libraries) to batch records that are published to a Pub/Sub topic.
Using [BatchingSettings](http://googleapis.github.io/gax-java/1.4.1/apidocs/com/google/api/gax/batching/BatchingSettings.html) correctly allows us to better utilize the available resources (cpu, memory, network bandwidth) on the client machine and to improve throughput.

In addition to BatchingSettings, this code sample also demonstrates the use of [Avro](http://avro.apache.org/docs/current/) [GenericRecord](https://avro.apache.org/docs/1.7.6/api/java/org/apache/avro/generic/GenericRecord.html) and [DataFileWriter](https://avro.apache.org/docs/1.7.6/api/java/org/apache/avro/file/DataFileWriter.html) to encode the messages instead of using JSON strings.
With GenericRecords, one could parse the Avro message from the consumer side without using any generated code or knowing the schema in advance

This code sample is based on [pubsub-publish-avro-example](https://github.com/GoogleCloudPlatform/professional-services/tree/master/examples/pubsub-publish-avro-example) with changes to use Avro GenericRecord 

## Components

[ObjectPublisher](src/main/java/com/google/cloud/pso/pubsub/common/ObjectPublisher.java) - A generic publisher class that can be used to publish any object as a payload to Cloud Pub/Sub.
This publisher class provides various configurable parameters for controlling the [BatchingSettings](http://googleapis.github.io/gax-java/1.4.1/apidocs/com/google/api/gax/batching/BatchingSettings.html) for the publishing client.

[EmployeePublisher](src/main/java/com/google/cloud/pso/pubsub/EmployeePublisherMain.java) -
An implementation of the [ObjectPublisher](src/main/java/com/google/cloud/pso/pubsub/common/ObjectPublisher.java) to publish Avro encoded test records to Cloud Pub/Sub.
This will generate random test records using the sample [Employee Avro Schema](src/main/avro/employee.avsc).

[employee.avsc](src/main/avro/employee.avsc) used to descibe the Avro schema of the sample Employee. The "doc" attribute is used in this code sample as a place holder for the destination table used by another code sample for BigQuery dynamic destinations. One could simply ignore or remove it if not needed.

### Requirements

* Java 8
* Maven 3
* Cloud Pub/Sub topic and subscription
    * The Pub/Sub topic will be used by the client to publish messages.
    * The Pub/Sub subscription on the same topic will be used by the Dataflow job to read the messages.
* BigQuery table to stream records into - The table schema should match the Avro schema of the messages.

### Building the Project

Build the entire project using the maven compile command.
```sh
mvn clean compile
```

### Running unit tests
Run the unit tests using the maven test command.
```sh
mvn clean test
```

### Publishing sample records to Cloud Pub/Sub
Publish sample [Employee](src/main/avro/employee.avsc) records using the maven exec command.
```sh
mvn compile exec:java \
  -Dexec.mainClass=com.google.cloud.pso.pubsub.EmployeePublisherMain \
  -Dexec.cleanupDaemonThreads=false \
  -Dexec.args=" \
  --topic <output-pubsub-topic> --numberOfMessages <number-of-messages>"
```

There are several other optional parameters related to batch settings. These parameters can be viewed by passing the help flag.
```sh
mvn compile exec:java \
  -Dexec.mainClass=com.google.cloud.pso.pubsub.EmployeePublisherMain \
  -Dexec.cleanupDaemonThreads=false \
  -Dexec.args="--help"

Usage: com.google.cloud.pso.pubsub.EmployeePublisherMain [options]
  * - Required parameters
  Options:
    --bytesThreshold, -b
      Batch threshold bytes.
      Default: 1024
    --delayThreshold, -d
      Delay threshold in milliseconds.
      Default: PT0.5S
    --elementCount, -c
      The number of elements to be batched in each request.
      Default: 500
    --help, -h

    --numberOfMessages, -n
      Number of sample messages to publish to Pub/Sub
      Default: 100000
  * --topic, -t
      The Pub/Sub topic to write messages to
```


### Authentication
These examples use the [Google client libraries to implicitly determine the credentials used][1]. It is strongly recommended that a Service Account with appropriate permissions be used for accessing the resources in Google Cloud Platform Project.

[1]: https://cloud.google.com/docs/authentication/getting-started
