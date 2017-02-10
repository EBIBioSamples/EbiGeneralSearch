package uk.ac.ebi.spot.biosamples;

import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.biosamples.Model.Sample;
import uk.ac.ebi.spot.biosamples.Model.SamplesIterator;
import uk.ac.ebi.spot.biosamples.Model.SamplesRelation;
import uk.ac.ebi.spot.biosamples.Service.SamplesIteratorService;
import uk.ac.ebi.spot.biosamples.Service.SamplesService;
import uk.ac.ebi.spot.biosamples.Service.xml.SampleToEntryConverter;
import uk.ac.ebi.spot.biosamples.Service.xml.XmlUtilities;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@SpringBootApplication
public class Application {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SamplesService samplesService;

    @Autowired private SamplesIteratorService samplesIteratorService;

    private static final Logger log = LoggerFactory.getLogger(Application.class);

	private static final List<String> wantedRelations = Arrays.asList("derivedTo", "derivedFrom", "groups");

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}


	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		return args -> {

			SamplesIterator iterator = samplesIteratorService.getSamplesIterator();
			List<Resource<Sample>> samples = Stream.generate(iterator::next).limit(100).collect(Collectors.toList());
			for(Resource<Sample> sample: samples) {
			    Element entry = SampleToEntryConverter.produceEntryFor(sample.getContent());
			    log.info(XmlUtilities.prettyPrint(entry));
            }


//			List<Element> entries = new ArrayList<>();
//			samples.forEach(sample -> {
//				Element entry = SampleToEntryConverter.produceEntryFor(sample);
//				entries.add(entry);
//			});
//			Document exportDocument = produceDocumentForEntries(entries);
//			log.info(XmlUtilities.prettyPrint(exportDocument));

		};
	}
	private Document produceDocumentForEntries(Collection<Element> entriesCollection) {
		Element database = new Element("database");
		SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");

		List<Element> databaseContent = new ArrayList<>();
		databaseContent.add(new Element("name").setText("biosamples"));
		databaseContent.add(new Element("description").setText(""));
		databaseContent.add(new Element("release").setText("1"));
		databaseContent.add(new Element("release_date").setText(dateformat.format(new Date())));
		databaseContent.add(new Element("entry_count").setText(Integer.toString(entriesCollection.size())));
		databaseContent.add(new Element("entries").addContent(entriesCollection));

		database.addContent(databaseContent);

		Document xmlDoc = new Document(new Element("xml"));
		xmlDoc.setContent(database);
        return xmlDoc;
	}


	private List<SamplesRelation> getSampleRelations(RestTemplate restTemplate, String relationLink) {
        ResponseEntity<Resource<SamplesRelation>> re = restTemplate.exchange(
                relationLink,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Resource<SamplesRelation>>(){});
        if (re.getStatusCode().is2xxSuccessful()) {
            List<Link> filteredLinks = re.getBody().getLinks().stream().filter(type -> wantedRelations.contains(type.getRel())).collect(Collectors.toList());
            for(Link link: filteredLinks) {
                Collection<Resource<SamplesRelation>> outRelations = readRelation(restTemplate, link.getHref());
            }
        }
        return null;
    }

	private Collection<Resource<SamplesRelation>> readRelation(RestTemplate restTemplate, String relationLink) {
        ResponseEntity<PagedResources<Resource<SamplesRelation>>> re = restTemplate.exchange(
                relationLink,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PagedResources<Resource<SamplesRelation>>>(){});
        if (re.getStatusCode().is2xxSuccessful()) {
            if(re.getBody().getContent() == null) {
                log.info("Occhio");
            }
            return re.getBody().getContent();

        }
        return Collections.emptyList();
    }


	
}

