package uk.ac.ebi.biosamples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.hateoas.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.biosamples.model.entities.BioSamplesIterator;
import uk.ac.ebi.biosamples.model.entities.Group;
import uk.ac.ebi.biosamples.model.entities.Sample;
import uk.ac.ebi.biosamples.model.enums.EntityType;
import uk.ac.ebi.biosamples.service.SamplesResourceService;

import java.util.HashSet;
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
    private SamplesResourceService service;

    @Test
    public void getFirstSamplesPage() {
        BioSamplesIterator<Sample> it = service.getSamplesIterator();
        assertThat(!it.getStatus().hasLink("prev")).withFailMessage("First sample page should not have a prev page");
    }

    @Test
    public void checkPageSize() {
        SamplesResourceService.URIBuilder builder = service
                .getURIBuilder(EntityType.SAMPLES)
                .startAtPage(10)
                .withPageSize(50);
        BioSamplesIterator<Sample> it = service.getSamplesIterator(builder.build());
        assertThat(it.getStatus().getMetadata().getSize()).isEqualTo(50);
        assertThat(it.getStatus().getMetadata().getNumber()).isEqualTo(10);
    }

    @Test
    public void getLastSamplesPage() {
        SamplesResourceService.URIBuilder builder = service
                .getURIBuilder(EntityType.SAMPLES)
                .startAtPage(106771)
                .withPageSize(50);
        BioSamplesIterator<Sample> lastPageIterator = service.getSamplesIterator(builder.build());
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

    @Test
    public void checkCorrectPage() {
        SamplesResourceService.URIBuilder builder = service
                .getURIBuilder(EntityType.SAMPLES)
                .startAtPage(1);
        BioSamplesIterator<Sample> it  = service.getSamplesIterator(builder.build());
        assertThat(it.getStatus().getMetadata().getNumber()).isEqualTo(1);
    }

    @Test
    public void checkDifferentPageResults() {
        int numToCheck = 10000;
        SamplesResourceService.URIBuilder builder = service
                .getURIBuilder(EntityType.SAMPLES)
                .startAtPage(0)
                .withPageSize(1000);
        BioSamplesIterator<Sample> it = service.getSamplesIterator(builder.build());

        builder = service.getURIBuilder(EntityType.SAMPLES)
                         .startAtPage(10)
                         .withPageSize(1000);
        BioSamplesIterator<Sample> it2 = service.getSamplesIterator(builder.build());
        Set accessions1 = Stream.generate(it::next).limit(numToCheck).map(sample -> sample.getContent().getAccession()).collect(Collectors.toSet());
        Set accessions2 = Stream.generate(it2::next).limit(numToCheck).map(sample -> sample.getContent().getAccession()).collect(Collectors.toSet());
        Set intersection = new HashSet<>(accessions1);
        Set union        = new HashSet(accessions1);
        union.addAll(accessions2);
        intersection.retainAll(accessions2);
        assertThat(intersection.isEmpty());
        assertThat(union.size()).isEqualTo(numToCheck*2);
    }


}
