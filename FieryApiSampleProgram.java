package com.efi.fiery.api.samples;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class FieryApiSampleProgram {

	// Trust All Certificates Special Class to enable REST API Calls to EFI Next
	// API. This class ignores certificate errors accessing Fiery API. Do Not
	// use this in production. Ignores all certificate errors (exposed MITM
	// attack)
	private static final class TrustAllCertificates implements
	X509TrustManager, HostnameVerifier {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		public void checkClientTrusted(X509Certificate[] certs, String authType) {}

		public void checkServerTrusted(X509Certificate[] certs, String authType) {}

		public boolean verify(String hostname, SSLSession session) {
			return true;
		}

		public static void install() {
			try {
				// Do Not use this in production.
				TrustAllCertificates trustAll = new TrustAllCertificates();
				// Install the all-trusting trust manager
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, new TrustManager[] {
					trustAll
				},
				new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				// Install the all-trusting host verifier
				HttpsURLConnection.setDefaultHostnameVerifier(trustAll);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(
					"Failed setting up all trusting certificate manager.",
				e);
			} catch (KeyManagementException e) {
				throw new RuntimeException(
					"Failed setting up all trusting certificate manager.",
				e);
			}
		}
	}

	public static void main(String[] args) throws IOException,
	InterruptedException {

		// Set the HostName as Fiery Name or IpAddress.
		final String hostname ="https://{{HOST_NAME}}";

		// Set the Username to login to the host fiery.
		final String username ="{{FIERY_USERNAME}}";

		// Set the Password to login to the fiery.
		final String password = "{{FIERY_PASSWORD}}";

		// Set the API Key for to access Fiery APIs.
		final String apikey = "{{API_KEY_STRING}}";

		// File Path to upload to Fiery.
		final String filePath ="{{ABSOLUTE_PATH_OF_THE_FILE}}";

		// Create the File Object with the location of the file to be uploaded into the Fiery.
		File fileToUpload = new File(filePath);

		// get the name of the file from the path.
		String fileName = fileToUpload.getName();
		
		
		// Create the payload to make the login request.
		String jsonPayloadforLogin = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"accessrights\":\"" + apikey + "\"}";

		// Initialize the Session Cookie to null. This cookie is used to make
		// subsequent Fiery API calls once the login was successful.
		String sessionCookie = null;

		//Buffered reader,DataOutputStream and input stream are used to append the json payload to the request and also to read the response for every API call.
		BufferedReader bufferedReader;
		InputStream inputStreamReader;
		DataOutputStream dataOutputStream;
		int responseCode = 0;
		StringBuffer response;

		// Login to fiery using FieryAPI.
		
		// Do Not use trust all certificates in production.Ignores all
		// certificate errors (exposed MITM attack) Comment the callback if the
		// server has a valid CA signed certificate installed.
		TrustAllCertificates.install();

		// Create a HTTP URL Connection object to connect to Fiery API using the URL object.
		HttpURLConnection connection;

		try {

			// Connect to the Fiery API Login URL using HttpURL Connection.
			connection = (HttpURLConnection) new URL(hostname + "/live/api/v3/login")
				.openConnection();

			// Set the HTTP Request Call type as POST.
			connection.setRequestMethod("POST");

			// Set the HTTP Request Content-Type.
			connection.setRequestProperty("Content-Type",
				"application/json; charset=utf-8");

			// Set the Output to true since the Http Request is POST and
			// contains request data.
			connection.setDoOutput(true);

			// Create a new data output stream to write data to the specified
			// underlying output stream.
			dataOutputStream = new DataOutputStream(
			connection.getOutputStream());

			// Write the JSON Payload bytes to the output stream object.
			dataOutputStream.writeBytes(jsonPayloadforLogin);

			// Read the input stream from the active HTTp connection.
			inputStreamReader = connection.getInputStream();

			// Read the text from the Input Stream
			bufferedReader = new BufferedReader(new InputStreamReader(
			inputStreamReader));

			// Create string object to read the response and use it to append
			// the response to buffer.
			String line;

			// Create the string buffer to store the response from Fiery API
			// Call.
			response = new StringBuffer();

			// Retrieve the response from bufferedReader.
			while ((line = bufferedReader.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}

			// Extracting the Session Cookie from the login response to make
			// subsequent fiery api requests.
			String headerName = null;
			for (int i = 1;
			(headerName = connection.getHeaderFieldKey(i)) != null; i++) {
				if (headerName.equals("Set-Cookie")) {
					sessionCookie = connection.getHeaderField(i);
				}
			}

			// Print the Session Cookie to the console.
			System.out.println("Session Cookie Received is" + sessionCookie);

			// Get the Response Code for the Login Request.
			responseCode = connection.getResponseCode();

		} catch (ProtocolException e) {
			sessionCookie = null;
			e.printStackTrace();
		} catch (IOException e) {
			sessionCookie = null;
			e.printStackTrace();
		}
		// Verify if Login is successful and Session cookie is not null.
		if (responseCode == 200 && sessionCookie != null) {

			// Fiery API URL to Upload new Jobs.
			String fileUploadURL = hostname + "/live/api/v3/jobs";

			// Create boundary object with the current timestamp in milli
			// seconds.
			String boundary = Long.toHexString(System.currentTimeMillis());

			// Open the connection to the Fiery URL using Http URl Connection.
			connection = (HttpURLConnection) new URL(fileUploadURL)
				.openConnection();

			// This sets request method to POST.
			connection.setDoOutput(true);

			// Set the Sesion cookie obtained from the login request to make subsequent Fiery API calls.
			connection.setRequestProperty("Cookie", sessionCookie);
			
			// Set the content type header.
			connection.setRequestProperty("Content-Type","multipart/form-data; boundary=" + boundary);
			
			// Initialize the Print Writer object to null.Creates a new PrintWriter, without automatic line flushing, with the specified file.
			PrintWriter writer = null;

			try {
				OutputStream output = connection.getOutputStream();
				writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true);
				
				writer.println("--" + boundary);
				
				// Set the Writer with the target file name content disposition.
				writer.println("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"");
				
				// Set the content type.
				writer.println();

				// Flush current writer before switching to output
				writer.flush();

				// Stream file content to output
				File textFile = new File(filePath);
				Files.copy(textFile.toPath(), output);

				// Flush current output stream before switching to writer
				output.flush();

				writer.println("--" + boundary + "--");
			} finally {
				// Close the writer object on null.
				if (writer != null) writer.close();
			}
			// Get the Response code for the Fiery Job Creation Request.If the
			// response code is 200 then file is successfully uploaded into the
			// fiery.
			responseCode = ((HttpURLConnection) connection).getResponseCode();
			System.out.println("Fiery Job Creation Response Code" + responseCode);
		}

		// Verify if the file is Uploaded successfully and is present in the Fiery.
		if (responseCode == 200) {
			
			// Get all the jobs in the Fiery.
			connection = (HttpURLConnection) new URL(hostname + "/live/api/v3/jobs").openConnection();
			
			// Set the Request Type as Get.
			connection.setRequestMethod("GET");
			
			// Set the session cookie from the login response to make calls to Fiery API.
			connection.setRequestProperty("Cookie", sessionCookie);

			// Initialize the response reader variable.
			String inputLine;
			
			// Make the Get Request and capture the response.
			bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			//Initialize the buffer to read the response contents.
			response = new StringBuffer();

			while ((inputLine = bufferedReader.readLine()) != null) {
				response.append(inputLine);
			}
			//close the buffered reader after writing the response to string buffer.
			bufferedReader.close();
			System.out.println("Get Jobs from Fiery \t" + response.toString());

			// Get the Details of the File Uploaded into the Fiery.
			String jobId=RetrieveJobIdFromResponse(response.toString(),fileName);
			System.out.println("JOb ID Retrieved is \t" + jobId);
			
			//Get details of single Job BY Id retrieved above.
			connection = (HttpURLConnection) new URL(hostname + "/live/api/v3/jobs/" + jobId).openConnection();
			
			//Set the Request method as GET
			connection.setRequestMethod("GET");
			
			//Set the Session Cookie to the GET Request.
			connection.setRequestProperty("Cookie", sessionCookie);
			
			//Read input stream that reads from the open connection
			bufferedReader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			
			//Initiate the String buffer to store the response.
			response = new StringBuffer();
			
			//append the response to buffer.
			while ((inputLine = bufferedReader.readLine()) != null) {
				response.append(inputLine);
			}
			
			//close the buffered reader.
			bufferedReader.close();
			System.out.println("Get Single Job By Id\t" + response.toString());

			//Rip the Job with JobId. (Rip can only be performed only once the job is successfully uploaded into the fiery. While uploading large files please wait for the file upload process to complete.)
			connection = (HttpURLConnection) new URL(hostname + "/live/api/v3/jobs/" + jobId + "/rip").openConnection();
			
			//Set the Rip Method type as PUT
			connection.setRequestMethod("PUT");
			
			//Set the Session Cookie to the PUT request
			connection.setRequestProperty("Cookie", sessionCookie);
			
			//Read input stream that reads from the open connection
			bufferedReader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			
			//Initiate the String buffer to store the response.
			response = new StringBuffer();
			
			//append the response to buffer.
			while ((inputLine = bufferedReader.readLine()) != null) {
				response.append(inputLine);
			}
			//close the buffered reader.
			bufferedReader.close();
			System.out.println("Rip Single Job By Id\t" + response.toString());
			
			//Print a a job in Fiery based on the JobID. (Print can only be performed only once the job is successfully uploaded into the fiery. While uploading large files please wait for the file upload process to complete before performing the print operation.)
			//If print is followed by Rip. The print operation can return false if a rip is in progress. Issue the print command only when the file is successfully uploaded to fiery and is not busy in the rip process. 
			connection = (HttpURLConnection) new URL(hostname + "/live/api/v3/jobs/" + jobId + "/print").openConnection();
			
			//Set the  Method type as GET
			connection.setRequestMethod("PUT");
			
			//Set the Session Cookie to the GET Request.
			connection.setRequestProperty("Cookie", sessionCookie);
			
			//Read input stream that reads from the open connection
			bufferedReader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			
			//Initiate the String buffer to store the response.
			response = new StringBuffer();
			
			//append the response to buffer.
			while ((inputLine = bufferedReader.readLine()) != null) {
				response.append(inputLine);
			}
			
			//close the buffered reader.
			bufferedReader.close();
			System.out.println("Print Single Job By Id\t" + response.toString());

			
			 //Preview The job from Fiery API based on the jobId only if the upload process is successful & Preview can only be generated once the Rip is successfully completed. use jobs api to get the job id after the rip.
			 if(responseCode==200){
				 
				 	//Get Print Preview Job BY Id.
					connection = (HttpURLConnection) new URL(hostname+"/live/api/v3/jobs/" + jobId+ "/preview/1").openConnection();
					
					//Set the  Method type as GET
					connection.setRequestMethod("GET");
					
					//Set the Session Cookie to the GET Request.
					connection.setRequestProperty("Cookie", sessionCookie);
					
					//Read input stream that reads from the open connection
					bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					
					//Initiate the String buffer to store the response.
					response = new StringBuffer();
					
					//append the response to buffer.
					while ((inputLine = bufferedReader.readLine()) != null) {
						response.append(inputLine.getBytes());
					}
					
					//close the buffered reader.
					bufferedReader.close();
					System.out.println("Get Print Preview Job By Id\t"+response.toString());
			 }

			 	
			 	//Job Update with Attribute for JobId. //Seting the Number of Copies of a job to 10
			 	
			 	connection = (HttpURLConnection) new URL(hostname + "/live/api/v3/jobs/"+jobId ).openConnection();
				
			 	//Set the Rip Method type as PUT
				connection.setRequestMethod("PUT");
				
				//Set the Session Cookie to the PUT request
				connection.setRequestProperty("Cookie", sessionCookie);
				
			 	//Set the jsonPayload for Job Attributes
			 	String jsonPayLoadForJobAttributes = "{\"attributes\": {\"numcopies\": \"10\"}}";
			 	
				// Set the HTTP Request Content-Type.
				connection.setRequestProperty("Content-Type","application/json; charset=utf-8");

				// Set the Output to true since the Http Request is POST and contains request data.
				connection.setDoOutput(true);

				// Create a new data output stream to write data to the specified underlying output stream.
				dataOutputStream = new DataOutputStream(connection.getOutputStream());

				// Write the JSON Payload bytes to the output stream object.
				dataOutputStream.writeBytes(jsonPayLoadForJobAttributes);

				// Read the input stream from the active HTTp connection.
				inputStreamReader = connection.getInputStream();

				// Read the text from the Input Stream
				bufferedReader = new BufferedReader(new InputStreamReader(inputStreamReader));

				// Create string object to read the response and use it to append the response to buffer.
				String line;

				// Create the string buffer to store the response from Fiery API Call.
				response = new StringBuffer();

				// Retrieve the response from bufferedReader.
				while ((line = bufferedReader.readLine()) != null) {
					response.append(line);
					response.append('\r');
				}
				
				//close the buffered reader.
				bufferedReader.close();
				
				System.out.println("Update Job Attributes by Id\t" + response.toString());





			// Logout.
			connection = (HttpURLConnection) new URL(hostname + "/live/api/v3/logout").openConnection();
			
			//Set the Request method as GET
			connection.setRequestMethod("GET");
			
			//Set the Session Cookie to the GET Request.
			connection.setRequestProperty("Cookie", sessionCookie);
			
			//Read input stream that reads from the open connection
			bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			//Initiate the String buffer to store the response.
			response = new StringBuffer();
			
			//append the response to buffer.
			while ((inputLine = bufferedReader.readLine()) != null) {
				response.append(inputLine);
			}
			
			//close the buffered reader.
			bufferedReader.close();
			System.out.println("Log Out\t" + response.toString());
		}
	}
	final static String RetrieveJobIdFromResponse(String responseString, String fileName){
		
		//An alternative approach is to use the JSON parser to parse the response string to obtain jobId.
		
		// The logic retrieve jobID from response assuming the fiery has only successfully uploaded file with the file name as "SampleUpload.pdf".
		int indexOfUploadFileTitle=responseString.indexOf(fileName);
		String temp=responseString.substring(0,indexOfUploadFileTitle);
		temp=temp.substring(temp.indexOf("id")+5,temp.indexOf("\","));
		return temp;
	}

}