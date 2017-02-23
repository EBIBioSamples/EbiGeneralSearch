package uk.ac.ebi.spot.biosamples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.hateoas.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.spot.biosamples.Model.Entities.BioSamplesIterator;
import uk.ac.ebi.spot.biosamples.Model.Entities.Group;
import uk.ac.ebi.spot.biosamples.Model.Entities.Sample;
import uk.ac.ebi.spot.biosamples.Service.BioSamplesIteratorService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class,
        initializers = ConfigFileApplicationContextInitializer.class)
public class BioSamplesIteratorTests {

    @Autowired
    private BioSamplesIteratorService service;

    @Test
    public void getFirstSamplesPage() {
        BioSamplesIterator<Sample> it = service.getSamplesIterator();
        assertThat(!it.getStatus().hasLink("prev")).withFailMessage("First sample page should not have a prev page");
    }

    @Test
    public void getLastSamplesPage() {
        BioSamplesIterator<Sample> lastPageIterator = service.getSamplesIterator("https://www.ebi.ac.uk/biosamples/api/samples?page=106771&size=50");
        assertThat(!lastPageIterator.getStatus().hasLink("next")).withFailMessage("Last sample page should not have a next page");
    }

    @Test
    public void getTenSamples() {
        BioSamplesIterator<Sample> it = service.getSamplesIterator();
        List<Resource<Sample>> samples = Stream.generate(it::next).limit(10).collect(Collectors.toList());
        assertThat(samples).hasSize(10);

    }

    @Test
    public void getHundredSamples() {
        BioSamplesIterator<Sample> it = service.getSamplesIterator();
        List<Resource<Sample>> samples = Stream.generate(it::next).limit(100).collect(Collectors.toList());
        assertThat(samples).hasSize(100);
    }

    @Test
    public void getDifferentSamples() {
        BioSamplesIterator<Sample> it = service.getSamplesIterator();
        List<Resource<Sample>> samples = Stream.generate(it::next)
                .limit(1000)
                .collect(Collectors.toList());
        assertThat(samples).hasSize(1000);
        Set<String> groupAccessions = samples.stream().map(sample -> sample.getContent().getAccession()).collect(Collectors.toSet());
        assertThat(groupAccessions).hasSize(1000);
    }

    @Test
    public void groupPageContentNonEmpty() {
        BioSamplesIterator<Group> it = service.getGroupIterator();
        Resource<Group> group = it.next();
        assertThat(group).isNotNull().withFailMessage("Resource should not be null");
        assertThat(group.getContent()).isNotNull().withFailMessage("Resource content should not be null");
    }

    @Test
    public void getDifferentGroups() {
        BioSamplesIterator<Group> it = service.getGroupIterator();
        List<Resource<Group>> groups = Stream.generate(it::next)
                .limit(1000)
                .collect(Collectors.toList());
        assertThat(groups).hasSize(1000);
        Set<String> groupAccessions = groups.stream().map(group -> group.getContent().getAccession()).collect(Collectors.toSet());
        assertThat(groupAccessions).hasSize(1000);

    }

//    @Test
//    public void getFirstSamplesPage() {
//        BioSamplesIterator<Sample> it = service.getSamplesIterator();
//        assertThat(!it.getStatus().hasLink("prev")).withFailMessage("First sample page should not have a prev page");
//    }
//
//    @Test
//    public void getLastSamplesPage() {
//        BioSamplesIterator<Sample> lastPageIterator = service.getSamplesIterator("https://www.ebi.ac.uk/biosamples/api/samples?page=106771&size=50");
//        assertThat(!lastPageIterator.getStatus().hasLink("next")).withFailMessage("Last sample page should not have a next page");
//    }
//
//    @Test
//    public void getTenSamples() {
//        BioSamplesIterator<Sample> it = service.getSamplesIterator();
//        List<Resource<Sample>> samples = Stream.generate(it::next).limit(10).collect(Collectors.toList());
//        assertThat(samples).hasSize(10);
//
//    }
//
//    @Test
//    public void getHundredSamples() {
//        BioSamplesIterator<Sample> it = service.getSamplesIterator();
//        List<Resource<Sample>> samples = Stream.generate(it::next).limit(100).collect(Collectors.toList());
//        assertThat(samples).hasSize(100);
//    }
//
}
