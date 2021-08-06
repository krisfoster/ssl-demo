package org.example;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Iterator;

public class DummyClient {

    // -Djavax.net.ssl.trustStore=./security/testTrustStore -Djavax.net.ssl.trustStorePassword=b0000m
    public static void main(String[] args) throws Exception {
        System.out.println("TrustStore : " + System.getProperty("javax.net.ssl.trustStore"));

        final URL targetURL = new URL("https://google.com/");
        HttpURLConnection responseConnection = (HttpURLConnection) targetURL.openConnection();
        responseConnection.setRequestMethod("GET");
        try (final InputStream is = responseConnection.getInputStream()) {
            is.read();
        }
        System.out.println("Successfully communicated with " + targetURL);
        //listCertificates();
    }
  /*
  public static void listCertificates() {
      try {
          // Load the JDK's cacerts keystore file
          final String trustStore = System.getProperty(JAVAX_NET_SSL_TRUST_STORE);
          final String trustStorePassword = System.getProperty(JAVAX_NET_SSL_TRUST_STORE_PASSWORD);
          System.out.println("Custom Trust Store     : " + trustStore);
          System.out.println("Custom Trust Store Pass: " + trustStorePassword);

          //String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
          FileInputStream is = new FileInputStream(trustStore);
          KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
          keystore.load(is, trustStorePassword.toCharArray());

          // This class retrieves the most-trusted CAs from the keystore
          PKIXParameters params = new PKIXParameters(keystore);

          // Get the set of trust anchors, which contain the most-trusted CA certificates
          Iterator it = params.getTrustAnchors().iterator();
          while( it.hasNext() ) {
              TrustAnchor ta = (TrustAnchor)it.next();
              // Get certificate
              X509Certificate cert = ta.getTrustedCert();
              System.out.println(cert);
          }
      } catch (CertificateException e) {
      } catch (KeyStoreException e) {
      } catch (NoSuchAlgorithmException e) {
      } catch (InvalidAlgorithmParameterException e) {
      } catch (IOException e) {
      }
  }

   */
}
