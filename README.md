# Class Vs. Jar
This is a minimal project for reproducing an issue.
It has a very simple HTML5 file hard-coded in the Kotlin code.

If I run this webapp the way IntelliJ does (giving the compiled classes directory, then adding all the jar dependencies):

```bash
M2REP=/home/me/.m2/repository

java -classpath \
target/classes:\
$M2REP/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar:\
$M2REP/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar:\
$M2REP/org/eclipse/jetty/jetty-server/9.4.20.v20190813/jetty-server-9.4.20.v20190813.jar:\
$M2REP/javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar:\
$M2REP/org/eclipse/jetty/jetty-http/9.4.20.v20190813/jetty-http-9.4.20.v20190813.jar:\
$M2REP/org/eclipse/jetty/jetty-io/9.4.20.v20190813/jetty-io-9.4.20.v20190813.jar:\
$M2REP/org/eclipse/jetty/jetty-alpn-conscrypt-server/9.4.20.v20190813/jetty-alpn-conscrypt-server-9.4.20.v20190813.jar:\
$M2REP/org/conscrypt/conscrypt-openjdk-uber/2.1.0/conscrypt-openjdk-uber-2.1.0.jar:\
$M2REP/org/eclipse/jetty/jetty-alpn-server/9.4.20.v20190813/jetty-alpn-server-9.4.20.v20190813.jar:\
$M2REP/org/eclipse/jetty/http2/http2-common/9.4.20.v20190813/http2-common-9.4.20.v20190813.jar:\
$M2REP/org/eclipse/jetty/http2/http2-hpack/9.4.20.v20190813/http2-hpack-9.4.20.v20190813.jar:\
$M2REP/org/eclipse/jetty/jetty-util/9.4.20.v20190813/jetty-util-9.4.20.v20190813.jar:\
$M2REP/org/eclipse/jetty/http2/http2-server/9.4.20.v20190813/http2-server-9.4.20.v20190813.jar:\
$M2REP/org/slf4j/slf4j-api/1.7.28/slf4j-api-1.7.28.jar:\
$M2REP/org/jetbrains/kotlin/kotlin-stdlib/1.3.50/kotlin-stdlib-1.3.50.jar:\
$M2REP/org/jetbrains/kotlin/kotlin-stdlib-common/1.3.50/kotlin-stdlib-common-1.3.50.jar:\
$M2REP/org/jetbrains/annotations/13.0/annotations-13.0.jar \
org.organicdesign.classVsJar.ClazzVsJarKt
```

That works:
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

But if I run from the fat-jar file `java -jar target/ROOT.jar` it doesn't work
```bash
$ rm headers.txt

$ curl --insecure https://localhost:8443 -D headers.txt
curl: (56) Unexpected EOF

$ ls -s
total 0
0 headers.txt
```

Requirements:
 - Java 11.0.4
 - Maven 3.6.0
 - Intellij IDEA (for testing, not for building)
 - I'm using Ubuntu 18.04 Desktop

Build:
```bash
mvn clean package
```

## Chrome
With chrome, the class files work great, but the Jar file produces, "This site can’t be reached... unexpectedly closed the connection... ERR_CONNECTION_CLOSED"

## wget2
Works great with class files (simply downloads the html file), but with the Jar file, it goes into a loop until I hit CTRL-C:

