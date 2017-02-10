package uk.ac.ebi.spot.biosamples.Model;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SamplesIterator implements Iterator<Resource<Sample>> {

    private RestTemplate restTemplate;
    private static final String DEFAULT_URL = "https://www.ebi.ac.uk/biosamples/api/samples/";
    private String baseUrl;
    private PagedResources<Resource<Sample>> currentPage;
    private Iterator<Resource<Sample>> currentCollectionIterator;


    public SamplesIterator(RestTemplate restTemplate, String baseUrl) throws HttpStatusCodeException {

        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        try {
            updatePageStatusWith(baseUrl);
        } catch (HttpStatusCodeException e) {
            throw e;
        }
    }

    public SamplesIterator(RestTemplate restTemplate) {
        this(restTemplate, DEFAULT_URL);
    }

    private PagedResources<Resource<Sample>> getNextSamplesPage(String url) throws HttpStatusCodeException {
        ResponseEntity<PagedResources<Resource<Sample>>> re = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedResources<Resource<Sample>>>() {
                });
        if (re.getStatusCode().is2xxSuccessful()) {
            return re.getBody();
        } else {
            throw new HttpStatusCodeException(re.getStatusCode()) {
            };
        }
    }

    private void updatePageStatusWith(String url) throws HttpStatusCodeException {
        this.currentPage = getNextSamplesPage(baseUrl);
        this.currentCollectionIterator = this.currentPage.getContent().iterator();
    }

    @Override
    public boolean hasNext() {
        return this.currentCollectionIterator.hasNext() || this.currentPage.hasLink("next");
    }

    @Override
    public Resource<Sample> next() {
        if (this.currentCollectionIterator.hasNext()) {
            return this.currentCollectionIterator.next();
        } else {
            if (this.currentPage.hasLink("next")) {
                updatePageStatusWith(this.currentPage.getNextLink().getHref());
                return this.currentCollectionIterator.next();
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    public PagedResources<Resource<Sample>> getStatus() {
        return this.currentPage;
    }
}
