package uk.ac.ebi.spot.biosamples.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.biosamples.Model.SamplesIterator;

@Service
public class SamplesIteratorService {

    @Autowired
    private RestTemplate restTemplate;

    public SamplesIterator getSamplesIterator() {
        return new SamplesIterator(restTemplate);
    }

    public SamplesIterator getSamplesIterator(String samplesStartingPage) {
       return new SamplesIterator(restTemplate, samplesStartingPage);
    }
}
