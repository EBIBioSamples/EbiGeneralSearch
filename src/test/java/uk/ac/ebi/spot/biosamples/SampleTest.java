package uk.ac.ebi.spot.biosamples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.*;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.biosamples.Model.Sample;
import uk.ac.ebi.spot.biosamples.Model.SamplesRelation;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest
@ContextConfiguration(classes = Application.class,
        initializers = ConfigFileApplicationContextInitializer.class)
public class SampleTest {

    @Autowired
    private RestTemplate restTemplate;

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

    @Test
    public void traversonDerivedFrom() throws URISyntaxException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("accession", "SAMEA2590966");
        UriTemplate template = new UriTemplate("http://www.ebi.ac.uk/biosamples/api/samples/{accession}");

        Traverson traverson = new Traverson(template.expand(parameters), MediaTypes.HAL_JSON);
        ParameterizedTypeReference<Resources<SamplesRelation>> rel = new ParameterizedTypeReference<Resources<SamplesRelation>>() {};
        Resources<SamplesRelation> relations = traverson.follow("relations","derivedFrom").toObject(rel);
        assertNotNull("Relations should not be null", relations);
        assertThat(relations.getContent().size()).isEqualTo(1).withFailMessage("Derived from size should be 1");
        SamplesRelation content = relations.getContent().stream().findFirst().get();
        assertThat(content.getAccession()).isEqualTo("SAMEA2590957").withFailMessage("Derived from sample should have accession SAMEA2590957");
    }

    @Test
    public void traversonDerivedFromAccession() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("accession", "SAMEA2590966");
        UriTemplate template = new UriTemplate("http://www.ebi.ac.uk/biosamples/api/samples/{accession}");

        Traverson traverson = new Traverson(template.expand(parameters), MediaTypes.HAL_JSON);
        String accession = traverson.follow("relations","derivedFrom").toObject("$._embedded.samplesrelations[0].accession");
        assertThat(accession)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("SAMEA2590957");
    }

    @Test(expected = IllegalStateException.class)
    public void traversonNonExistingRelation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("accession", "SAMEA2590966");
        UriTemplate template = new UriTemplate("http://www.ebi.ac.uk/biosamples/api/samples/{accession}");

        Traverson traverson = new Traverson(template.expand(parameters), MediaTypes.HAL_JSON);
        traverson.follow("relations","deriveInto").asLink();
    }

    @Test
    public void traversonCheckDerivation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("accession", "SAMEA2590957");
        UriTemplate template = new UriTemplate("http://www.ebi.ac.uk/biosamples/api/samples/{accession}");

        Traverson traverson = new Traverson(template.expand(parameters), MediaTypes.HAL_JSON);
        Resources<SamplesRelation> deriveFrom = traverson.follow("relations","derivedFrom").toObject(new ParameterizedTypeReference<Resources<SamplesRelation>>() {});
        assertThat(deriveFrom).isNotNull();
        assertThat(deriveFrom.getContent()).isNotEmpty().hasSize(1);
        String accession = deriveFrom.iterator().next().getAccession();
        assertThat(accession).isEqualTo("SAMEA2591001").withFailMessage("Sample should derive from SAMEA2591001");

        traverson = new Traverson(template.expand(parameters), MediaTypes.HAL_JSON);
        Resources<SamplesRelation> deriveTo = traverson.follow("relations", "derivedTo").toObject(new ParameterizedTypeReference<Resources<SamplesRelation>>() {});
        assertThat(deriveTo).isNotNull();
        assertThat(deriveTo).isNotEmpty();
        Collection<String> deriveToSamplesRelation = deriveTo.getContent().stream().map(SamplesRelation::getAccession).collect(Collectors.toList());
        assertThat(deriveToSamplesRelation).contains("SAMEA2672925", "SAMEA2590900", "SAMEA2590887").withFailMessage("Sample is missing some deriveTo relations");


    }
}
