package com.example.user.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.user.UserServiceApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.junit.jupiter.api.BeforeEach;
import io.restassured.RestAssured;

@org.springframework.test.context.ActiveProfiles("test")
@SpringBootTest(classes = UserServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,properties = "server.ssl.enabled=false")
class AuthAndUserIntegrationTest {

  @LocalServerPort
  int port;
  @BeforeEach
  void restAssured() {
        RestAssured.baseURI = "http://localhost"; // Tomcat logs show HTTP, not HTTPS
        RestAssured.port = port;                  // use the random port (e.g., 40646)
  }
  @BeforeAll
  static void setup() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @Test
  void signup_login_refresh_flow() {
    var signup = RestAssured.given().contentType(ContentType.JSON)
      .body(Map.of("username","sajid","password","Str0ngPass!","email","sajid@example.com"))
      .post("/api/auth/signup")
      .then().statusCode(201).extract().jsonPath();
    String access = signup.getString("accessToken");
    assertThat(access).isNotBlank();

    var login = RestAssured.given().contentType(ContentType.JSON)
      .body(Map.of("username","sajid","password","Str0ngPass!"))
      .post("/api/auth/login")
      .then().statusCode(200).extract().jsonPath();
    String refresh = login.getString("refreshToken");
    assertThat(refresh).isNotBlank();

    RestAssured.given().header("Authorization","Bearer " + refresh)
      .post("/api/auth/refresh")
      .then().statusCode(200);
  }
}
