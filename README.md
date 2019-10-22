# Jetty HTTP/2 Sample
This is neither approved nor endorsed by the Jetty project, Eclispe Foundation, or similar.
It's just a minimal project for reproducing and solving my personal jetty HTTP/2 issues.
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
    -dname "CN=jettyHttp2Sample.organicdesign.org, OU=Testing, O=OrganicDesign, L=Upstate, ST=South Carolina, C=US" \
    -genkeypair \
    -keyalg EC \
    -keysize 256 \
    -keystore src/main/resources/keystore \
    -sigalg SHA256withECDSA \
    -storetype pkcs12 \
    -validity 1096
```
### Test TLS/SSL ciphers
with curl (this is what success looks like):
```bash
$ curl --insecure https://localhost:8443 -D headers.txt
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>Test Page</title>
  </head>
  <body>
    <h1>It works!</h1>
    <p>Working.</p>
  </body>
</html>

$ cat headers.txt
HTTP/2 200
server: Jetty(9.4.20.v20190813)
content-type: text/html;charset=utf-8
```

with nmap (simple cipher test):
```bash
nmap --script ssl-enum-ciphers -p 8443 localhost
```

with testssl.sh (detalied cipher test):
```bash
testssl.sh localhost:8443
```

with browser: https://localhost:8443/