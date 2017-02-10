package uk.ac.ebi.spot.biosamples.Service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.biosamples.Model.Sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ApiSupport {

    private static final String baseUrl = "https://www.ebi.ac.uk/biosamples/api";

    public static Sample getSample(RestTemplate restTemplate, String id) {

        String url = "{baseUrl}/samples/{id}";
        ResponseEntity<Resource<Sample>> re = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Resource<Sample>>() {},
                baseUrl, id);
        if (re.getStatusCode().is2xxSuccessful()) {
            Resource<Sample> resourceSample = re.getBody();
            return resourceSample.getContent();
        }
        return null;
    }

    public static Collection<Sample> getSamples(RestTemplate restTemplate) {
        return getSamples(restTemplate, 0, 50);
    }

    public static Collection<Sample> getSamples(RestTemplate restTemplate, int page, int size) {
        String requestUrl = "{baseUrl}/samples?page={page}&size={size}";
        List<Sample> sampleToReturn = new ArrayList<>();
        ResponseEntity<PagedResources<Resource<Sample>>> re = restTemplate.exchange(
                requestUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedResources<Resource<Sample>>>(){},
                baseUrl,page,size);
        if (re.getStatusCode().is2xxSuccessful()) {
            Collection<Resource<Sample>> samples = re.getBody().getContent();
            samples.forEach(sample -> {
                if (sample != null) {
                    sampleToReturn.add(sample.getContent());
                    Optional<Link> relationLink = sample.getLinks().stream().filter(linkType ->
                            linkType.getRel().equalsIgnoreCase("relations")
                    ).findFirst();
                    if (relationLink.isPresent()) {
//                        List<SamplesRelation> rel = getSampleRelations(restTemplate, relationLink.get().getHref());
                    }
                }
            });
        }
        return sampleToReturn;
    }


}
