package se.unlogic.standardutils.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLUtils {

	public static final TrustManager DEFAULTTRUSTMANAGER = new DefaultTrustManager();
	
	private static class DefaultTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

		}

		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

		}

		public X509Certificate[] getAcceptedIssuers() {

			return null;
		}

	}

}
