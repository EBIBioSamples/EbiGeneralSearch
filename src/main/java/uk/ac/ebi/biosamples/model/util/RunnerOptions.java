package uk.ac.ebi.biosamples.model.util;

import org.springframework.boot.ApplicationArguments;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RunnerOptions {
    private final int DEFAULT_SIZE = Integer.MAX_VALUE;
    private final int DEFAULT_PAGE = 0;
    private final Path DEFAULT_FILENAME = Paths.get("./output.xml");
    int size;
    int startPage;
    Path filename;

    private RunnerOptions(ApplicationArguments args) {
        this.size = args.containsOption("size") ?
                Integer.parseInt(args.getOptionValues("size").get(0),10) :
                DEFAULT_SIZE;
        this.startPage = args.containsOption("page") ?
                Integer.parseInt(args.getOptionValues("page").get(0),10) :
                DEFAULT_PAGE;
        this.filename = args.containsOption("filename") ?
                Paths.get(args.getOptionValues("filename").get(0)):
                DEFAULT_FILENAME;
    }

    public int getSize() {
        return size;
    }

    public int getStartPage() {
        return startPage;
    }

    public Path getFilename() {
        return filename;
    }

    public static RunnerOptions from(ApplicationArguments args) {
        return new RunnerOptions(args);
    }
}
