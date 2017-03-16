package uk.ac.ebi.biosamples.runners;

import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.hateoas.Resource;
import uk.ac.ebi.biosamples.model.entities.BioSamplesRelation;
import uk.ac.ebi.biosamples.model.entities.Sample;
import uk.ac.ebi.biosamples.model.enums.BioSamplesRelationType;
import uk.ac.ebi.biosamples.service.RelationsService;
import uk.ac.ebi.biosamples.service.SamplesResourceService;
import uk.ac.ebi.biosamples.service.XmlService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//@Component
//@Order
public class FixedLengthRunner implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(FixedLengthRunner.class);

    private XmlService xmlService;
    private SamplesResourceService samplesService;
    private RelationsService relationsService;

    public FixedLengthRunner(XmlService xmlService,
                             RelationsService relationsService,
                             SamplesResourceService samplesService)
    {
          this.relationsService = relationsService;
          this.xmlService = xmlService;
          this.samplesService = samplesService;
    }

    @Override
    public void run(String... args) throws Exception {

        log.info("Started FixedLengthRunner application");
        long startTime = System.currentTimeMillis();


        Iterator<Resource<Sample>> iterator = samplesService.getSamplesIterator();
        List<Resource<Sample>> samples = Stream.generate(iterator::next).limit(10).collect(Collectors.toList());
        ExecutorService executor = null;
        try {
            executor = Executors.newFixedThreadPool(16);
            List<CompletableFuture<Resource<Sample>>> futList = new ArrayList<>();
            for(Resource<Sample> sample: samples) {
                futList.add(CompletableFuture.supplyAsync(new Supplier<Resource<Sample>>() {
                    @Override
                    public Resource<Sample> get() {
                        Map<BioSamplesRelationType, List<BioSamplesRelation>> relations = relationsService.getSampleRelations(sample.getContent().getAccession());
                        sample.getContent().setRelations(relations);
                        return sample;
                    }
                }, executor));
            }
            CompletableFuture[] futArray = futList.toArray(new CompletableFuture[futList.size()]);
            CompletableFuture.allOf(futArray).join();
            List<Element> entries = futList.stream()
                    .map(CompletableFuture::join)
                    .map(resource -> xmlService.getEntryForSample(resource.getContent()))
                    .collect(Collectors.toList());
            Document doc = xmlService.produceDocumentForEntries(entries);
            Path path = Paths.get(this.getClass().getResource("/").getPath(),"output.xml");
            try {
                writeFile(doc,path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            if (executor != null) {
                executor.shutdown();
                executor.awaitTermination(1, TimeUnit.SECONDS);
            }
        }

        long endTime = System.currentTimeMillis();
        log.info(String.format("Finished FixedLengthRunner in %d millis",endTime - startTime ));
    }



    private void writeFile(Document doc, Path destination) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(destination.toFile()));
        writer.write(xmlService.prettyPrint(doc));
        writer.close();
    }

}
