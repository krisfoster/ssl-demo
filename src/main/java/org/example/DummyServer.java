package org.example;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;

/* ClassFileServer.java -- a simple file server that can server
 * Http get request in both clear and secure channel
 *
 * The ClassFileServer implements a ClassServer that
 * reads files from the file system. See the
 * doc for the "Main" method for how to run this
 * server.
 *
 * Sample taken from :
 * https://docs.oracle.com/javase/10/security/sample-code-illustrating-secure-socket-connection-client-and-server.htm#JSSEC-GUID-1B7038DC-7564-4EE6-A1DF-6B1445077E2E
 */

public class DummyServer extends Server {

    /**
     * Password for key store
     */
    public static final String DEFAULT_TOP_SECRET_PASSWORD = "b0000m";
    /**
     * Value for TLS parameter
     */
    public static final String TLS = "TLS";
    /**
     * Key Manager Implementation
     */
    public static final String SUN_X_509 = "SunX509";
    /**
     * KeyStore type
     */
    public static final String JKS = "JKS";

    /**
     * Default PORT for listening on
     */
    private static int DefaultServerPort = 8844;

    /**
     * Default location of the Key Store used for SSL
     */
    private static String DefaultKeyStore = "/app/security/selfsigned.jks";

    /**
     * Constructs a ClassFileServer.
     * @param ss ServerSocket
     */
    public DummyServer(ServerSocket ss) throws IOException {
        super(ss);
    }

    /**
     * Main method. Simple Server that returns some text over HTTP / HTTPS
     */
    public static void main(String args[])
    {
        System.out.println(
                "USAGE: java DummyServer port [TLS keyStorePath]");
        int port = DefaultServerPort;
        String keyStore = DefaultKeyStore;
        String password = DEFAULT_TOP_SECRET_PASSWORD;

        // Process the command line options
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        String type = "PlainSocket";
        if (args.length >= 2) {
            type = args[1];
        }
        if (args.length >= 3) {
            keyStore = args[2];
        }
        if (args.length >= 4) {
            password = args[3];
        }

        try {
            ServerSocketFactory ssf = DummyServer.getServerSocketFactory(type, keyStore, password);
            ServerSocket ss = ssf.createServerSocket(port);
            new DummyServer(ss);
        } catch (IOException e) {
            System.out.println("Unable to start ClassServer: " +
                    e.getMessage());
            e.printStackTrace();
        }
    }

    private static ServerSocketFactory getServerSocketFactory(final String type, final String keyPath, final String password) {
        //
        if (type.equals(TLS)) {
            SSLServerSocketFactory ssf = null;
            try {
                // set up key manager to do server authentication
                SSLContext ctx;
                KeyManagerFactory kmf;
                KeyStore ks;
                char[] passphrase = password.toCharArray();

                ctx = SSLContext.getInstance(TLS);
                kmf = KeyManagerFactory.getInstance(SUN_X_509);
                ks = KeyStore.getInstance(JKS);

                // Load the KeyStore
                ks.load(new FileInputStream(keyPath), passphrase);
                kmf.init(ks, passphrase);
                ctx.init(kmf.getKeyManagers(), null, null);

                ssf = ctx.getServerSocketFactory();
                return ssf;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return ServerSocketFactory.getDefault();
        }
        return null;
    }
}