```bash
$ wget2 -d --no-check-certificate https://localhost:8443/
02.140606.876 name=check-certificate value=https://localhost:8443/ invert=1
02.140606.876 name=check-certificate value=https://localhost:8443/ invert=1
02.140606.876 Local URI encoding = 'UTF-8'
02.140606.876 Input URI encoding = 'UTF-8'
02.140606.876 Fetched HSTS data from '/home/gpeterso/.wget-hsts'
02.140606.876 Fetched HPKP data from '/home/gpeterso/.wget-hpkp'
02.140606.877 add TLS session data for localhost (maxage=64800, size=1260)
02.140606.877 Fetched TLS session data from '/home/gpeterso/.wget-session'
02.140606.877 Fetched OCSP hosts from '/home/gpeterso/.wget-ocsp_hosts'
02.140606.877 Fetched OCSP fingerprints from '/home/gpeterso/.wget-ocsp'
02.140606.877 *url = https://localhost:8443/
02.140606.877 *3 https://localhost:8443/
02.140606.877 local filename = 'index.html'
02.140606.877 host_add_job: job fname index.html
02.140606.877 host_add_job: 0x55b15a6bfb60 https://localhost:8443/
02.140606.877 host_add_job: qsize 1 host-qsize=1
02.140606.877 queue_size: qsize=1
02.140606.877 queue_size: qsize=1
02.140606.877 queue_size: qsize=1
02.140606.877 [0] action=1 pending=0 host=0x0
02.140606.877 qsize=1 blocked=0
02.140606.877 pause=-1570039566877
02.140606.877 dequeue job https://localhost:8443/
02.140606.877 resolving localhost:8443...
02.140606.877 has 127.0.0.1:8443
02.140606.877 Add dns cache entry localhost
02.140606.877 trying 127.0.0.1:8443...
02.140606.877 GnuTLS init
02.140606.877 Certificates loaded: -1
02.140606.877 GnuTLS init done
02.140606.877 TLS False Start requested
02.140606.877 ALPN offering h2
02.140606.877 ALPN offering http/1.1
02.140606.877 found cached session data for localhost
WARNING: The certificate is NOT trusted. The certificate issuer is unknown.
02.140606.885 TLS False Start: on
02.140606.885 ALPN: Server accepted protocol 'h2'
02.140606.885 Handshake completed
02.140606.885 established connection localhost
02.140606.885 cookie_create_request_header for host=localhost path=
02.140606.885 HTTP2 stream id 1
02.140606.885 [0] action=1 pending=1 host=0x55b15a6bf9c0
02.140606.885 qsize=1 blocked=0
02.140606.885 pause=-1570039566885
02.140606.885 [0] action=2 pending=1 host=0x55b15a6bf9c0
02.140606.885   ##  pending_requests = 1
02.140606.885   ##  loop responses=0
02.140606.885 [FRAME 0] > SETTINGS
02.140606.885 [FRAME 1] > HEADERS
02.140606.885 [FRAME 1] > :method: GET
02.140606.885 [FRAME 1] > :path: /
02.140606.885 [FRAME 1] > :scheme: https
02.140606.885 [FRAME 1] > :authority: localhost
02.140606.885 [FRAME 1] > accept-encoding: gzip, deflate, bzip2, xz, lzma
02.140606.885 [FRAME 1] > accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
02.140606.885 [FRAME 1] > user-agent: wget2/1.0.0
02.140606.885   ##  loop responses=0
02.140606.886 Got delayed session data
02.140606.886 found TLS session data for localhost
02.140606.886 removed TLS session data for localhost
02.140606.886 add TLS session data for localhost (maxage=64800, size=1260)
02.140606.886   ##  loop responses=0
02.140606.886 [FRAME 0] < SETTINGS
02.140606.886 [FRAME 0] < WINDOW_UPDATE
02.140606.886   ##  loop responses=0
02.140606.886 [FRAME 0] > SETTINGS
02.140606.886   ##  loop responses=0
02.140606.886 [FRAME 0] < SETTINGS
02.140606.886   ##  loop responses=0
02.140608.260   ##  loop responses=0
02.140608.260   ##  loop responses=0
...
```

That scrolled really fast in a loop until I hit CTRL-C

```bash
02.140608.260 host_increase_failure: localhost failures=1
02.140608.260 closing connection
```

It just sat there doing nothing, so I hit CTRL-C again to exit


## Nmap Cipher Test:
Whether it yields HTML or not, the server gives the same response to my cipher test:
```bash
$ nmap --script ssl-cert,ssl-enum-ciphers -p 8443 localhost

Starting Nmap 7.60 ( https://nmap.org ) at 2019-10-02 13:32 EDT
Nmap scan report for localhost (127.0.0.1)
Host is up (0.000053s latency).
rDNS record for 127.0.0.1: localhost

PORT     STATE SERVICE
8443/tcp open  https-alt
| ssl-cert: Subject: commonName=classVsJar.organicdesign.org/organizationName=OrganicDesign/stateOrProvinceName=South Carolina/countryName=US
| Issuer: commonName=classVsJar.organicdesign.org/organizationName=OrganicDesign/stateOrProvinceName=South Carolina/countryName=US
| Public Key type: rsa
| Public Key bits: 2048
| Signature Algorithm: sha256WithRSAEncryption
| Not valid before: 2019-09-25T20:57:00
| Not valid after:  2022-09-25T20:57:00
| MD5:   483e 988f 709f 7300 3f3d 1d51 4f95 81cb
|_SHA-1: abe8 c872 c7d7 dfb1 4152 8bf3 2fee 5617 7613 94cd
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

Nmap done: 1 IP address (1 host up) scanned in 0.19 seconds
```