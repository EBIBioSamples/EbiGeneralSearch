package uk.ac.ebi.biosamples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.client.Traverson;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.biosamples.model.Relations.BioSamplesRelation;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class, initializers = ConfigFileApplicationContextInitializer.class)
public class TraversonTests {

    @Test
    public void traversonDerivedFrom() throws URISyntaxException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("accession", "SAMEA2590966");
        UriTemplate template = new UriTemplate("http://www.ebi.ac.uk/biosamples/api/samples/{accession}");

        Traverson traverson = new Traverson(template.expand(parameters), MediaTypes.HAL_JSON);
        ParameterizedTypeReference<Resources<BioSamplesRelation>> rel = new ParameterizedTypeReference<Resources<BioSamplesRelation>>() {};
        Resources<BioSamplesRelation> relations = traverson.follow("relations","derivedFrom").toObject(rel);
        assertNotNull("Relations should not be null", relations);
        assertThat(relations.getContent().size()).isEqualTo(1).withFailMessage("Derived from size should be 1");
        BioSamplesRelation content = relations.getContent().stream().findFirst().get();
        assertThat(content.getRelationIdentifier()).isEqualTo("SAMEA2590957").withFailMessage("Derived from sample should have accession SAMEA2590957");
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
        Resources<BioSamplesRelation> deriveFrom = traverson.follow("relations","derivedFrom").toObject(new ParameterizedTypeReference<Resources<BioSamplesRelation>>() {});
        assertThat(deriveFrom).isNotNull();
        assertThat(deriveFrom.getContent()).isNotEmpty().hasSize(1);
        String accession = deriveFrom.iterator().next().getRelationIdentifier();
        assertThat(accession).isEqualTo("SAMEA2591001").withFailMessage("Sample should derive from SAMEA2591001");

        traverson = new Traverson(template.expand(parameters), MediaTypes.HAL_JSON);
        Resources<BioSamplesRelation> deriveTo = traverson.follow("relations", "derivedTo").toObject(new ParameterizedTypeReference<Resources<BioSamplesRelation>>() {});
        assertThat(deriveTo).isNotNull();
        assertThat(deriveTo).isNotEmpty();
        Collection<String> deriveToSamplesRelation = deriveTo.getContent().stream().map(BioSamplesRelation::getRelationIdentifier).collect(Collectors.toList());
        assertThat(deriveToSamplesRelation).contains("SAMEA2672925", "SAMEA2590900", "SAMEA2590887").withFailMessage("Sample is missing some deriveTo relations");
    }

}
