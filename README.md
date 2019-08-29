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
String jsonPayloadforLogin = "{\"username\":\"" + FieryUsername + "\",\"password\":\"" + FieryPassword + "\",\"accessrights\":{\"a1\":\"" + FieryAPIAccessKey + "\"}}";
TrustAllCertificates.install(); //To neglect Certificate Errors
HttpURLConnection connection = (HttpURLConnection) new URL(FieryAddress + "/live/api/v3/login").openConnection();
connection.setRequestMethod("POST");
connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
connection.setDoOutput(true);
DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
wr.writeBytes(jsonPayloadforLogin);
InputStream is = connection.getInputStream();
BufferedReader in = new BufferedReader(new InputStreamReader(is));
String line;
StringBuffer response = new StringBuffer();
while ((line = in .readLine()) != null) {
	response.append(line);
	response.append('\r');
}
```

### LogOut

```java
connection = (HttpURLConnection) new URL(FieryAddress+"/live/logout").openConnection();
connection.setRequestMethod("GET");
in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
response.append(inputLine);
}
in.close();
```

### Get Jobs

```java
String inputLine;
connection = (HttpURLConnection) new URL(FieryAddress+"/live/api/v3/jobs").openConnection();
connection.setRequestMethod("GET");
connection.setRequestProperty("Cookie", sessionCookie);
in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
response.append(inputLine);
}
in.close();
```

### Get Single Job

```java
String jobId = "00000000.54889219.205";  //Job Id is set to the job id of the required job.
connection = (HttpURLConnection) new URL(FieryAddress+"/live/api/v3/jobs/"+jobId).openConnection();
connection.setRequestMethod("GET");
connection.setRequestProperty("Cookie", sessionCookie);
in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
response.append(inputLine);
}		
in.close();
```

### Print a Job

```java
connection = (HttpURLConnection) new URL(FieryAddress+"/live/api/v3/jobs/" + jobId+ "/print").openConnection();
connection.setRequestMethod("PUT");
connection.setRequestProperty("Cookie", sessionCookie);
in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
response.append(inputLine);
}
in.close();
```

### Get Job Preview

```java
connection = (HttpURLConnection) new URL(FieryAddress+"/live/api/v3/jobs/" + jobId+ "/preview/1").openConnection();
connection.setRequestMethod("GET");
connection.setRequestProperty("Cookie", sessionCookie);
in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
response.append(inputLine);
}
in.close();
```

### Set Job Attribute

```java
connection = (HttpURLConnection) new URL(hostname + "/live/api/v3/jobs/"+jobId ).openConnection();
connection.setRequestMethod("PUT");
connection.setRequestProperty("Cookie", sessionCookie);
String jsonPayLoadForJobAttributes = "{\"attributes\": {\"numcopies\": \"10\"}}"; 
connection.setRequestProperty("Content-Type","application/json; charset=utf-8");
connection.setDoOutput(true);
DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
dataOutputStream.writeBytes(jsonPayLoadForJobAttributes);
in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
response.append(inputLine);
}
in.close();
```

### Licenses

This Java code is provided under the terms and conditions of the [MIT](./LICENSE) license.

The ```snellen_chart.pdf``` is licensed under [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/)
and is a derived work based on https://en.wikipedia.org/wiki/Snellen_chart#/media/File:Snellen_chart.svg,
which was provided by Jeff Dahl.