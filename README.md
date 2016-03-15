Java Samples
===============

Fiery job management samples. The sample code requires JRE installed on the system.

**Note** Always use secure connection (HTTPS) when connecting to Fiery API in production.


### Login

```
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

```
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

```
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

```
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

```
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

```
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

```
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


