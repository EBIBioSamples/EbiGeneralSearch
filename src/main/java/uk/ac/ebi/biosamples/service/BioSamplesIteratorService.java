package uk.ac.ebi.biosamples.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biosamples.model.entities.BioSamplesIterator;
import uk.ac.ebi.biosamples.model.entities.Group;
import uk.ac.ebi.biosamples.model.entities.Sample;

import java.net.URI;

@Service
public class BioSamplesIteratorService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RelationsService relationsService;

    @Value("${biosamples.api.root:'http://www.ebi.ac.uk/biosamples/api'}")
    private String apiRoot;

    public BioSamplesIterator<Sample> getSamplesIterator() {

        BioSamplesIterator<Sample> it = new BioSamplesIterator<>(restTemplate,
                relationsService,
                URI.create(apiRoot + "/samples/"),
                new ParameterizedTypeReference<PagedResources<Resource<Sample>>>(){});
        it.initialize();
        return it;
    }

    public BioSamplesIterator<Sample> getSamplesIterator(String samplesStartingPage) {
        BioSamplesIterator<Sample> it = new BioSamplesIterator<>(restTemplate,
                relationsService,
                URI.create(samplesStartingPage),
                new ParameterizedTypeReference<PagedResources<Resource<Sample>>>(){});
        it.initialize();
        return it;
    }

    public BioSamplesIterator<Group> getGroupIterator() {
       BioSamplesIterator<Group> it = new BioSamplesIterator<>(restTemplate,
               relationsService,
               URI.create(apiRoot + "/groups"),
               new ParameterizedTypeReference<PagedResources<Resource<Group>>>() {});
       it.initialize();
       return it;
    }

    public BioSamplesIterator<Group> getGroupIterator(String groupsStartingPage) {
        BioSamplesIterator<Group> it = new BioSamplesIterator<>(restTemplate,
                relationsService,
                URI.create(groupsStartingPage),
                new ParameterizedTypeReference<PagedResources<Resource<Group>>>() {});
        it.initialize();
        return it;
    }
}
