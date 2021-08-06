.PHONY: build

ARTIFACT=ssl-demo
VERSION=1.0-SNAPSHOT
MAIN_CLASS=org.example.ClassFileServer
JAR_FILE="./target/$(ARTIFACT)-$(VERSION).jar"

REPO="krisfoster"
GRAAL_VER = "20.2.0"
JAVA_VER = "11"
PWD=$$PWD

$(JAR_FILE):
	rm -rf target
	mkdir -p target/native-image
	echo "Packaging $ARTIFACT with Maven"
	mvn -ntp package > target/native-image/output.txt

build-jdk: $(JAR_FILE)
	docker build \
		--build-arg JAR_FILE=$(JAR_FILE) \
		-f Dockerfile-java \
		-t krisfoster/ssl-demo:java .

build-ni: $(JAR_FILE)
	docker build \
		--build-arg JAR_FILE=$(JAR_FILE) \
		-f Dockerfile-ni \
		-t krisfoster/ssl-demo:ni .

clean:
	mvn clean

run-jdk:
	docker run --publish=8844:8844 --mount type=bind,source="$$(pwd)"/,target=/app krisfoster/ssl-demo:java

run-ni:
	docker run --publish=8844:8844 --mount type=bind,source="$$(pwd)"/,target=/app krisfoster/ssl-demo:ni

# This create a custom keystore for our SSL Server. It will contain a self signed certificate and the public private
# keys required for the SSL chanel
create-keystore:
	keytool -genkey -keyalg RSA -alias demo01 -keystore selfsigned.jks -validity 365 -keysize 4098

# This creates a new TrustStore with a certificate in it, that is essentially useless. The point of this is
# that if we are using this TrustStore our test secure connection to google throws a security error. This allows
# us to see easily that a custom TrustStore has been sued and not the default cacerts file that is shipped it Java.
# I am mainly interested in this as it is a way to categorically prove which TrustStore a native image is using
create-trusts:
	keytool -import -alias entrust_2048 -keystore ./security/testTrustStore1 -file ./security/entrust_2048_ca.cer

# -agentlib:native-image-agent=config-output-dir=META-INF/native-image
profile: $(JAR_FILE)
	java -agentlib:native-image-agent=config-output-dir=META-INF/native-image \
		 -cp $(JAR_FILE) -Djavax.net.ssl.trustStore=./security/testTrustStore -Djavax.net.ssl.trustStorePassword=b0000m \
		 org.example.DummyClient

run-client: $(JAR_FILE)
	java -cp $(JAR_FILE) org.example.DummyClient

run-client-with-custom-trusts: $(JAR_FILE)
	java -cp $(JAR_FILE) -Djavax.net.ssl.trustStore=./security/testTrustStore -Djavax.net.ssl.trustStorePassword=b0000m org.example.DummyClient

# Builds a native image of the client. Notice that we specify the cusotm trustore here. This is what will get baked into
# the native image. After the image is built, you can not change the TrustStore using the JVM parameters
client-ni: $(JAR_FILE)
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

run-ni-client:
	./org.example.DummyClient
