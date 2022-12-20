# Quarkus / Temporal integration example

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

A Temporal Workflow Execution is a durable, reliable, and scalable function execution.

**Durability**

Durability is the absence of an imposed time limit.

A Workflow Execution is durable because it executes a Temporal Workflow Definition (also called a Temporal Workflow Function), your
application code, effectively once and to completion—whether your code executes for seconds or years.

**Reliability**

Reliability is responsiveness in the presence of failure.

A Workflow Execution is reliable, because it is fully recoverable after a failure. The Temporal Platform ensures the state of the Workflow
Execution persists in the face of failures and outages and resumes execution from the latest state.

**Scalability**

Scalability is responsiveness in the presence of load.

A single Workflow Execution is limited in size and throughput but is scalable because it can Continue-As-NewLink
preview icon in response to load. A Temporal Application is scalable because the Temporal Platform is capable of 
supporting millions to billions of Workflow Executions executing concurrently, which is realized by the design and
nature of the Temporal ClusterLink preview icon and Worker ProcessesLink preview icon

If you want to learn more about Quarkus, please visit its website: https://docs.temporal.io/concepts .

## Demo Requirements

- Java 17
- Temporal ([Run on your localhost](https://docs.temporal.io/clusters/quick-install#docker-compose))
- Maven 3.8.4
- Quarkus 2.15+
- Git cli 2.38+

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script

./mvnw compile quarkus:dev -Dquarkus.args="helloWorld worker"

```

## Packaging and running the application

```shell script
./mvnw package -DskipTests

java -jar target/quarkus-app/quarkus-run.jar helloWorld worker
```

## Creating a native executable

Not supported due to a Temporal dependency issue.

## Examples

I really recommend you to read and understand from a conceptual point of view some Temporal Key concepts as Workflow, Workers, Tasks and Activities.
Doc Ref: https://docs.temporal.io/concepts

For all the example I assume that you already have launched a [local temporal server](http://localhost:8080)
```shell
git clone https://github.com/temporalio/docker-compose.git
cd  docker-compose
docker-compose -f docker-compose-postgres.yml up
```

All the examples are launched from a command Cli developed with [PicoCli](https://quarkus.io/guides/picocli) Quarkus extension.

### Example 1. HelloWorld

1. Create a HelloWorld namespace

**DevMode**

```shell
./mvnw quarkus:dev -Dquarkus.args="new-namespace my-project"
```

**Runnable application**

```shell
java -jar target/quarkus-app/quarkus-run.jar new-namespace my-project
```

2. Launch helloWorld worker in order to handler all incoming tasks that are pushed to Queue `HELLO_WORLD_TASK_QUEUE`

**DevMode**

```shell
./mvnw quarkus:dev -Dquarkus.args="helloWorld worker -n my-project"
```

**Runnable application**

```shell
java -jar target/quarkus-app/quarkus-run.jar helloWorld worker -n my-project
```

3. Launch HelloWorld workflow

**DevMode**

```shell
./mvnw quarkus:dev -Dquarkus.args="helloWorld workflow -n my-project"
```

**Runnable application**

```shell
java -jar target/quarkus-app/quarkus-run.jar helloWorld workflow -n my-project
```