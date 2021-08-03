# SSL Sample for Native Image

The build process uses GNU make - sorry. You can just cull the actual scripts form the make file if you can't / won't install
make.

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
```

Then access the following URL with you browser - you will probably need to navigate through a long series of warnings about self signed certs.

[The SSL Home Page](https://localhost:8844)

## To Demo that the Native Image is loading the KeyStore from the fileSystem

* Just remove the mount params to the `docker run` command - then there is no keystore file and the apps dies.

## Notes & Links

* [Java KeySTore & TrustStore Differences](https://www.baeldung.com/java-keystore-truststore-difference)
* [How to change the Default Trustore Path, Java](https://stackoverflow.com/questions/59772588/how-to-change-default-truststore-path)
* [Generating a new TrustStore](https://docs.oracle.com/cd/E19509-01/820-3503/6nf1il6er/index.html)
