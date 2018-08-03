package uk.gov.ons.fwmt.acceptancetests.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.fwmt.acceptancetests.dto.JobDto;
import uk.gov.ons.fwmt.acceptancetests.dto.UserDto;
import uk.gov.ons.fwmt.acceptancetests.helper.ResourceRESTHelper;

import java.io.File;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Component
@Slf4j
public class JobServiceStep {

  private String userName = "user";
  private String password = "password";

  private RestTemplate resourcesRestTemplate(RestTemplateBuilder builder) {
    return builder
        .basicAuthorization(userName, password)
        .build();
  }

  @Given("^I have created the following test user in the gateway with authno \"([^\"]*)\" and TM id \"([^\"]*)\"$")
  public void iHaveCreatedTheFollowingTestUserInTheGatewayWithAuthnoAndTmId(String authNo, String tmUsername) {
    UserDto userDto = UserDto.builder().authNo(authNo).tmUsername(tmUsername).active(true).alternateAuthNo(null)
        .build();

    final RestTemplate restTemplate = resourcesRestTemplate(new RestTemplateBuilder());
    String location = "http://localhost:9093/resources/users";
    final Optional<UserDto> resultUserDto = ResourceRESTHelper.post(restTemplate, location, userDto, UserDto.class);
  }

  @When("^I submit a sample CSV of type LFS named \"([^\"]*)\"$")
  public void iHaveASampleCSVOfType(String fileName) {
    int expectedHttpStatusCode = 200;
    String location = "http://localhost:9091/jobs/samples";

    final RestTemplate restTemplate = resourcesRestTemplate(new RestTemplateBuilder());
    File file = new File(String.valueOf("src/test/resources/data/" + fileName));
    Resource fileConvert = new FileSystemResource(file);
    MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
    bodyMap.add("file", fileConvert);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    final HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(bodyMap, headers);
    ResponseEntity result = restTemplate
        .exchange(location, HttpMethod.POST, request, String.class);

    assertEquals(expectedHttpStatusCode, result.getStatusCode().value());
  }

  @Then("^A database record for the job should contain TM id \"([^\"]*)\"$")
  public void aDatabaseRecordForTheJobShouldContainTMId(String tmJobId) {
    final RestTemplate restTemplate = resourcesRestTemplate(new RestTemplateBuilder());
    String location = "http://localhost:9093/resources/jobs" + tmJobId;

    final Optional<JobDto> jobDto = ResourceRESTHelper.get(restTemplate, location, JobDto.class, tmJobId);

    assertTrue(jobDto.isPresent());
    assertEquals(tmJobId, jobDto.toString());
  }
}
