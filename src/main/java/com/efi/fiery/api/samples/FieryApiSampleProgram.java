package com.efi.fiery.api.samples;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FieryApiSampleProgram {

	// --- START CONFIGURATION --------------------------------------

	// Set the HostName as Fiery Name or IpAddress.
	private static final String HOST_NAME = "{{HOST_NAME}}";

	// Set the Username to login to the host fiery.
	private static final String FIERY_USERNAME = "{{USER}}";

	// Set the Password to login to the fiery.
	private static final String FIERY_PASSWORD = "{{PASSWORD}}";

	// Set the API Key for to access Fiery APIs.
	private static final String API_KEY = "{{API_KEY}}";

	// --- END CONFIGURATION --------------------------------------

	private static final String FILE_NAME = "snellen_chart.pdf";
	private static final String HTTP_LINE_SEPARATOR = "\r\n";

	// ------------------------------------------------------------

	public static void main(String[] args) {

		// Do Not use trust all certificates in production.Ignores all
		// certificate errors (exposed MITM attack) Comment the callback if the
		// server has a valid CA signed certificate installed.
		TrustAllCertificates.install();

		try {

			LoginResponse loginResponse = doRequestLogin();

			if (loginResponse.responseCode != 200 || loginResponse.sessionCookie == null) {
				System.err.println("API did not respond success fully. Expecting responseCode=200. Stopping further execution");
				System.exit(-1);
			}

			doCreateAJob(loginResponse);

			String jobId = findJobIdByFileName(loginResponse, FILE_NAME);
			fetchJobDetails(loginResponse, jobId);
			ripJob(loginResponse, jobId);
			printJob(loginResponse, jobId);
			getPrintPreview(loginResponse, jobId);
			alterNumberOfCopiesByJobID(loginResponse, jobId);

			doRequestLogout(loginResponse);

		} catch (IOException e) {
			// --------------------------
			throw new RuntimeException(e);
		}
	}

	private static LoginResponse doRequestLogin() throws IOException {
		// Connect to the Fiery API Login URL using HttpURL Connection.
		HttpURLConnection connection = (HttpURLConnection) new URL(HOST_NAME + "/live/api/v5/login")
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
		DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());

		// Write the JSON Payload bytes to the output stream object.
		// Create the payload to make the login request.
		String jsonPayloadforLogin = "{\"username\":\"" + FIERY_USERNAME
				+ "\",\"password\":\"" + FIERY_PASSWORD
				+ "\",\"apikey\":\"" + API_KEY + "\"}";


		dataOutputStream.writeBytes(jsonPayloadforLogin);

		readAllResponseFromConnection(connection);

		// Extracting the Session Cookie from the login response to make
		// subsequent fiery api requests.
		String headerName;
		String sessionCookie = "";
		for (int i = 1; (headerName = connection.getHeaderFieldKey(i)) != null; i++) {
			if (headerName.equals("Set-Cookie")) {
				sessionCookie = connection.getHeaderField(i);
			}
		}

		// Print the Session Cookie to the console.
		System.out.println("Session Cookie Received is: " + sessionCookie);

		// Get the Response Code for the Login Request.
		int responseCode = connection.getResponseCode();
		return new LoginResponse(responseCode, sessionCookie);
	}

	private static void doCreateAJob(LoginResponse loginResponse) throws IOException {
		// Fiery API URL to Upload new Jobs.
		String fileUploadURL = HOST_NAME + "/live/api/v5/jobs";

		// Create boundary object with the current timestamp in milliseconds.
		String boundary = Long.toHexString(System.currentTimeMillis());

		// Open the connection to the Fiery URL using Http URl Connection.
		HttpURLConnection connection = (HttpURLConnection) new URL(fileUploadURL).openConnection();

		// This sets request method to POST.
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");

		// Set the Sesion cookie obtained from the login request to make subsequent Fiery API calls.
		connection.setRequestProperty("Cookie", loginResponse.sessionCookie);

		// Set the content type header.
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		// Initialize the Print Writer object to null.Creates a new PrintWriter, without automatic line flushing, with the specified file.
		PrintWriter writer = null;
		try {
			OutputStream output = connection.getOutputStream();
			writer = new PrintWriter(new OutputStreamWriter(output, UTF_8), true);

			writer.print("--" + boundary);
			writer.print(HTTP_LINE_SEPARATOR);

			// Set the Writer with the target file name content disposition.
			writer.print("Content-Disposition: form-data; name=\"file\"; filename=\"" + FILE_NAME + "\"");
			writer.print(HTTP_LINE_SEPARATOR);
			writer.print("Content-Type: application/octet-stream");
			writer.print(HTTP_LINE_SEPARATOR);

			// Set the content type.
			writer.print(HTTP_LINE_SEPARATOR);

			// Flush current writer before switching to output
			writer.flush();

			// Stream file content to output
			try (InputStream inputStream = FieryApiSampleProgram.class.getResourceAsStream("/" + FILE_NAME)) {
				inputStream.transferTo(output);
			}

			// Flush current output stream before switching to writer
			output.flush();

			writer.print("--" + boundary + "--");
			writer.print(HTTP_LINE_SEPARATOR);
		} finally {
			// Close the writer object on null.
			if (writer != null) writer.close();
		}
		// Get the Response code for the Fiery Job Creation Request.If the
		// response code is 200 then file is successfully uploaded into the
		// fiery.
		readAllResponseFromConnection(connection);
		int responseCode = connection.getResponseCode();
		System.out.println("Fiery Job Creation Response Code: " + responseCode);
	}

	private static String findJobIdByFileName(LoginResponse loginResponse, String fileName) throws IOException {
		System.out.println("Find a job a a given file name: "+ fileName);

		// Get all the jobs in the Fiery.
		HttpURLConnection connection = (HttpURLConnection) new URL(HOST_NAME + "/live/api/v5/jobs").openConnection();

		// Set the Request Type as Get.
		connection.setRequestMethod("GET");

		// Set the session cookie from the login response to make calls to Fiery API.
		connection.setRequestProperty("Cookie", loginResponse.sessionCookie);

		String response = readAllResponseFromConnection(connection);

		// Get the Details of the File Uploaded into the Fiery.
		String jobId = retrieveJobIdFromResponse(response, fileName);
		System.out.println("Job ID retrieved is: " + jobId);

		return jobId;
	}

	private static void fetchJobDetails(LoginResponse loginResponse, String jobId) throws IOException {
		System.out.println("Get details of single job by Id=" + jobId);

		//Get details of single Job BY Id retrieved above.
		HttpURLConnection connection = (HttpURLConnection) new URL(HOST_NAME + "/live/api/v5/jobs/" + jobId).openConnection();

		//Set the Request method as GET
		connection.setRequestMethod("GET");

		//Set the Session Cookie to the GET Request.
		connection.setRequestProperty("Cookie", loginResponse.sessionCookie);

		readAllResponseFromConnection(connection);
	}

	private static void ripJob(LoginResponse loginResponse, String jobId) throws IOException {
		System.out.println("Rip Single Job By Id=" + jobId);

		// Rip the Job with JobId. (Rip can only be performed only once the job is successfully uploaded into the fiery.
		// While uploading large files please wait for the file upload process to complete.)
		HttpURLConnection connection = (HttpURLConnection) new URL(HOST_NAME + "/live/api/v5/jobs/" + jobId + "/rip").openConnection();

		//Set the Rip Method type as PUT
		connection.setRequestMethod("PUT");

		//Set the Session Cookie to the PUT request
		connection.setRequestProperty("Cookie", loginResponse.sessionCookie);

		readAllResponseFromConnection(connection);
	}

	private static void printJob(LoginResponse loginResponse, String jobId) throws IOException {
		System.out.println("Print Single Job By id=" + jobId);

		//Print a a job in Fiery based on the JobID. (Print can only be performed only once the job is successfully uploaded into the fiery. While uploading large files please wait for the file upload process to complete before performing the print operation.)
		//If print is followed by Rip. The print operation can return false if a rip is in progress. Issue the print command only when the file is successfully uploaded to fiery and is not busy in the rip process.
		HttpURLConnection connection = (HttpURLConnection) new URL(HOST_NAME + "/live/api/v5/jobs/" + jobId + "/print").openConnection();

		//Set the  Method type as GET
		connection.setRequestMethod("PUT");

		//Set the Session Cookie to the GET Request.
		connection.setRequestProperty("Cookie", loginResponse.sessionCookie);

		readAllResponseFromConnection(connection);
	}

	private static void getPrintPreview(LoginResponse loginResponse, String jobId) throws IOException {
		System.out.println("Get Print Preview Job By id=" + jobId);

		// Preview The job from Fiery API based on the jobId only if the upload process is successful & Preview
		// can only be generated once the Rip is successfully completed. use jobs api to get the job id after the rip.

		//Get Print Preview Job BY Id.
		HttpURLConnection connection = (HttpURLConnection) new URL(HOST_NAME + "/live/api/v5/jobs/" + jobId + "/preview/1").openConnection();

		//Set the  Method type as GET
		connection.setRequestMethod("GET");

		//Set the Session Cookie to the GET Request.
		connection.setRequestProperty("Cookie", loginResponse.sessionCookie);

		File tempFile = File.createTempFile("print-preview", ".jpg");
		readAllResponseAndWriteToFileFromConnection(connection, tempFile);
		System.out.println("Preview written to temp file: " + tempFile);
	}

	private static void alterNumberOfCopiesByJobID(LoginResponse loginResponse, String jobId) throws IOException {
		System.out.println("Update Job Attributes by id=" + jobId);

		//Job Update with Attribute for JobId. //Seting the Number of Copies of a job to 10

		HttpURLConnection connection = (HttpURLConnection) new URL(HOST_NAME + "/live/api/v5/jobs/" + jobId).openConnection();

		// Set the Rip Method type as PUT
		connection.setDoOutput(true);
		connection.setRequestMethod("PUT");

		//Set the Session Cookie to the PUT request
		connection.setRequestProperty("Cookie", loginResponse.sessionCookie);

		//Set the jsonPayload for Job Attributes
		String jsonPayLoadForJobAttributes = "{\"attributes\": {\"numcopies\": \"10\"}}";

		// Set the HTTP Request Content-Type.
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

		// Create a new data output stream to write data to the specified underlying output stream.
		DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());

		// Write the JSON Payload bytes to the output stream object.
		dataOutputStream.writeBytes(jsonPayLoadForJobAttributes);

		readAllResponseFromConnection(connection);
	}

	private static void doRequestLogout(LoginResponse loginResponse) throws IOException {
		System.out.println("Log Out.");
		// Logout.
		HttpURLConnection connection = (HttpURLConnection) new URL(HOST_NAME + "/live/api/v5/logout").openConnection();

		//Set the Request method as POST
		connection.setRequestMethod("POST");

		//Set the Session Cookie to the POST Request.
		connection.setRequestProperty("Cookie", loginResponse.sessionCookie);

		readAllResponseFromConnection(connection);
	}

	private static String readAllResponseFromConnection(HttpURLConnection connection) throws IOException {
		// Read the input stream from the active HTTp connection.
		InputStream inputStreamReader = connection.getInputStream();

		// Read the text from the Input Stream
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamReader))) {
			// Create string object to read the response and use it to append the response to buffer.
			String line;
			// Create the string buffer to store the response from Fiery API Call.
			StringBuilder response = new StringBuilder();
			// Retrieve the response from bufferedReader.
			while ((line = bufferedReader.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			String responseString = response.toString();
			System.out.println("> API Response: " + responseString);
			return responseString;
		}
	}

	private static void readAllResponseAndWriteToFileFromConnection(HttpURLConnection connection, File file) throws IOException {
		// Read the input stream from the active HTTp connection.
		System.out.println("> API response code: " + connection.getResponseCode());
		InputStream inputStream = connection.getInputStream();
		try (FileOutputStream fos = new FileOutputStream(file)){
			inputStream.transferTo(fos);
		}
	}

	private static String retrieveJobIdFromResponse(String responseString, String fileName) {
		// An alternative approach is to use the JSON parser to parse the response string to obtain jobId.
		// The logic retrieve jobID from response assuming the fiery has only successfully uploaded file with the file name as "SampleUpload.pdf".
		// This simple parser assumes, there is an "id" followed by the "title" in the JSON response
		// e.g. ..."id":"uninit.5B03446F.1","title":"Connect_Alumni_Newsletter_LTR.pdf",....
		Pattern pattern = Pattern.compile("\"id\":\"([^\"]+)\",\"title\":\"" + fileName);
		Matcher matcher = pattern.matcher(responseString);
		if (!matcher.find()) {
			System.out.println("ERROR: file name '" +fileName + "' not found in API response.");
			return null;
		}
		return matcher.group(1);
	}

	private static class LoginResponse {

		final int responseCode;
		final String sessionCookie;

		LoginResponse(int responseCode, String sessionCookie) {
			this.responseCode = responseCode;
			this.sessionCookie = sessionCookie;
		}
	}
}