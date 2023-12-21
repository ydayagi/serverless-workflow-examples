package dev.parodos;

import dev.parodos.move2kube.ApiException;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;

@QuarkusTest
@TestProfile(CreatePlanningFunctionIT.OverridePropertiesTestProfile.class)
public class CreatePlanningFunctionIT {
  @ConfigProperty(name = "plan-created.event.name")
  private String planCreatedEventName;

  public static class OverridePropertiesTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of(
          "move2kube.api", "http://localhost:8080/api/v1"
      );
    }
  }

  @Test
  @Disabled
  // TODO: before each (or all?) create a workspoce and a project in move2kube local instance
  public void testCreatePlanOK() throws GitAPIException, IOException, ApiException {
    UUID workflowCallerId = UUID.randomUUID();
    RestAssured.given().contentType("application/json")
        .header("ce-specversion", "1.0")
        .header("ce-id", UUID.randomUUID().toString())
        .header("ce-type", "create-plan")
        .header("ce-source", "test")
        .body("{\"gitRepo\": \"https://github.com/gabriel-farache/dotfiles\", " +
            "\"branch\": \"master\"," +
            " \"token\": \"githubtoken\"," +
            " \"workspaceId\": \"aa18a496-a5a6-4a45-9877-a606167114ae\"," +
            " \"projectId\": \"5ff38ab6-3e5a-4f32-94df-5d11eb50b4a6\"," +
            " \"workflowCallerId\": \"" + workflowCallerId + "\"" +
            "}")
        .post("/")
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .header("ce-type", planCreatedEventName)
        .header("ce-kogitoprocrefid", workflowCallerId.toString())
        .header("ce-source", CreatePlanningFunction.SOURCE)
        .body(containsString("\"error\":null"));
  }
}
