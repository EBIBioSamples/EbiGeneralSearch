package uk.ac.ebi.spot.biosamples.Model;

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

import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class BioSamplesIterator<E> implements Iterator<Resource<E>> {

    private RestTemplate restTemplate;
    private URI baseUrl;
    private PagedResources<Resource<E>> currentPage;
    private ParameterizedTypeReference<PagedResources<Resource<E>>> parameterizedTypeReference;
    private Iterator<Resource<E>> currentCollectionIterator = null;
    private Boolean initialized;


    public BioSamplesIterator(RestTemplate restTemplate, URI baseUrl, ParameterizedTypeReference<PagedResources<Resource<E>>> type) {

        assert(baseUrl != null);
        assert(restTemplate != null);
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.parameterizedTypeReference = type;
        this.initialized = false;
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
        } else {
            if (this.currentPage.hasLink("next")) {
                updateStatusWith(URI.create(this.currentPage.getNextLink().getHref()));
                return this.currentCollectionIterator.next();
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    public PagedResources<Resource<E>> getStatus() {
        return this.currentPage;
    }



}
