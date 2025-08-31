package com.example.user.integration;

import static org.hamcrest.Matchers.notNullValue;

import com.example.user.UserServiceApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = UserServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "server.ssl.enabled=false")
class RefreshTokenRotationTest {

  @LocalServerPort
  int port;
  @BeforeEach
  void restAssured() {
  RestAssured.baseURI = "http://localhost";
  RestAssured.port = port;
  }

  @BeforeAll
  static void setup() { RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(); }

  @Test
  void refresh_rotates_and_prevents_reuse() {
    RestAssured.port = port;

    // signup -> get refresh
    String refresh = RestAssured.given().contentType(ContentType.JSON)
      .body(Map.of("username","rotator","password","Str0ngPass!","email","rotator@example.com"))
      .post("/api/auth/signup")
      .then().statusCode(201)
      .extract().jsonPath().getString("refreshToken");

    // first refresh succeeds and returns a new refresh token
    String newRefresh = RestAssured.given().header("Authorization","Bearer " + refresh)
      .post("/api/auth/refresh")
      .then().statusCode(200)
      .body("accessToken", notNullValue())
      .body("refreshToken", notNullValue())
      .extract().jsonPath().getString("refreshToken");

    // reusing the original refresh should fail (revoked)
    RestAssured.given().header("Authorization","Bearer " + refresh)
      .post("/api/auth/refresh")
      .then().statusCode(401);
  }
}
