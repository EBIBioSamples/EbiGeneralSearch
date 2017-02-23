package uk.ac.ebi.spot.biosamples.Model.Entities;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.biosamples.Service.RelationsService;

import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class BioSamplesIterator<E extends BioSamplesEntity> implements Iterator<Resource<E>> {

    private RestTemplate restTemplate;
    private RelationsService relationsService;
    private URI baseUrl;
    private PagedResources<Resource<E>> currentPage;
    private ParameterizedTypeReference<PagedResources<Resource<E>>> parameterizedTypeReference;
    private Iterator<Resource<E>> currentCollectionIterator = null;
    private Boolean initialized;


    public BioSamplesIterator(RestTemplate restTemplate,
                              RelationsService relationsService,
                              URI baseUrl,
                              ParameterizedTypeReference<PagedResources<Resource<E>>> type) {

        assert(baseUrl != null);
        assert(restTemplate != null);
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.parameterizedTypeReference = type;
        this.initialized = false;
        this.relationsService = relationsService;
    }

    public void initialize() throws HttpStatusCodeException, UnsupportedOperationException{
        this.updateStatusWith(baseUrl);
        this.initialized = true;
    }

    private void updateStatusWith(URI url) throws HttpServerErrorException, UnsupportedOperationException {
        this.currentPage = getNextSamplesPage(url);
        this.currentCollectionIterator = this.currentPage.getContent().iterator();
    }

    private PagedResources<Resource<E>> getNextSamplesPage(URI url) throws HttpStatusCodeException, UnsupportedOperationException {
        ResponseEntity<PagedResources<Resource<E>>> re = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                parameterizedTypeReference);
        HttpStatus statusCode = re.getStatusCode();
        if (statusCode.is2xxSuccessful()) {
            return re.getBody();
        } else {
            if(statusCode.is4xxClientError()) throw new HttpClientErrorException(statusCode);
            else if(statusCode.is5xxServerError()) throw new HttpServerErrorException(statusCode);
            else throw new UnsupportedOperationException();
        }
    }


    @Override
    public boolean hasNext() {
        if(!this.initialized) {
            this.initialize();
        }
        return this.currentCollectionIterator.hasNext() || this.currentPage.hasLink("next");
    }

    @Override
    public Resource<E> next() {

        if(!initialized) {
            this.initialize();
        }

        if (this.currentCollectionIterator.hasNext()) {
            return this.currentCollectionIterator.next();
//            return getExtendedResource(nextResource);
        } else {
            if (this.currentPage.hasLink("next")) {
                updateStatusWith(URI.create(this.currentPage.getNextLink().getHref()));
                return this.next();
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    public PagedResources<Resource<E>> getStatus() {
        return this.currentPage;
    }

//    public Resource<E> getExtendedResource(Resource<E> originalResource) {
//        E content = originalResource.getContent();
//        Map<BioSamplesRelationType, List<Relation>> relations = null;
//        if (content.getEntityType().equals(Group.class)) {
//            relations = relationsService.getGroupsRelations(content.getAccession());
//        } else if (content.getEntityType().equals(Sample.class)) {
//            relations = relationsService.getSampleRelations(content.getAccession());
//        }
//
//        if(relations  != null) {
//           content.setRelations(relations);
//           return new Resource<>(content,originalResource.getLinks());
//        } else {
//            return originalResource;
//        }
//
//
//
//    }



}
