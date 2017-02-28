package uk.ac.ebi.biosamples.Runners;

import org.jdom2.Document;
import org.jdom2.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Component;
import uk.ac.ebi.biosamples.Model.Entities.BioSamplesIterator;
import uk.ac.ebi.biosamples.Model.Entities.Sample;
import uk.ac.ebi.biosamples.Model.Relations.BioSamplesRelation;
import uk.ac.ebi.biosamples.Model.Relations.BioSamplesRelationType;
import uk.ac.ebi.biosamples.Service.RelationsService;
import uk.ac.ebi.biosamples.Service.SamplesResourceService;
import uk.ac.ebi.biosamples.Service.XmlService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FixedLengthRunner implements CommandLineRunner {

    @Autowired
    private RelationsService relationsService;

    @Autowired
    private SamplesResourceService samplesResourceService;

    @Autowired
    private XmlService xmlService;

    @Override
    public void run(String... strings) throws Exception {


        long startTime = System.currentTimeMillis();

        BioSamplesIterator<Sample> iterator = samplesResourceService.getSamplesIterator();
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
        System.out.println(String.format("Finished in %d millis",endTime - startTime ));
    }



    private void writeFile(Document doc, Path destination) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(destination.toFile()));
        writer.write(xmlService.prettyPrint(doc));
        writer.close();
    }
}
