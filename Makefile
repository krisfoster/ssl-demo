.PHONY: build

ARTIFACT=ssl-demo
VERSION=1.0-SNAPSHOT
MAIN_CLASS=org.example.ClassFileServer
JAR_FILE="$(ARTIFACT)-$(VERSION).jar"

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
	rm -rf ./installs

run-jdk:
	docker run --publish=8844:8844 --mount type=bind,source="$$(pwd)"/,target=/app krisfoster/ssl-demo:java

run-ni:
	docker run --publish=8844:8844 --mount type=bind,source="$(pwd)"/,target=/app krisfoster/ssl-demo:ni