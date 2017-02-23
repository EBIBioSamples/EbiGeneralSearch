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
import uk.ac.ebi.spot.biosamples.Model.Entities.Sample;
import uk.ac.ebi.spot.biosamples.Model.Relations.BioSamplesRelation;
import uk.ac.ebi.spot.biosamples.Model.Relations.BioSamplesRelationType;
import uk.ac.ebi.spot.biosamples.Service.BioSamplesIteratorService;
import uk.ac.ebi.spot.biosamples.Service.RelationsService;
import uk.ac.ebi.spot.biosamples.Service.SamplesResourceService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
        initializers = ConfigFileApplicationContextInitializer.class)
public class BioSamplesTests {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BioSamplesIteratorService samplesIteratorService;

    @Autowired private SamplesResourceService samplesResourceService;

    @Autowired private RelationsService relationsService;

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
        Sample sample = samplesResourceService.getSample(accession).getContent();
        assertThat(sample).isNotNull().hasFieldOrPropertyWithValue("accession",accession);
    }

    @Test
    public void relationIsEmpty() {
            ResponseEntity<PagedResources<Resource<BioSamplesRelation>>> re = restTemplate.exchange(
                    emptySampleRelations,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<PagedResources<Resource<BioSamplesRelation>>>(){});
            assertTrue("Response must be 200", re.getStatusCode().is2xxSuccessful());
            assertTrue("Content should be empty", re.getBody().getContent().isEmpty());
    }

    @Test
    public void relationIsNotEmpty() {
        ResponseEntity<Resources<Resource<BioSamplesRelation>>> re = restTemplate.exchange(
                nonEmptySampleRelation,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Resources<Resource<BioSamplesRelation>>>(){});
        assertTrue("Response Status Code should be 200", re.getStatusCode().is2xxSuccessful());
        assertNotNull("Content should not be null",re.getBody());
        assertTrue("Content should not be empty", !re.getBody().getContent().isEmpty());
    }

    @Test
    public void groupRelationNotEmpty() {
        String groupUrl = "https://www.ebi.ac.uk/biosamples/api/samplesrelations/SAME1422202/groups";
        ResponseEntity<Resources<Resource<BioSamplesRelation>>> re = restTemplate.exchange(
                groupUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Resources<Resource<BioSamplesRelation>>>(){});
        assertTrue("Response Status Code should be 200", re.getStatusCode().is2xxSuccessful());
        assertNotNull("Content should not be null",re.getBody());
        assertTrue("Content should not be empty", !re.getBody().getContent().isEmpty());
        BioSamplesRelation rel = re.getBody().getContent().stream().findFirst().get().getContent();
        assertThat(rel.getRelationIdentifier()).isEqualTo("SAMEG159724");

    }

    @Test
    public void relationsServiceGetAllRelations() {
        String accession = "SAMEA2590966";
//        Sample sample = samplesService.getSample(accession);
        Map<BioSamplesRelationType, List<BioSamplesRelation>> samplesRelations = relationsService.getSampleRelations(accession);
        assertThat(samplesRelations).isNotNull();
        Set<BioSamplesRelationType> relationTypes = samplesRelations.keySet();
        assertThat(relationTypes).containsAll(Arrays.asList(BioSamplesRelationType.values()));
    }




}
