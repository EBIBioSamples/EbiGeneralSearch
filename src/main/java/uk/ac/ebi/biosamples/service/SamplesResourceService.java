package uk.ac.ebi.biosamples.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biosamples.model.entities.Group;
import uk.ac.ebi.biosamples.model.entities.Sample;
import uk.ac.ebi.biosamples.model.enums.EntityType;
import uk.ac.ebi.biosamples.model.enums.Sort;
import uk.ac.ebi.biosamples.model.util.PagedResourceIterator;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


@Service
public class SamplesResourceService {


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RelationsService relationService;

    @Value("${biosamples.api.root:'http://www.ebi.ac.uk/biosamples/api'}")
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
        }
        return null;
    }

    public Iterator<Resource<Sample>> getSamplesIterator() {

        PagedResourceIterator<Sample> it = new PagedResourceIterator<>(restTemplate,
                relationService,
                URI.create(apiRoot + "/samples/"),
                new ParameterizedTypeReference<PagedResources<Resource<Sample>>>(){});
        return it;
    }

    public Iterator<Resource<Sample>> getSamplesIterator(URI specificUrl) {
        return new PagedResourceIterator<>(restTemplate,
                relationService,
                specificUrl,
                new ParameterizedTypeReference<PagedResources<Resource<Sample>>>(){});
    }

    public Iterator<Resource<Group>> getGroupIterator() {
        PagedResourceIterator<Group> it = new PagedResourceIterator<>(restTemplate,
                relationService,
                URI.create(apiRoot + "/groups"),
                new ParameterizedTypeReference<PagedResources<Resource<Group>>>() {});
        return it;
    }

    public Iterator<Resource<Group>> getGroupIterator(URI specificUrl) {
        PagedResourceIterator<Group> it = new PagedResourceIterator<>(restTemplate,
                relationService,
                specificUrl,
                new ParameterizedTypeReference<PagedResources<Resource<Group>>>() {});
        return it;
    }

    public URIBuilder getURIBuilder(EntityType type) {
        return new URIBuilder(apiRoot, type);
    }


    public static class URIBuilder {

        private final int DEFAULT_STARTING_PAGE = 0;
        private final int DEFAULT_PAGE_SIZE = 50;
        private final Sort DEFAULT_SORT = Sort.ASCENDING;

        private Sort sortMethod;
        private int startingPage;
        private int pageSize;
        private EntityType entityType;
        private String apiRoot;

        URIBuilder(String apiRoot, EntityType type) {
           startingPage = DEFAULT_STARTING_PAGE;
           pageSize = DEFAULT_PAGE_SIZE;
           sortMethod = DEFAULT_SORT;
           this.entityType = type;
           this.apiRoot = apiRoot;
       }

       public URIBuilder startAtPage(int startPage) {
           this.startingPage = startPage;
           return this;
       }

       public URIBuilder withPageSize(int pageSize) {
           this.pageSize = pageSize;
           return this;
       }

       public URIBuilder setSort(Sort sortMethod) {
           this.sortMethod = sortMethod;
           return this;
       }

       public URI build() {
           Map<String, Object> parameters = new HashMap<>();
           parameters.put("page", this.startingPage);
           parameters.put("size", this.pageSize);
           parameters.put("sort", this.sortMethod.getType());
           parameters.put("entity", this.entityType.getType());
           return new UriTemplate(apiRoot + "/{entity}?page={page}&size={size}&sort={sort}").expand(parameters);
       }
    }


}
