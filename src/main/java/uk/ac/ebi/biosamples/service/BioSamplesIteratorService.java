package uk.ac.ebi.biosamples.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biosamples.model.entities.Group;
import uk.ac.ebi.biosamples.model.entities.Sample;
import uk.ac.ebi.biosamples.model.util.PagedResourceIterator;

import java.net.URI;
import java.util.Iterator;

@Service
public class BioSamplesIteratorService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RelationsService relationsService;

    @Value("${biosamples.api.root:'http://www.ebi.ac.uk/biosamples/api'}")
    private String apiRoot;

    public Iterator<Resource<Sample>> getSamplesIterator() {

        PagedResourceIterator<Sample> it = new PagedResourceIterator<>(restTemplate,
                relationsService,
                URI.create(apiRoot + "/samples/"),
                new ParameterizedTypeReference<PagedResources<Resource<Sample>>>(){});
        return it;
    }

    public Iterator<Resource<Sample>> getSamplesIterator(String samplesStartingPage) {
        PagedResourceIterator<Sample> it = new PagedResourceIterator<>(restTemplate,
                relationsService,
                URI.create(samplesStartingPage),
                new ParameterizedTypeReference<PagedResources<Resource<Sample>>>(){});
        return it;
    }

    public Iterator<Resource<Group>> getGroupIterator() {
       PagedResourceIterator<Group> it = new PagedResourceIterator<>(restTemplate,
               relationsService,
               URI.create(apiRoot + "/groups"),
               new ParameterizedTypeReference<PagedResources<Resource<Group>>>() {});
       return it;
    }

    public Iterator<Resource<Group>> getGroupIterator(String groupsStartingPage) {
        PagedResourceIterator<Group> it = new PagedResourceIterator<>(restTemplate,
                relationsService,
                URI.create(groupsStartingPage),
                new ParameterizedTypeReference<PagedResources<Resource<Group>>>() {});
        return it;
    }
}
