package com.example.fraud;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import io.restassured.response.ResponseOptions;
import org.junit.Test;

import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;

public class FraudTest extends FraudBase {

	@Test
	public void validate_should_Mark_Client_as_Fraud() throws Exception {
		// given:
			MockMvcRequestSpecification request = given()
					.header("Content-Type", "application/json")
					.body("{\"client.id\":1234567890,\"loanAmount\":99999}");

		// when:
			ResponseOptions response = given().spec(request)
					.put("/fraudcheck");

		// then:
			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.header("Content-Type")).isEqualTo("application/json;charset=UTF-8");
		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).field("['rejection.reason']").isEqualTo("Amount too high");
			assertThatJson(parsedJson).field("['fraudCheckStatus']").isEqualTo("FRAUD");
	}

	@Test
	public void validate_should_Not_Mark_Client_as_Fraud() throws Exception {
		// given:
			MockMvcRequestSpecification request = given()
					.header("Content-Type", "application/json")
					.body("{\"client.id\":1234567890,\"loanAmount\":123.123}");

		// when:
			ResponseOptions response = given().spec(request)
					.put("/fraudcheck");

		// then:
			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.header("Content-Type")).isEqualTo("application/json;charset=UTF-8");
		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).field("['rejection.reason']").isNull();
			assertThatJson(parsedJson).field("['fraudCheckStatus']").isEqualTo("OK");
		// and:
			assertThatRejectionReasonIsNull(parsedJson.read("$.['rejection.reason']"));
	}

	@Test
	public void validate_should_return_all_frauds___should_count_all_frauds() throws Exception {
		// given:
			MockMvcRequestSpecification request = given();

		// when:
			ResponseOptions response = given().spec(request)
					.get("/frauds");

		// then:
			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.header("Content-Type")).isEqualTo("application/json;charset=UTF-8");
		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).field("['count']").isEqualTo(200);
	}

	@Test
	public void validate_drunk_frauds() throws Exception {
		// given:
			MockMvcRequestSpecification request = given();

		// when:
			ResponseOptions response = given().spec(request)
					.get("/drunks");

		// then:
			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.header("Content-Type")).isEqualTo("application/json;charset=UTF-8");
		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).field("['count']").isEqualTo(100);
	}

}
