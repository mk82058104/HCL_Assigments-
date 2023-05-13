package HCL_RestAssured.HCL_RestAssured;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class UserAPI {
    private static final String BASE_URL = "https://reqres.in/api";
    private String endpoint = "https://reqres.in/api/users";
    private static final int USER_ID = 2;
  
    private static String createdUserId;

    @BeforeTest
    public void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void testGetUsers() {
        Response response = given()
            .when()
            .get("/users?page=1")
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .response();

        // Extract first name and email of all users
        List<String> firstNames = response.jsonPath().getList("data.first_name");
        List<String> emails = response.jsonPath().getList("data.email");

        // Print first name and email of all users
        for (int i = 0; i < firstNames.size(); i++) {
            System.out.println(firstNames.get(i) + " - " + emails.get(i));
        }

        // Filter out user details by ID
        Map<String, ?> userDetails = filterUserById(response, USER_ID);

        // Validate response code and user details
        given()
            .pathParam("userId", USER_ID)
            .when()
            .get("/users/{userId}")
            .then()
            .assertThat()
            .statusCode(200)
            .body("data.id", equalTo(USER_ID))
            .body("data.email", equalTo(userDetails.get("email")))
            .body("data.first_name", equalTo(userDetails.get("first_name")))
            .body("data.last_name", equalTo(userDetails.get("last_name")));
    }

    private Map<String, ?> filterUserById(Response response, int userId) {
        List<Map<String, ?>> users = response.jsonPath().getList("data");
        return users.stream().filter(user -> user.get("id").equals(userId)).findFirst().get();
    }
    
    @Test(priority = 1)
    public void createNewUserAndGetID() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "John Doe");
        requestBody.put("job", "Software Engineer");

        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .post(endpoint);

        Assert.assertEquals(response.getStatusCode(), 201);

        String userID = response.jsonPath().getString("id");
        Assert.assertNotNull(userID);
        System.out.println("New user ID: " + userID);
    }

    @Test(priority = 2, dependsOnMethods = "createNewUserAndGetID")
    public void updateUserInformation() {
        String userID = "2"; // ID retrieved in the previous step

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "John Doe Jr.");
        requestBody.put("job", "Senior Software Engineer");

        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .put(endpoint + "/" + userID);

        Assert.assertEquals(response.getStatusCode(), 200);

        String name = response.jsonPath().getString("name");
        String job = response.jsonPath().getString("job");

        Assert.assertEquals(name, "John Doe Jr.");
        Assert.assertEquals(job, "Senior Software Engineer");
    }

    
}
