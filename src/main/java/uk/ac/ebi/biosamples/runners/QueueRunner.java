package uk.ac.ebi.biosamples.runners;

import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.hateoas.Resource;
import uk.ac.ebi.biosamples.Application;
import uk.ac.ebi.biosamples.model.entities.BioSamplesIterator;
import uk.ac.ebi.biosamples.model.entities.Sample;
import uk.ac.ebi.biosamples.model.relations.BioSamplesRelation;
import uk.ac.ebi.biosamples.model.relations.BioSamplesRelationType;
import uk.ac.ebi.biosamples.service.RelationsService;
import uk.ac.ebi.biosamples.service.SamplesResourceService;
import uk.ac.ebi.biosamples.service.XmlService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

//@Component
public class QueueRunner implements CommandLineRunner{
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Value("${ebi.search.page.size:100}")
    private int entitiesPerFile;
    private int currentFileNumber;

    private XmlService xmlService;
    private SamplesResourceService samplesResourceService;
    private RelationsService relationsService;

    public QueueRunner(XmlService xmlService,
                             RelationsService relationsService,
                             SamplesResourceService samplesService)
    {
        this.relationsService = relationsService;
        this.xmlService = xmlService;
        this.samplesResourceService = samplesService;
    }
    @Override
    public void run(String... strings) throws Exception {

        log.info("Starting QueueRunner");

        BioSamplesIterator<Sample> bioSamplesIterator = samplesResourceService.getSamplesIterator();
        int currentlyDone = 0;
        currentFileNumber = 0;
        int tempMaximum = 200;
        Executor executor = Executors.newFixedThreadPool(16);
        LinkedBlockingQueue<Resource<Sample>> writingQueue = new LinkedBlockingQueue<>(entitiesPerFile);
        while(bioSamplesIterator.hasNext() && currentlyDone < tempMaximum) {
            Resource<Sample> sample = bioSamplesIterator.next();
            CompletableFuture<Resource<Sample>> futureSample = CompletableFuture.supplyAsync(() -> {
                Map<BioSamplesRelationType, List<BioSamplesRelation>> allRelation = relationsService.getAllRelations(sample.getContent().getAccession(), Sample.class);
                return expandSample(sample, allRelation);
            }, executor);
            futureSample.thenAccept(sampleResource -> {
                if(writingQueue.remainingCapacity() < 1) {
                    writeQueueToFile(writingQueue);
                    writingQueue.clear();
                }
                try {
                    writingQueue.put(sampleResource);
                } catch (InterruptedException e) {
                    log.error("Error while putting sample resource in the queue",e);
                }
            });
            log.info(String.format("Currently processed %d samples", currentlyDone));
            currentlyDone++;
        }
    }

    private Resource<Sample> expandSample(Resource<Sample> sample, Map<BioSamplesRelationType, List<BioSamplesRelation>> relation) {
        log.info("Expanding sample " + sample.getContent().getAccession());
        sample.getContent().setRelations(relation);
        return sample;
    }

    private void writeQueueToFile(BlockingQueue<Resource<Sample>> resourceQueue) {
        log.info("Writing queue to file");
        List<Element> entries = resourceQueue.stream()
                .map(resource -> xmlService.getEntryForSample(resource.getContent()))
                .collect(Collectors.toList());
        Document doc = xmlService.produceDocumentForEntries(entries);
        Path path = Paths.get(
                this.getClass().getResource("/").getPath(),
                String.format("output_%d.xml",currentFileNumber)
        );
        try {
            writeFile(doc,path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentFileNumber = currentFileNumber+1;
    }

    private void writeFile(Document doc, Path destination) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(destination.toFile()));
        writer.write(xmlService.prettyPrint(doc));
        writer.close();
    }



}

