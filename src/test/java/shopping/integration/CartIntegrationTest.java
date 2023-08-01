package shopping.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import shopping.dto.CartItemCreateRequest;
import shopping.dto.LoginRequest;

class CartIntegrationTest extends IntegrationTest {

    @DisplayName("장바구니에 상품 추가")
    @Test
    void addCartItem() {
        // given
        CartItemCreateRequest cartItemCreateRequest = new CartItemCreateRequest(1L);
        String accessToken = RestAssured
                .given().log().all()
                .body(new LoginRequest("admin@example.com", "123456789"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login/token")
                .then().log().all()
                .extract().jsonPath().getString("accessToken");

        // when
        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(cartItemCreateRequest)
                .when().post("/cart")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }
}
