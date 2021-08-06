# SSL Sample for Native Image

The build process uses GNU make - sorry. You can just cull the actual scripts form the make file if you can't / won't install
make.

## In KeyStores We Trust

SSSL Security in Java rests on to key ideas, the KeyStore and the TrustStore. The following is a brief summary of the purpose of each:

* KeyStore : This is used to identify yourself. In here we place all keys and certificates that establish our identity.
* TrustStore : This is used to establish the identity of others. In here we store the X509 (can we use other types?) certificates for our
  trust authorities. These are the authorities that we trust to sign X509 certificates for sevrers / sites that we wish to use over SSL
  
## Establishing Trust

To establish that we can trust an endpoint we wish to connect to is who they say they are, and that there has been no
form of man in the middle attack, we need first to start with the certificate for that endpoint. As a part of the 
SSL handshake that establishes the secure connection, there ia an excahnge of certificates. Ther server sends it certificate
to us and, optionally if required by the server, the client sends it.  We consider only the server certificate from now on, but the situation is the same for the client side.  

When we receive the certificate, we know have the public key (PK) for the server and a signature for the certficate plus a signing
authority (the next entity in the chain that has signed the certificate fo the server with its' own public key). We step though the
signers until we reach the certificate of a singing party that is present in our TrustStore. This is called the Trust Anchor.
It is this, and the wonder so asymmetrical cryptogrpahy, that establish the facts that we can trust the certificate of the server to be from
who we believe it to be from and that the public key attached to the server certificate is correct and untampered with.

## KeyStores & TrustStores in GraalVM Native Image

In standard Java it is possible to use custom Keystores and TrustStores, in fact you need to specify a keystore if
you are going to provide an SSL ServerSocket. The folloing is a code snippet that shows this:

```java
// Set up KeyStore (containing out certs & keys) for use in SSL
SSLContext ctx;
KeyManagerFactory kmf;
KeyStore ks;
char[] passphrase = password.toCharArray();

ctx = SSLContext.getInstance(TLS);
kmf = KeyManagerFactory.getInstance(SUN_X_509);
ks = KeyStore.getInstance(JKS);

// Load the KeyStore
ks.load(new FileInputStream(keyStore), passphrase);
kmf.init(ks, passphrase);
ctx.init(kmf.getKeyManagers(), null, null);

ServerSocketFactory ssf = ctx.getServerSocketFactory();
```

Here we programmaticaly setup a keystore, loaded form the filesystem, and use the keys and certificate within to create
the secure channel. The custom keystore is created using the Java keytool, but I have a `make` tsk for this:

```shell
make create-keystore
```

If you build a native image out of this code, it is possible to not package the keystore within the application (you 
can, I think?) also package the keystore into the code as well. This gives some flexibilty as to how you handle the keystore/

This is all well and good for sever sockets, but if we want to use a custom TrustStore hwo do we acheive this using Java?
As a default Java ships with a TrustStrore called `cacerts`. This will be used unless we explicitly specify that a 
different one should be used. We typically may want to specify our on for internal use, where we need to add our own 
trusted authority that is responsible for signing certificates within an organisation. 

Setting up a custom TrustStore is done using the following JVM system properties:

```shell
java -cp ./target/app.jar -Djavax.net.ssl.trustStore=./security/testTrustStore -Djavax.net.ssl.trustStorePassword=b0000m org.example.DummyClient
```

If you build an native image from some code that uses a custom TrustStore, see my `org.example.DummyClient` example,
the Native Image will "bake" into the executable the TrustStore that was avaialable at image build time. Importantly
the TrustStore can not be updated at runtime, unlike how you can with Java.  To specify a custom TrustStore at build time
you need to pass the same parameters that you did to the JVM to set the custom TrusStore:

```shell
native-image \
      --verbose \
      --allow-incomplete-classpath \
      --no-fallback \
      --no-server \
      --enable-all-security-services \
      --enable-url-protocols=https \
      -Djavax.net.ssl.trustStore=./security/testTrustStore -Djavax.net.ssl.trustStorePassword=b0000m \
      -cp $(JAR_FILE) \
      org.example.DummyClient
```

This issue will be resolved in the 21.3 release of GraalVM. From this release onwards you should be able to specify a
custom TrustStore at runtime.

An alternative vie, might be that you can do SSL termination outisde of the app images as a part of your declarative infrastrucutre.
I am sure there are pros and cons of both approaches.

## What is in this Repo

This is a collection of various experiments relating to SSL, server and client side. Just to help me build a test harness
for my own understanding.

Please consult the `Makefile` to see what is available.

## To build the Java app and run it

```shell
# To build on docker
$ make build-jdk

# To run
$ make run-jdk
```

Then access the following URL with you browser - you will probably need to navigate through a long series of warnings about self signed certs.

[The SSL Home Page](https://localhost:8844)

## To build and run the Native Image

```shell
# To build it
$ make build-ni

# To un the native image
$ make run-ni

# To build the client
$ make client-ni
```

Then access the following URL with you browser - you will probably need to navigate through a long series of warnings about self signed certs.

[The SSL Home Page](https://localhost:8844)

## To Demo that the Native Image is loading the KeyStore from the fileSystem

* Just remove the mount params to the `docker run` command - then there is no keystore file and the apps dies.

## Notes & Links

* [Java KeySTore & TrustStore Differences](https://www.baeldung.com/java-keystore-truststore-difference)
* [How to change the Default Trustore Path, Java](https://stackoverflow.com/questions/59772588/how-to-change-default-truststore-path)
* [Generating a new TrustStore](https://docs.oracle.com/cd/E19509-01/820-3503/6nf1il6er/index.html)
* [Load root certificates at image build time #1999](https://github.com/oracle/graal/issues/1999)
* [Allow root certificates to be configured at run time of native image #3091](https://github.com/oracle/graal/pull/3091)
* [Quarkus: Native & SSL](https://quarkus.io/guides/native-and-ssl#the-truststore-path)
