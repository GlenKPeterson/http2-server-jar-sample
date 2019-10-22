# IE11/Win7 not working with Jetty/Conscrypt/ALPN

## Expectations

### HTTP1 only
IE11/Win7 does not support HTTP/2: "Partial support [for Http2] in Internet Explorer refers to being limited to Windows 10."

Source: https://caniuse.com/#feat=http2

### Four secure cipher suites
According to SSL Labs, as of 2019-10-18 there are the 4 strong cipher suites that work with IE11/Win7 (and Win8.1 and some phones - IE11/Win10 works):
 - TLS_DHE_RSA_WITH_AES_256_GCM_SHA384 - weak
 - TLS_DHE_RSA_WITH_AES_128_GCM_SHA256 - weak
 - TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384
 - TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256

#### DHE < 2048 are weak
SSLLabs considers DHE-RSA-AES256-GCM-SHA384 with a 1024 bit Diffie Helman ("DH") cipher to be secure, but ssltest.sh considers it "weak" and cites the Logjam CVE-2015-4000 vulnerability.
"Weak" is probably the appropriate category for DH < 2048.

Source:https://weakdh.org/

That leaves us with just two:
 - TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384
 - TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256


### Jetty reports ciphers available

When Jetty starts up, it shows support for these ciphers:
```text
Server@16d04d3d[provider=Conscrypt,keyStore=file:///home/gpeterso/Documents/planbase/goalQpc/jettyHttp2Sample/target/classes/keystore,trustStore=null] - STARTED
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

## Problem
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

## Solution
1. Use EC key algorithm in your keystore
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

2. Don't force ALPN to try h2 first.  Just accept the defaults so that ALPN tries http/1.1 before h2:
    ```kotlin
    val alpn = ALPNServerConnectionFactory()
    // alpn.defaultProtocol = "h2"
    ```

Now, nmap shows the desired ciphers:
```text
$ nmap --script ssl-enum-ciphers -p 8443 localhost

Starting Nmap 7.60 ( https://nmap.org ) at 2019-10-21 17:42 EDT
Nmap scan report for localhost (127.0.0.1)
Host is up (0.000052s latency).

PORT     STATE SERVICE
8443/tcp open  https-alt
| ssl-enum-ciphers: 
|   TLSv1.2: 
|     ciphers: 
|       TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 (prime256v1) - A
|       TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 (prime256v1) - A
|       TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256 (prime256v1) - A
|     compressors: 
|       NULL
|     cipher preference: server
|_  least strength: A

Nmap done: 1 IP address (1 host up) scanned in 0.19 seconds
```

IE11/Win7 and Win8.1 are happy, and no insecurities are reported.