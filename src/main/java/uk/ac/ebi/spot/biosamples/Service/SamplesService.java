package uk.ac.ebi.spot.biosamples.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.biosamples.Model.Sample;

@Service
public class SamplesService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${biosamples.api.root:'http://www.ebi.ac.uk/biosamples/api/}")
    private String apiRoot;


    public Sample getSample(String id) {

        String url = "{apiRoot}/samples/{id}";
        ResponseEntity<Resource<Sample>> re = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Resource<Sample>>() {},
                apiRoot, id);
        if (re.getStatusCode().is2xxSuccessful()) {
            Resource<Sample> resourceSample = re.getBody();
            return resourceSample.getContent();
        }
        return null;
    }

}
