# Jetty HTTP/2 Sample
This is a minimal project for reproducing jetty HTTP/2 issues.
It has a very simple HTML5 file hard-coded in the Kotlin code.

## To Build
Requires:
* Java (I use openjdk11)
* Maven

Command:
```bash
mvn clean package
```
## To Run
```text
java -jar target/ROOT.jar
```
or to debug javax.net:
```text
java -Djavax.net.debug=all -jar target/ROOT.jar
```

## Solved Problems:
1. [Fat jar file doesn't return HTTP responses](problem1.md)
2. [IE11/Win7 not working with Jetty/Conscrypt/ALPN](problem2.md)

## Extras

### Debugging Jetty
Edit `src/main/resources/logback.xml` and change `<root level="info">` to `<root level="debug">`

### Certificate Creation:
```bash
sudo $JAVA_HOME/bin/keytool \
    -alias jetty \
    -dname "CN=classVsJar.organicdesign.org, OU=Testing, O=OrganicDesign, L=Upstate, ST=South Carolina, C=US" \
    -genkeypair \
    -keyalg EC \
    -keysize 256 \
    -keystore src/main/resources/keystore \
    -sigalg SHA256withECDSA \
    -storetype pkcs12 \
    -validity 1096
```
### Test TLS/SSL ciphers
with nmap:
```bash
nmap --script ssl-enum-ciphers -p 8443 localhost
```

with testssl.sh:
```bash
testssl.sh localhost:8443
```

with browser: https://localhost:8443/
