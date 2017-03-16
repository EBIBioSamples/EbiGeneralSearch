package uk.ac.ebi.biosamples.model.util;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.biosamples.service.RelationsService;

import java.net.URI;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PagedResourceIterator<E> implements Iterator<Resource<E>> {

    private RestTemplate restTemplate;
    private RelationsService relationsService;
    private URI baseUrl;
    private PagedResources<Resource<E>> currentPage;
    private Future<PagedResources<Resource<E>>> nextPage;
    private ParameterizedTypeReference<PagedResources<Resource<E>>> parameterizedTypeReference;
    private Iterator<Resource<E>> currentCollectionIterator = null;
    private Boolean initialized;


    public PagedResourceIterator(RestTemplate restTemplate,
                                 RelationsService relationsService,
                                 URI baseUrl,
                                 ParameterizedTypeReference<PagedResources<Resource<E>>> type) {

        assert (baseUrl != null);
        assert (restTemplate != null);
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.parameterizedTypeReference = type;
        this.initialized = false;
        this.relationsService = relationsService;
    }

    private void initialize() throws HttpStatusCodeException, UnsupportedOperationException {
        this.updateStatusWith(baseUrl);
        this.initialized = true;
    }

    /**
     * Set the current page and iterator using the url provided and try to fetch the next page
     * @param url
     */
    private void updateStatusWith(URI targetPageUrl) {
        if (targetPageIsNextPage(targetPageUrl)) {
            try {
                this.currentPage = this.nextPage.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            this.nextPage = null;
        } else {
            try {
                this.currentPage = getNextSamplesPage(targetPageUrl).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

        }

        if (this.currentPage.getNextLink() != null) {
            // If there's a next page start a thread to fetch it
            URI nextPageURI = URI.create(this.currentPage.getNextLink().getHref());
            this.nextPage = getNextSamplesPage(nextPageURI);
        }

        this.currentCollectionIterator = this.currentPage.getContent().iterator();
    }

    /**
     * Check if the passed URI corresponds to the saved next page URI
     * @param targetPageUri
     * @return
     */
    private boolean targetPageIsNextPage(URI targetPageUri) {
        try {
            return this.nextPage != null &&
                    URI.create(this.nextPage.get(1, TimeUnit.SECONDS).getLink("self").getHref()).equals(targetPageUri);
        } catch ( TimeoutException  e) {
            return false;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    private Future<PagedResources<Resource<E>>> getNextSamplesPage(URI url) {
        ResponseEntity<PagedResources<Resource<E>>> re = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                parameterizedTypeReference);
        return new AsyncResult<>(re.getBody());
    }


    @Override
    public boolean hasNext() {
        if (!this.initialized) {
            this.initialize();
        }
        return this.currentCollectionIterator.hasNext() || this.currentPage.hasLink("next");
    }

    @Override
    public Resource<E> next() {

        if (!initialized) {
            this.initialize();
        }

        if (this.currentCollectionIterator.hasNext()) {
            return this.currentCollectionIterator.next();
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
        if (!initialized) {
            this.initialize();
        }
        return this.currentPage;
    }

}