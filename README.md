# Diabetes Real-Time Classifier

## Description
This is a high-performance predictive software designed to process healthcare data (diabetes parameters) in near real-time. 

It uses an actor-based architecture (built with Akka) to ensure high parallelism and separate responsibilities across different components (data ingestion, model training, and prediction). The system integrates with distributed computing and data streaming frameworks to handle large volumes of data efficiently.

### Key Features
* Actor-Based Architecture: High concurrency and synchronization managed by Akka Actors and Streams.
* Automatic ML Model Selection: The system trains 5 different classification algorithms in parallel and automatically selects the one with the best accuracy.
* Real-Time Data Streaming: Continuous data ingestion using Apache Kafka and a local directory watcher.
* Distributed Storage: Historical data is saved to Hadoop HDFS for batch training.
* Real-Time Prediction: New incoming records without a label are evaluated immediately.

## Technologies and Versions
* Language: Scala 2.13.14 / Java 8
* Core Processing: Apache Spark 3.5.2 (Core, SQL, MLlib)
* Actor Framework: Akka 2.8.6
* Message Broker: Apache Kafka (Akka Stream Kafka 4.0.2 / Kafka 2.4.1)
* Distributed Storage: Apache Hadoop HDFS 2.9.2

## Machine Learning Models
The system automatically trains and evaluates the following Spark MLlib classifiers:
* Logistic Regression
* Decision Tree Classifier
* Random Forest Classifier
* Gradient-Boosted Trees (GBT) Classifier
* Linear Support Vector Machine (LinearSVC)

## Prerequisites
To run this software locally, you need to install:
* Java 8 and Maven
* Apache Hadoop (e.g., version 2.9.2) running on localhost:9000
* Apache Kafka and Zookeeper (e.g., version 2.4.1) running on localhost:9092
* Apache Spark configured in Standalone mode. The application requires 3 Spark master instances running on ports 8001, 8002, and 8003.

## Installation and Setup

1. Create the necessary local directories for input, output, and models:
```bash
mkdir -p rt/diabetes/input
mkdir -p rt/diabetes/output
mkdir -p ml-model/diabetes
```

2. Update framework paths in scripts:
Open the bash scripts located in the `bin/` folder (such as `start-all.sh`, `start-kafka.sh`, etc.) and change the `FWK_PATH` variable to match the directory where you installed Hadoop, Kafka, and Spark on your local machine.

## Usage

### 1. Start the Big Data Infrastructure
You can start Zookeeper, Kafka, HDFS, and the 3 Spark instances using the provided unified script:
```bash
./bin/start-all.sh
```

### 2. Start the Application
Build the project using Maven and start the Akka Actor System:
```bash
./bin/start-software.sh
```

### 3. Generate Real-Time Data for Testing
To test the real-time pipeline, you can use the provided data generator. It will continuously create dummy CSV files in the input directory:
```bash
./bin/start-diabetes-maker.sh
```
The software will detect these files, send the data to Kafka, process it, and save the predictions to `rt/diabetes/output/diabetes-prediction.csv`.

### 4. Stop the Infrastructure
To safely stop all running background services and frameworks:
```bash
./bin/stop-all.sh
```

## License
This project is licensed under the MIT License. See the LICENSE file for more details.