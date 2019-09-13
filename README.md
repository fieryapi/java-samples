Java Samples
===============

Fiery job management samples. The sample code requires JRE installed on the system.

**Note** Always use secure connection (HTTPS) when connecting to Fiery API in production.


## Compile this samples

This sample code is using [Maven](http://maven.apache.org/) as build tool.
In order to compile the sources, you need to download and install Maven for your operating system.

```bash
mvn compile
```

## Code examples for API requests

The following code snippets doe outline how to code against the Fiery API.

### Login

```java
String jsonPayloadforLogin = "{\"username\":\"" + FIERY_USERNAME
        + "\",\"password\":\"" + FIERY_PASSWORD
        + "\",\"apikey\":\"" + API_KEY + "\"}";
HttpURLConnection connection = (HttpURLConnection) new URL(FieryAddress + "/live/api/v5/login").openConnection();
connection.setRequestMethod("POST");
connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
connection.setDoOutput(true);
DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
dataOutputStream.writeBytes(jsonPayloadforLogin);
readAllResponseFromConnection(connection);
```

### LogOut

```java
HttpURLConnection connection = (HttpURLConnection) new URL(FieryAddress + "/live/api/v5/logout").openConnection();
connection.setRequestMethod("POST");
connection.setRequestProperty("Cookie", sessionCookie);
readAllResponseFromConnection(connection);
```

### Get Jobs

```java
HttpURLConnection connection = (HttpURLConnection) new URL(FieryAddress + "/live/api/v5/jobs").openConnection();
connection.setRequestMethod("GET");
connection.setRequestProperty("Cookie", sessionCookie);
String response = readAllResponseFromConnection(connection);
```

### Get Single Job

```java
String jobId = "00000000.54889219.205";  //Job Id is set to the job id of the required job.
HttpURLConnection connection = (HttpURLConnection) new URL(FieryAddress + "/live/api/v5/jobs/" + jobId).openConnection();
connection.setRequestMethod("GET");
connection.setRequestProperty("Cookie", sessionCookie);
readAllResponseFromConnection(connection);
```

### Print a Job

```java
String jobId = "00000000.54889219.205";  //Job Id is set to the job id of the required job.
HttpURLConnection connection = (HttpURLConnection) new URL(FieryAddress + "/live/api/v5/jobs/" + jobId + "/print").openConnection();
connection.setRequestMethod("PUT");
connection.setRequestProperty("Cookie", sessionCookie);
readAllResponseFromConnection(connection);
```

### Get Job Preview

```java
HttpURLConnection connection = (HttpURLConnection) new URL(FieryAddress + "/live/api/v5/jobs/" + jobId + "/preview/1").openConnection();
connection.setRequestMethod("GET");
connection.setRequestProperty("Cookie", sessionCookie);
File file = new File("print-preview.jpg");
readAllResponseAndWriteToFileFromConnection(connection, file);
```

### Set Job Attribute

```java
String jobId = "00000000.54889219.205";  //Job Id is set to the job id of the required job.
HttpURLConnection connection = (HttpURLConnection) new URL(FieryAddress + "/live/api/v5/jobs/" + jobId).openConnection();
connection.setDoOutput(true);
connection.setRequestMethod("PUT");
connection.setRequestProperty("Cookie", sessionCookie);
String jsonPayLoadForJobAttributes = "{\"attributes\": {\"numcopies\": \"10\"}}";
connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
dataOutputStream.writeBytes(jsonPayLoadForJobAttributes);
readAllResponseFromConnection(connection);
```

### Licenses

This Java code is provided under the terms and conditions of the [MIT](./LICENSE) license.

The ```snellen_chart.pdf``` is licensed under [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/)
and is a derived work based on https://en.wikipedia.org/wiki/Snellen_chart#/media/File:Snellen_chart.svg,
which was provided by Jeff Dahl.