package uk.ac.ebi.spot.biosamples.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Component
public class SamplesIterator implements Iterator<Sample>{

    @Autowired
    RestTemplate restTemplate;

    private static final String DEFAULT_URL = "https://www.ebi.ac.uk/biosamples/api/samples/";
    private String baseUrl, currentUrl;
    private List<Sample> currentCollection;


    public SamplesIterator(String baseUrl) {
       this.baseUrl = baseUrl;
       this.currentUrl = baseUrl;
       this.currentCollection = getNextSamplesBatch();

    }

    public SamplesIterator() {
        this(DEFAULT_URL);
    }

    private List<Sample> getNextSamplesBatch() {
        ResponseEntity
        Collection<Sample>
    }


    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Sample next() {
        return null;
    }
}
