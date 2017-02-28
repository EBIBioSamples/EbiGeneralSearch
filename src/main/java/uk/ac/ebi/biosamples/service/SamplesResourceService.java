package uk.ac.ebi.biosamples.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biosamples.model.Entities.Group;
import uk.ac.ebi.biosamples.model.Entities.BioSamplesIterator;
import uk.ac.ebi.biosamples.model.Entities.Sample;

import java.net.URI;


@Service
public class SamplesResourceService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RelationsService relationService;

//    @Value("${biosamples.api.root:'http://www.ebi.ac.uk/biosamples/api'}")
    @Value("${biosamples.api.root:'https://www.ebi.ac.uk/biosamples/api'}")
    private String apiRoot;


    public Resource<Sample> getSample(String id) {

        String url = "{apiRoot}/samples/{id}";
        ResponseEntity<Resource<Sample>> re = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Resource<Sample>>() {},
                apiRoot, id);
        if (re.getStatusCode().is2xxSuccessful()) {
            return re.getBody();
//            Resource<Sample> resourceSample = re.getBody();
//            Sample sample = resourceSample.getContent();
//            sample.setRelations(relationService.getSampleRelations(sample.getAccession()));
//            Resource<Sample> extendedResource =
//                    new Resource<>(sample,resourceSample.getLinks());
        }
        return null;
    }

    public BioSamplesIterator<Sample> getSamplesIterator() {

        BioSamplesIterator<Sample> it = new BioSamplesIterator<>(restTemplate,
                relationService,
                URI.create(apiRoot + "/samples/"),
                new ParameterizedTypeReference<PagedResources<Resource<Sample>>>(){});
        it.initialize();
        return it;
    }

    public BioSamplesIterator<Sample> getSamplesIterator(String samplesStartingPage) {
        BioSamplesIterator<Sample> it = new BioSamplesIterator<>(restTemplate,
                relationService,
                URI.create(samplesStartingPage),
                new ParameterizedTypeReference<PagedResources<Resource<Sample>>>(){});
        it.initialize();
        return it;
    }

    public BioSamplesIterator<Group> getGroupIterator() {
        BioSamplesIterator<Group> it = new BioSamplesIterator<>(restTemplate,
                relationService,
                URI.create(apiRoot + "/groups"),
                new ParameterizedTypeReference<PagedResources<Resource<Group>>>() {});
        it.initialize();
        return it;
    }

    public BioSamplesIterator<Group> getGroupIterator(String groupsStartingPage) {
        BioSamplesIterator<Group> it = new BioSamplesIterator<>(restTemplate,
                relationService,
                URI.create(groupsStartingPage),
                new ParameterizedTypeReference<PagedResources<Resource<Group>>>() {});
        it.initialize();
        return it;
    }


}
