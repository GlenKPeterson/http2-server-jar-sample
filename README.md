# Jetty HTTP/2 Questions
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
1. [Fat jar file doesn't return HTTP responses](PROBLEM1.md)

## Unsolved Problem:
### IE11/Win7 not working with Jetty/Conscrypt/ALPN and HTTP/2.

According to SSL Labs, as of 2019-10-18 there are the 4 strong cipher suites that work with IE11/Win7 (and Win8.1 and some phones - IE11/Win10 works):
 - TLS_DHE_RSA_WITH_AES_256_GCM_SHA384
 - TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
 - TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384
 - TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256

When Jetty starts up, it shows support for these ciphers:
```text
Server@16d04d3d[provider=Conscrypt,keyStore=file:///home/gpeterso/Documents/planbase/goalQpc/classVsJar/target/classes/keystore,trustStore=null] - STARTED
|  |     +> trustAll=false
|  |     +> Protocol Selections
|  |     |  +> Enabled size=4
|  |     |  |  +> TLSv1
|  |     |  |  +> TLSv1.1
|  |     |  |  +> TLSv1.2
|  |     |  |  +> TLSv1.3
|  |     |  +> Disabled size=2
|  |     |     +> SSLv2Hello - ConfigExcluded:'SSLv2Hello' JVM:disabled
|  |     |     +> SSLv3 - ConfigExcluded:'SSLv3' JVM:disabled
|  |     +> Cipher Suite Selections
|  |        +> Enabled size=27
|  |        |  +> TLS_AES_128_GCM_SHA256
|  |        |  +> TLS_AES_256_GCM_SHA384
|  |        |  +> TLS_DHE_DSS_WITH_AES_128_CBC_SHA256
|  |        |  +> TLS_DHE_DSS_WITH_AES_128_GCM_SHA256
|  |        |  +> TLS_DHE_DSS_WITH_AES_256_CBC_SHA256
|  |        |  +> TLS_DHE_DSS_WITH_AES_256_GCM_SHA384
|  |        |  +> TLS_DHE_RSA_WITH_AES_128_CBC_SHA256
|  |        |  +> TLS_DHE_RSA_WITH_AES_128_GCM_SHA256     <= Here
|  |        |  +> TLS_DHE_RSA_WITH_AES_256_CBC_SHA256
|  |        |  +> TLS_DHE_RSA_WITH_AES_256_GCM_SHA384     <= Here
|  |        |  +> TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256
|  |        |  +> TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 <= Here
|  |        |  +> TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384
|  |        |  +> TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 <= Here
|  |        |  +> TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256
|  |        |  +> TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
|  |        |  +> TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384
|  |        |  +> TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
|  |        |  +> TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256
|  |        |  +> TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256
|  |        |  +> TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384
|  |        |  +> TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384
|  |        |  +> TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256
|  |        |  +> TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256
|  |        |  +> TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384
|  |        |  +> TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384
|  |        |  +> TLS_EMPTY_RENEGOTIATION_INFO_SCSV
|  |        +> Disabled size=18
```

Unfortunately, Nmap only finds 3 ciphers for TLS 1.2 and none of them are the IE11/Win7 ones.
```text
$ nmap --script ssl-enum-ciphers -p 8443 localhost

Starting Nmap 7.60 ( https://nmap.org ) at 2019-10-18 17:04 EDT
Nmap scan report for localhost (127.0.0.1)
Host is up (0.000056s latency).

PORT     STATE SERVICE
8443/tcp open  https-alt
| ssl-enum-ciphers: 
|   TLSv1.2: 
|     ciphers: 
|       TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 (rsa 2048) - A
|       TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 (rsa 2048) - A
|       TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256 (rsa 2048) - A
|     compressors: 
|       NULL
|     cipher preference: server
|_  least strength: A

Nmap done: 1 IP address (1 host up) scanned in 0.22 seconds
```



## Extras

### Debugging Jetty
Edit `src/main/resources/logback.xml` and change `<root level="info">` to `<root level="debug">`

### Certificate Creation:
```bash
sudo $JAVA_HOME/bin/keytool -genkey \
    -alias jetty \
    -dname "CN=classVsJar.organicdesign.org, OU=Testing, O=OrganicDesign, L=Upstate, ST=South Carolina, C=US" \
    -keyalg RSA \
    -keysize 2048 \
    -keystore src/main/resources/keystore \
    -sigalg SHA256withRSA \
    -storetype pkcs12 \
    -validity 1096
```
### Test TLS/SSL ciphers
with nmap:
```bash
nmap --script ssl-cert,ssl-enum-ciphers -p 8443 localhost
```

with browser: https://localhost:8443/