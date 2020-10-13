package Proxy;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import tourGuide.Model.AttractionMapper;

import java.util.Arrays;
import java.util.List;

public class RestTemplateTest {

    @Test
    public void testProxyGetAttractions() {
        String attractionUrl = "http://localhost:8046/gpsattractions";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<AttractionMapper[]> result =
                restTemplate.getForEntity(attractionUrl, AttractionMapper[].class);
        System.out.println(result.getBody());
        List<AttractionMapper> attractionMappers = Arrays.asList(result.getBody());
        System.out.println(attractionMappers.size());
    }

}
