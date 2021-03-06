package uk.gov.ons.fwmt.acceptancetests.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
public class ResourceRESTHelper {
  public static <T> Optional<T> get(RestTemplate restTemplate, String url, Class<T> klass, Object... parameters) {
    try {
      final ResponseEntity<T> responseEntity = restTemplate.getForEntity(url, klass, parameters);
      if (responseEntity.getStatusCode().is2xxSuccessful()) {
        // return a successful result
        return Optional.ofNullable(responseEntity.getBody());
      }
      // any success that doesn't have a 2xx is an unexpected error
      throw new RuntimeException();
    } catch (HttpClientErrorException httpException) {
      if (httpException.getStatusCode() == HttpStatus.NOT_FOUND) {
        // a 404, which occurs when we can't find the entity
        return Optional.empty();
      }
      throw httpException;

    } catch (ResourceAccessException accessException) {
      // unable to contact the resource service
      throw accessException;

    }
  }

  public static <T> Optional<T> post(RestTemplate restTemplate, String url, Object request, Class<T> klass,
      Object... parameters) {
    try {
      final ResponseEntity<T> responseEntity = restTemplate.postForEntity(url, request, klass, parameters);
      if (responseEntity.getStatusCode().is2xxSuccessful()) {
        // return a successful result
        return Optional.ofNullable(responseEntity.getBody());
      }
      // any success that doesn't have a 2xx is an unexpected error
      throw new RuntimeException();
    } catch (HttpClientErrorException httpException) {
      // any other unexpected error
      throw httpException;
    } catch (ResourceAccessException accessException) {
      // unable to contact the resource service
      throw accessException;
    }
  }

  public static <T> void put(RestTemplate restTemplate, String url, Object request, Object... parameters) {
    try {
      restTemplate.put(url, request, parameters);
      // any non-exception is a success
    } catch (HttpClientErrorException httpException) {
      // any other unexpected error
      throw httpException;
    } catch (ResourceAccessException accessException) {
      // unable to contact the resource service
      throw accessException;
    }
  }
}
