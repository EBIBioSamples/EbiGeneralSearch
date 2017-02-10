package uk.ac.ebi.spot.biosamples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.biosamples.Model.Sample;
import uk.ac.ebi.spot.biosamples.Model.SamplesRelation;
import uk.ac.ebi.spot.biosamples.Service.SamplesIteratorService;
import uk.ac.ebi.spot.biosamples.Service.SamplesService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
        initializers = ConfigFileApplicationContextInitializer.class)
public class SampleTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SamplesIteratorService samplesIteratorService;

    @Autowired private SamplesService samplesService;

    private static final String sampleUrl = "https://www.ebi.ac.uk/biosamples/api/samples/SAMN00236429";
    private static final String emptySampleRelations = "https://www.ebi.ac.uk/biosamples/api/samplesrelations/SAME1422202/derivedFrom";
    private static final String nonEmptySampleRelation = "http://www.ebi.ac.uk/biosamples/api/samplesrelations/SAMEA2629457/derivedTo";

    @Test
    public void sampleExistsTest() {
        Sample sample = restTemplate.getForObject(
                sampleUrl,
                Sample.class);
        assertNotNull("Sample should not be null",sample);
    }

    @Test
    public void sampleWithAccession() {
        String accession = "SAMN00236429";
        Sample sample = samplesService.getSample(accession);
        assertThat(sample).isNotNull().hasFieldOrPropertyWithValue("accession",accession);
    }

    @Test
    public void relationIsEmpty() {
            ResponseEntity<PagedResources<Resource<SamplesRelation>>> re = restTemplate.exchange(
                    emptySampleRelations,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PagedResources<Resource<SamplesRelation>>>(){});
            assertTrue("Response must be 200", re.getStatusCode().is2xxSuccessful());
            assertTrue("Content should be empty", re.getBody().getContent().isEmpty());
    }

    @Test
    public void relationIsNotEmpty() {
        ResponseEntity<Resources<Resource<SamplesRelation>>> re = restTemplate.exchange(
                nonEmptySampleRelation,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Resources<Resource<SamplesRelation>>>(){});
        assertTrue("Response Status Code should be 200", re.getStatusCode().is2xxSuccessful());
        assertNotNull("Content should not be null",re.getBody());
        assertTrue("Content should not be empty", !re.getBody().getContent().isEmpty());
    }

    @Test
    public void groupRelationNotEmpty() {
        String groupUrl = "https://www.ebi.ac.uk/biosamples/api/samplesrelations/SAME1422202/groups";
        ResponseEntity<Resources<Resource<SamplesRelation>>> re = restTemplate.exchange(
                groupUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Resources<Resource<SamplesRelation>>>(){});
        assertTrue("Response Status Code should be 200", re.getStatusCode().is2xxSuccessful());
        assertNotNull("Content should not be null",re.getBody());
        assertTrue("Content should not be empty", !re.getBody().getContent().isEmpty());
        SamplesRelation rel = re.getBody().getContent().stream().findFirst().get().getContent();
        assertThat(rel.getAccession()).isEqualTo("SAMEG159724");

    }



}
