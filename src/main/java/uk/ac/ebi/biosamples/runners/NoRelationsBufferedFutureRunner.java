package uk.ac.ebi.biosamples.runners;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biosamples.model.entities.Sample;
import uk.ac.ebi.biosamples.model.enums.EntityType;
import uk.ac.ebi.biosamples.model.util.ExecutionInfo;
import uk.ac.ebi.biosamples.model.util.RunnerOptions;
import uk.ac.ebi.biosamples.service.RelationsService;
import uk.ac.ebi.biosamples.service.SamplesResourceService;
import uk.ac.ebi.biosamples.service.XmlService;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("Duplicates")
@Component
public class NoRelationsBufferedFutureRunner implements ApplicationRunner {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private XmlService xmlService;
    private SamplesResourceService samplesResourceService;
    private RelationsService relationsService;
    private XMLStreamWriter writer;
    private BlockingQueue<Resource<Sample>> exceptionQueue;
    private ConcurrentHashMap<Resource<Sample>, AtomicInteger> exceptionMap;
    private ExecutionInfo taskInfo;


    @Value("${resource.retrieve.max.error:10}")
    int maxErrorPerResource;

    @Value("${resource.retrieve.threads.count:16}")
    int threadsCount;

    public NoRelationsBufferedFutureRunner(XmlService xmlService,
                                           RelationsService relationsService,
                                           SamplesResourceService samplesService)
    {
        this.relationsService = relationsService;
        this.xmlService = xmlService;
        this.samplesResourceService = samplesService;
        this.writer = null;
        this.exceptionMap = new ConcurrentHashMap<>();
        this.exceptionQueue = new LinkedBlockingQueue<>();
        this.taskInfo = new ExecutionInfo();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        log.info("EBI General Search exporter started - Runner: {}", this.getClass().toGenericString());

        RunnerOptions options = RunnerOptions.from(args);

        SamplesResourceService.URIBuilder uriBuilder = samplesResourceService
                .getURIBuilder(EntityType.SAMPLES)
                .startAtPage(options.getStartPage())
                .withPageSize(options.getSize());
        Iterator<Resource<Sample>> pagedResourceIterator = samplesResourceService.getSamplesIterator(uriBuilder.build());


        Path path = options.getFilename();
        ExecutorService executor = null;

        try {
            writer = initWriter(path);
            writer = startDocument(writer);
            executor = Executors.newFixedThreadPool(threadsCount);

            // First submission
            while(pagedResourceIterator.hasNext() && taskInfo.getSubmitted() < options.getTotal()) {
                Resource<Sample> sample = pagedResourceIterator.next();
                try {
                    writeResourceToFile(sample, writer);
                    taskInfo.incrementCompleted(1);
                } catch (XMLStreamException e) {
                    log.error("An error occured while writing {} to file",
                            sample.getContent().getAccession(),
                            e);
                }
                taskInfo.incrementSubmitted(1);
                log.debug("Submitted {} tasks", taskInfo.getSubmitted());
                if (taskInfo.getSubmitted() % 1000 == 0) {
                    log.info("Successfully submitted {} tasks so far", taskInfo.getSubmitted());
                }
            }

            // Checking for exception queue
            log.debug("All task finished");
            closeDocument(writer);

        } catch (XMLStreamException | IOException e) {
            log.error("An error occurred while creating the file", e);
        } finally {
            if (writer != null)
                writer.close();
            if (executor != null && !executor.isShutdown()) {
                executor.shutdownNow();
            }

            log.info("EBI general search exporter finished");
            log.info("Total task submitted {}; Total task completed {}; Total task failed {}",
                    taskInfo.getSubmitted(), taskInfo.getCompleted(), taskInfo.getErrors());


        }
    }

    private XMLStreamWriter initWriter(Path documentPath) throws XMLStreamException, IOException {
        log.debug("Initializing XML document");
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty("escapeCharacters", false);
        return factory.createXMLStreamWriter(new FileWriter(documentPath.toFile()));
    }

    private void writeResourceToFile(Resource<Sample> sampleResource, XMLStreamWriter outputWriter) throws XMLStreamException {
        log.debug("Writing resource {} to XML document", sampleResource.getContent().getAccession());
        Element sampleEntry = xmlService.getEntryForSample(sampleResource.getContent());
        outputWriter.writeCharacters(xmlService.prettyPrint(sampleEntry));
        outputWriter.writeCharacters("\n");
        outputWriter.flush();
    }

    private XMLStreamWriter startDocument(XMLStreamWriter writer) throws XMLStreamException {
        log.debug("Started to write the XML document");
        writer.writeStartDocument();
        writer.writeCharacters("\n");
        writer.writeStartElement("database");
        writer.writeCharacters("\n");
        writer.writeStartElement("entries");
        writer.writeCharacters("\n");
        writer.flush();
        return writer;
    }

    private XMLStreamWriter closeDocument(XMLStreamWriter writer) throws XMLStreamException {
        log.debug("Closing the XML document");
        writer.writeEndElement();
        writer.writeCharacters("\n");
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");

        List<Element> databaseContent = new ArrayList<>();
        databaseContent.add(new Element("name").setText("biosamples"));
        databaseContent.add(new Element("description").setText(""));
        databaseContent.add(new Element("release").setText("1"));
        databaseContent.add(new Element("release_date").setText(dateformat.format(new Date())));
        databaseContent.add(new Element("entry_count").setText(Integer.toString(taskInfo.getCompleted())));

        databaseContent.forEach(element -> {
            try {
                writer.writeCharacters(xmlService.prettyPrint(element));
                writer.writeCharacters("\n");
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        });
        writer.writeEndElement();
        writer.writeCharacters("\n");
        writer.writeEndDocument();
        writer.flush();
        log.debug("Finished to write the XML document");
        writer.close();
        log.debug("XML Document closed");

        return writer;
    }

}
