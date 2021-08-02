package org.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import javax.net.*;

/*
 * ClassServer.java -- a simple file server that can serve
 * Http get request in both clear and secure channel
 *
 * Sample taken from :
 * https://docs.oracle.com/javase/10/security/sample-code-illustrating-secure-socket-connection-client-and-server.htm#JSSEC-GUID-1B7038DC-7564-4EE6-A1DF-6B1445077E2E
 */

public abstract class Server implements Runnable {

    private ServerSocket server = null;
    private static String VERSION = "0.1";
    private static String OUTPUT_TEXT =
            "<pre>" +
            "      ___           ___                                                ___                                                   \r\n" +
            "     /  /\\         /  /\\                                _____         /  /\\         _____          ___                    \r\n" +
            "    /  /:/_       /  /:/_                              /  /::\\       /  /::\\       /  /::\\        /__/|                   \r\n" +
            "   /  /:/ /\\     /  /:/ /\\    ___     ___             /  /:/\\:\\     /  /:/\\:\\     /  /:/\\:\\      |  |:|              \r\n" +
            "  /  /:/ /::\\   /  /:/ /::\\  /__/\\   /  /\\           /  /:/~/::\\   /  /:/~/::\\   /  /:/~/::\\     |  |:|               \r\n" +
            " /__/:/ /:/\\:\\ /__/:/ /:/\\:\\ \\  \\:\\ /  /:/          /__/:/ /:/\\:| /__/:/ /:/\\:\\ /__/:/ /:/\\:|  __|__|:|           \r\n" +
            " \\  \\:\\/:/~/:/ \\  \\:\\/:/~/:/  \\  \\:\\  /:/           \\  \\:\\/:/~/:/ \\  \\:\\/:/__\\/ \\  \\:\\/:/~/:/ /__/::::\\  \r\n" +
            "  \\  \\::/ /:/   \\  \\::/ /:/    \\  \\:\\/:/             \\  \\::/ /:/   \\  \\::/       \\  \\::/ /:/     ~\\~~\\:\\     \r\n" +
            "   \\__\\/ /:/     \\__\\/ /:/      \\  \\::/               \\  \\:\\/:/     \\  \\:\\        \\  \\:\\/:/        \\  \\:\\  \r\n" +
            "     /__/:/        /__/:/        \\__\\/                 \\  \\::/       \\  \\:\\        \\  \\::/          \\__\\/         \r\n" +
            "     \\__\\/         \\__\\/                                \\__\\/         \\__\\/         \\__\\/                " +
            "</pre>";


    /**
     * Constructs a ClassServer based on <b>ss</b> and
     * obtains a file's bytecodes using the method <b>getBytes</b>.
     *
     */
    protected Server(ServerSocket ss) {
        server = ss;
        newListener();
    }

    /**
     * The "listen" thread that accepts a connection to the
     * server, parses the header to obtain the file name
     * and sends back the bytes for the file (or error
     * if the file is not found or the response was malformed).
     */
    public void run()
    {
        Socket socket;

        // Accept a connection
        try {
            socket = server.accept();
        } catch (IOException e) {
            System.out.println("Class Server died: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Create a new thread to accept the next connection
        newListener();

        try {
            OutputStream rawOut = socket.getOutputStream();

            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    rawOut)));
            try {
                byte[] bytecodes = OUTPUT_TEXT.getBytes(StandardCharsets.UTF_8);
                // send bytecodes in response (assumes HTTP/1.0 or later)
                try {
                    out.print("HTTP/1.0 200 OK\r\n");
                    out.print("Content-Length: " + bytecodes.length + "\r\n");
                    out.print("Content-Type: text/html\r\n\r\n");
                    out.flush();
                    rawOut.write(bytecodes);
                    rawOut.flush();
                } catch (IOException ie) {
                    ie.printStackTrace();
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
                // write out error response
                out.println("HTTP/1.0 400 " + e.getMessage() + "\r\n");
                out.println("Content-Type: text/html\r\n\r\n");
                out.flush();
            }

        } catch (IOException ex) {
            // eat exception (could log error to log file, but
            // write out to stdout for now).
            System.out.println("error writing response: " + ex.getMessage());
            ex.printStackTrace();

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Create a new thread to listen.
     */
    private void newListener()
    {
        (new Thread(this)).start();
    }
}