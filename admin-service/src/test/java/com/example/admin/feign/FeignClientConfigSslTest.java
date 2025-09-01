package com.example.admin.feign;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import feign.Util;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import javax.net.ssl.SSLHandshakeException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class FeignClientConfigSslTest {

  @Test
  @Disabled("Illustrative: would require an HTTPS WireMock with wrong truststore to fail")
  void wrongTruststore_causesSslFailure() throws Exception {
    Client client = new Client.Default(
      (javax.net.ssl.SSLSocketFactory) null,
      (hostname, session) -> true // trust-all hostnames for this illustrative test
    );

    Request request = Request.create(
      Request.HttpMethod.GET,
      new URL("https://expired.badssl.com/").toString(),
      Collections.emptyMap(),     // Map<String, Collection<String>> headers
      null,                       // body (Request.Body) -> null for no body
      Util.UTF_8,                 // Charset
      (RequestTemplate) null      // optional template
    );

    assertThatThrownBy(() ->
      client.execute(request, new Request.Options())
    )
      .isInstanceOf(Exception.class)
      .hasRootCauseInstanceOf(SSLHandshakeException.class); // stronger expectation
  }
}
