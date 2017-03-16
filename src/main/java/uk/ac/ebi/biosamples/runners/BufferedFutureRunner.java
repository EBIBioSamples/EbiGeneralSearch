package uk.ac.ebi.biosamples.runners;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.hateoas.Resource;
import uk.ac.ebi.biosamples.model.entities.BioSamplesRelation;
import uk.ac.ebi.biosamples.model.entities.Sample;
import uk.ac.ebi.biosamples.model.enums.BioSamplesRelationType;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

//@Component
public class BufferedFutureRunner implements ApplicationRunner {
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

        log.info("EBI General Search exporter started");

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
            while(pagedResourceIterator.hasNext() && taskInfo.getSubmitted() < options.getSize()) {
                Resource<Sample> sample = pagedResourceIterator.next();
                submitExpansionTask(sample, executor);
                taskInfo.incrementSubmitted(1);
                log.debug("Submitted {} tasks", taskInfo.getSubmitted());
            }

            // Checking for exception queue

            while(taskInfo.getSubmitted() > taskInfo.getCompleted() + taskInfo.getErrors()) {
                while (!exceptionQueue.isEmpty()) {
                    Resource<Sample> sample = exceptionQueue.poll();
                    log.debug("Resubmitting task for sample {}", sample.getContent().getAccession());
                    submitExpansionTask(sample, executor);
                }
                Thread.sleep(5000);
            }
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
