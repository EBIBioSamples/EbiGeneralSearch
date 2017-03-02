package uk.ac.ebi.biosamples.runners;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biosamples.model.entities.BioSamplesIterator;
import uk.ac.ebi.biosamples.model.entities.Sample;
import uk.ac.ebi.biosamples.model.enums.EntityType;
import uk.ac.ebi.biosamples.model.relations.BioSamplesRelation;
import uk.ac.ebi.biosamples.model.relations.BioSamplesRelationType;
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
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BufferedFutureRunner implements ApplicationRunner {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private XmlService xmlService;
    private SamplesResourceService samplesResourceService;
    private RelationsService relationsService;
    private XMLStreamWriter writer;
    private BlockingQueue<Resource<Sample>> exceptionQueue;
    private ConcurrentHashMap<Resource<Sample>, AtomicInteger> exceptionMap;
    private ExecutionInfo taskInfo;


    @Value("${resource.max.error:10}")
    int maxErrorPerResource;

    public BufferedFutureRunner(XmlService xmlService,
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

        log.debug("Starting BufferedFutureRunner");

        RunnerOptions options = RunnerOptions.from(args);

        SamplesResourceService.URIBuilder uriBuilder = samplesResourceService.getURIBuilder(EntityType.SAMPLES);
        uriBuilder.startAtPage(options.getStartPage()).withPageSize(options.getSize());
        BioSamplesIterator<Sample> bioSamplesIterator = samplesResourceService.getSamplesIterator(uriBuilder.build());


        Path path = Paths.get(this.getClass().getResource("/").getPath(), options.getFilename());
        ExecutorService executor = null;
        Duration maxDuration = Duration.ofMinutes(60);
        Temporal start = Instant.now();

        try {
            writer = initWriter(path);
            writer = startDocument(writer);
            executor = Executors.newFixedThreadPool(32);

            // First submission
            for (int i = 0; i < options.getSize(); i++) {
                if (bioSamplesIterator.hasNext()) {
                    Resource<Sample> sample = bioSamplesIterator.next();
                    submitExpansionTask(sample, executor);
                    taskInfo.incrementSubmitted(1);
                }
            }

            // Checking for exception queue

            while(taskInfo.getSubmitted() > taskInfo.getCompleted() + taskInfo.getErrors()) {
                while (!exceptionQueue.isEmpty()) {
                    Resource<Sample> sample = exceptionQueue.poll();
                    submitExpansionTask(sample, executor);
                }
                Thread.sleep(5000);
            }

            closeDocument(writer);

        } catch (XMLStreamException | IOException e) {
            log.error("An error occurred while creating the file", e);
        } finally {
            log.debug("Operation completed");
            if (writer != null)
                writer.close();
            if (executor != null && !executor.isShutdown()) {
                executor.shutdownNow();
            }

        }
    }

    private CompletableFuture<Resource<Sample>> submitExpansionTask(Resource<Sample> sample, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Retrieving relations for sample " + sample.getContent().getAccession());
            Map<BioSamplesRelationType, List<BioSamplesRelation>> allRelation = relationsService.getAllRelations(sample.getContent().getAccession(), Sample.class);
            return expandSample(sample, allRelation);
        }, executor).handle((sampleResource,t) -> {
            if (t == null) {
                try {
                    writeResourceToFile(sampleResource, writer);
                    return null;
                } catch (XMLStreamException e) {
                    log.error("An error occured while writing {} to file",
                            sampleResource.getContent().getAccession(),
                            e);
                }
            }
            return sample;
        }).whenComplete((sampleError, throwable) -> {
            if (sampleError != null || throwable != null) {
                try {
                    log.error(String.format("There was an error while expanding sample %s",sampleError.getContent().getAccession()), throwable);
                    if (!exceptionMap.containsKey(sampleError)) {
                        exceptionMap.put(sampleError, new AtomicInteger(0));
                    }
                    int nErr = exceptionMap.get(sampleError).incrementAndGet();
                    log.debug("Resource {} has failed {} times", sampleError.getContent().getAccession(), nErr);
                    if (nErr > maxErrorPerResource) {
                        // If we can't retrieve the sample resource, add an error
                        log.error("Too many errors for resource {}, skipping it", sampleError.getContent().getAccession());
                        taskInfo.incrementError(1);
                        log.debug("Errors occurred {}", taskInfo.getErrors());
                    } else {
                        log.debug("Putting resource {} in the exception queue", sampleError.getContent().getAccession());
                        exceptionQueue.put(sampleError);
                    }

                } catch (InterruptedException e) {
                    log.error("An error occurred while adding sample to exception queue", e);
                }
            } else {
                taskInfo.incrementCompleted(1);
                log.debug("Completed {} tasks", taskInfo.getCompleted());
            }
        });
    }

    private Resource<Sample> expandSample(Resource<Sample> sample, Map<BioSamplesRelationType, List<BioSamplesRelation>> relation) {
        log.debug("Expanding sample " + sample.getContent().getAccession());
        sample.getContent().setRelations(relation);
        return sample;
    }

    private XMLStreamWriter initWriter(Path documentPath) throws XMLStreamException, IOException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty("escapeCharacters", false);
        return factory.createXMLStreamWriter(new FileWriter(documentPath.toFile()));
    }

    private void writeResourceToFile(Resource<Sample> sampleResource, XMLStreamWriter outputWriter) throws XMLStreamException {
        Element sampleEntry = xmlService.getEntryForSample(sampleResource.getContent());
        outputWriter.writeCharacters(xmlService.prettyPrint(sampleEntry));
        outputWriter.flush();
    }

    private XMLStreamWriter startDocument(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartDocument();
        writer.writeCharacters("\n");
        writer.writeStartElement("database");
        writer.writeCharacters("\n");
        writer.writeStartElement("entries");
        writer.writeCharacters("\n");
        writer.flush();
        return writer;
    }

    private XMLStreamWriter closeDocument(XMLStreamWriter writer) throws XMLStreamException
    {
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
        return writer;
    }

    public Duration durationFrom(Temporal start) {
        return Duration.between(start, Instant.now());
    }

}
