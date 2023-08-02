package com.example.productservice.web.controller;

import com.example.productservice.web.service.DataManipulate;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import static org.hamcrest.Matchers.equalTo;
import static io.restassured.RestAssured.with;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecurityConfig.class)
@WebAppConfiguration
class ProductControllerTest {

//    @Container
//    static final KeycloakContainer keycloak;
//
//    static {
////        keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:20.0.3")
////                .withRealmImportFile("/keycloak/realm-export.json");
////        keycloak.start();
//    }

    @MockBean
    private DataManipulate dataManipulate;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @LocalServerPort
    private int port;

//    @DynamicPropertySource
//    static void registerResourceProperty(DynamicPropertyRegistry registry){
//        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", ()->
//                keycloak.getAuthServerUrl() + "/realms/shopping-application");
//    }

    @Test
    @WithMockUser(value = "testUser")
    void addProduct() throws Exception {
        RestAssured.baseURI = "http://localhost:" + port;
        with().header("Authorization",getTestUserBearer())
                .contentType("application/json")
                .param("product", "product")
                .param("image", String.valueOf(new MockMultipartFile("image", new byte[]{})))
                .when()
                .post("/add")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body(equalTo(true));
    }

    @Test
    void getProduct() {
    }

    @Test
    void getAllProducts() {
    }

    @Test
    void getProductImages() {
    }

    private String getTestUserBearer()  {
        try {
//            URI authorizationURI = new URIBuilder(keycloak.getAuthServerUrl() + "/realms/shopping-application" +
//                    "/protocol/openid-connect/token").build();
            URI authorizationURI = new URIBuilder("http://localhost:8078/realms/shopping-application/protocol/openid-connect/token").build();
            WebClient webclient = WebClient.builder()
                    .build();
            MultiValueMap<String,String> formData = new LinkedMultiValueMap<>();
            formData.put("grant_type", Collections.singletonList("password"));
            formData.put("client_id",Collections.singletonList("browser-client"));
            formData.put("username",Collections.singletonList("user1"));
            formData.put("password",Collections.singletonList("123"));
            String result = webclient.post()
                    .uri(authorizationURI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            JacksonJsonParser jsonParser = new JacksonJsonParser();
            return "Bearer " + jsonParser.parseMap(result)
                    .get("access_token")
                    .toString();
        } catch (URISyntaxException e) {
            log.error("Can't obtain an access token from Keycloak!", e);
            throw new RuntimeException(e);
        }
    }
}