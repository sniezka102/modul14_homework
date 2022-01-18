import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class InvalidDataTest {

    private static final String KEY = "your_key";
    private static final String TOKEN = "your_token";


    private static Stream<Arguments> invalidOrganizationData() {
        return Stream.of(
                Arguments.of("Three upper letter", "snow", "ANN", "sniezka102"),
                Arguments.of("with minus", "snow", "ann-102", "org/github.com/sniezka102"),
                Arguments.of("two lower letters", "snow", "as", "__github.com__sniezka102"),
                Arguments.of("special characters", "snow", "@!@!@!??", "https://github.com/sniezka102"),
                Arguments.of("point", "snow", ".", "htt://github.com/sniezka102"),
                Arguments.of("number", "snow", "9", "https//github.com/sniezka102"),
                Arguments.of("one letter", "snow", "l", "https/github.com/sniezka102...."),
                Arguments.of("upper and lower letters", "snow", "PkP", "proj//github.com/sniezka102"),
                Arguments.of("", "", "", ""));
    }

    @DisplayName("create organization - validation")
    @ParameterizedTest(name = "Display name: {0}, desc: {1}, name: {2}, website: {3}")
    @MethodSource("invalidOrganizationData")
    public void invalidOrganizationData(String displayName, String desc, String name, String website) {


        Organization organization = new Organization();
        organization.setDisplayName(displayName);
        organization.setName(name);
        organization.setWebsite(website);
        organization.setDesc(desc);

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("key", KEY)
                .queryParam("token", TOKEN)
                .queryParam("displayName", organization.getDisplayName())
                .queryParam("name", organization.getName())
                .queryParam("website", organization.getWebsite())
                .queryParam("desc", organization.getDesc())
                .when()
                .post("https://api.trello.com/1/organizations")
                .then()
                .statusCode(400)
                .extract().response();

        JsonPath json = response.jsonPath();
        Assertions.assertThat(json.getString("displayName")).isEqualTo(organization.getDisplayName());

        final String organizationId = json.getString("id");
        given()
                .contentType(ContentType.JSON)
                .queryParam("key", KEY)
                .queryParam("token", TOKEN)
                .when()
                .delete("https://api.trello.com/1/organizations" + "/" + organizationId)
                .then()
                .statusCode(200);

    }
}
