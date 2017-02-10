package uk.ac.ebi.spot.biosamples;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.hateoas.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.spot.biosamples.Model.Sample;
import uk.ac.ebi.spot.biosamples.Model.SamplesIterator;
import uk.ac.ebi.spot.biosamples.Service.SamplesIteratorService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
        initializers = ConfigFileApplicationContextInitializer.class)
public class SamplesIteratorTest {

    @Autowired
    private SamplesIteratorService service;

    @Test
    @Ignore
    public void getFirstSamplesPage() {
        SamplesIterator it = service.getSamplesIterator();
        assertThat(!it.getStatus().hasLink("prev")).withFailMessage("First sample page should not have a prev page");
    }

    @Test
    @Ignore
    public void getLastSamplesPage() {
        SamplesIterator lastPageIterator = service.getSamplesIterator("https://www.ebi.ac.uk/biosamples/api/samples?page=106771&size=50");
        assertThat(!lastPageIterator.getStatus().hasLink("next")).withFailMessage("Last sample page should not have a next page");
    }

    @Test
    public void getTenSamples() {
        SamplesIterator it = service.getSamplesIterator();
        List<Resource<Sample>> samples = Stream.generate(it::next).limit(10).collect(Collectors.toList());
        assertThat(samples).hasSize(10);

    }

    @Test
    public void getHundredSamples() {
        SamplesIterator it = service.getSamplesIterator();
        List<Resource<Sample>> samples = Stream.generate(it::next).limit(100).collect(Collectors.toList());
        assertThat(samples).hasSize(100);
    }
}
