package org.organicdesign.classVsJar

import org.conscrypt.OpenSSLProvider
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory
import org.eclipse.jetty.http2.HTTP2Cipher
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnection
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.SecureRequestCustomizer
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.SslConnectionFactory
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.handler.SecuredRedirectHandler
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.security.Security
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_OK

private val logger: Logger = LoggerFactory.getLogger(ClazzVsJar::class.java)

object ClazzVsJar : AbstractHandler() {

    override fun handle(target: String,
                        baseRequest: Request,
                        request: HttpServletRequest,
                        response: HttpServletResponse) {
        try {
            response.contentType = "text/html;charset=utf-8"
            response.writer.println("<!DOCTYPE html>\n" +
                                    "<html lang=\"en\">\n" +
                                    "  <head>\n" +
                                    "    <meta charset=\"utf-8\">\n" +
                                    "    <title>Test Page</title>\n" +
                                    "  </head>\n" +
                                    "  <body>\n" +
                                    "    <h1>It works!</h1>\n" +
                                    "    <p>Working.</p>\n" +
                                    "  </body>\n" +
                                    "</html>")

            response.contentType = "text/html;charset=utf-8"
            response.status = SC_OK

            findHandlableRequest(request).isHandled = true
        } catch (t: Throwable) {
            logger.error("Error trying to process request", t)
        }
        logger.info("End of 'handle'")
    } // end handle()
} // end ClazzVsJar object

fun findHandlableRequest(request: HttpServletRequest): Request =
        if (request is Request) {
            request
        } else {
            HttpConnection.getCurrentConnection().httpChannel.request
        }

fun main(args: Array<String>) {

    // HTTP Configuration
    val httpConfig = HttpConfiguration()
    httpConfig.secureScheme = "https"
    httpConfig.securePort = 8443
    httpConfig.outputBufferSize = 32768
    httpConfig.requestHeaderSize = 8192
    httpConfig.responseHeaderSize = 8192
    httpConfig.sendServerVersion = true
    httpConfig.sendDateHeader = false
    httpConfig.addCustomizer(SecureRequestCustomizer())

    val keyStorePath = "target/classes/keystore"
    logger.info("keyStorePath=$keyStorePath")
    val keyStoreFile = File(keyStorePath)

    logger.info("keyStoreFile.exists()=${keyStoreFile.exists()}")
    logger.info("keyStoreFile.isFile=${keyStoreFile.isFile}")
    logger.info("keyStoreFile.canRead()=${keyStoreFile.canRead()}")

    Security.addProvider(OpenSSLProvider())
    val sslContextFactory = SslContextFactory.Server()
    sslContextFactory.keyStorePath = keyStorePath

    /*
sudo $JAVA_HOME/bin/keytool -genkey \
    -alias jetty \
    -dname "CN=classVsJar.organicdesign.org, OU=Testing, O=OrganicDesign, L=Upstate, ST=South Carolina, C=US" \
    -keyalg RSA \
    -keysize 2048 \
    -keystore src/main/resources/keystore \
    -sigalg SHA256withRSA \
    -storetype pkcs12 \
    -validity 1096
     */
    sslContextFactory.setKeyStorePassword("Not3A2Real1Password")
    sslContextFactory.cipherComparator = HTTP2Cipher.COMPARATOR
    sslContextFactory.provider = "Conscrypt"

    // test TLS with: nmap --script ssl-cert,ssl-enum-ciphers -p 8443 localhost
    // Test with browser: https://localhost:8443/

    sslContextFactory.isRenegotiationAllowed = false
    sslContextFactory.provider = "Conscrypt"

    val server = Server()
    server.isDumpAfterStart = true
    server.setAttribute("module", "session-cache-hash")

    // https://github.com/fstab/http2-examples/blob/master/jetty-http2-server-example/src/main/java/de/consol/labs/h2c/examples/server/Http2Server.java
    // HTTP/2 Connection Factory
    val h2 = HTTP2ServerConnectionFactory(httpConfig)
//    NegotiatingServerConnectionFactory.checkProtocolNegotiationAvailable()
    val alpn = ALPNServerConnectionFactory()
    alpn.defaultProtocol = "h2"

    // SSL Connection Factory
    val ssl = SslConnectionFactory(sslContextFactory, alpn.protocol)

    // HTTP/2 Connector
    val http2Connector = ServerConnector(server, ssl, alpn, h2, HttpConnectionFactory(httpConfig))
    http2Connector.port = 8443
    server.addConnector(http2Connector)

    // SSL Connector
//    val sslConnector = ServerConnector(server,
//                                       SslConnectionFactory(sslContextFactory,
//                                                            HttpVersion.HTTP_1_1.asString()),
//                                       HttpConnectionFactory(httpConfig))
//    sslConnector.port = 8443
//
//    server.addConnector(sslConnector)
//    server.handler = MemJogLib
    val handlers = HandlerList()
    handlers.addHandler(SecuredRedirectHandler())
    handlers.addHandler(ClazzVsJar)
    server.handler = handlers

    server.start()
    server.join()
}