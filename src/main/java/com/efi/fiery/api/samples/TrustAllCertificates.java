package com.efi.fiery.api.samples;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Trust All Certificates Special Class to enable REST API Calls to EFI Next
 * API. This class ignores certificate errors accessing Fiery API. Do Not
 * use this in production. Ignores all certificate errors (exposed MITM attack)
 */
final class TrustAllCertificates implements X509TrustManager, HostnameVerifier {

	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	public void checkClientTrusted(X509Certificate[] certs, String authType) {
	}

	public void checkServerTrusted(X509Certificate[] certs, String authType) {
	}

	public boolean verify(String hostname, SSLSession session) {
		return true;
	}

	public static void install() {
		try {
			// Do Not use this in production.
			TrustAllCertificates trustAll = new TrustAllCertificates();
			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, new TrustManager[]{
							trustAll
					},
					new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(trustAll);
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new RuntimeException("Failed setting up all trusting certificate manager.", e);
		}
	}
}
