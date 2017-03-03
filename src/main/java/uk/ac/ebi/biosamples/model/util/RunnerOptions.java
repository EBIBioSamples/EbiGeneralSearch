package uk.ac.ebi.biosamples.model.util;

import org.springframework.boot.ApplicationArguments;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RunnerOptions {

    private final int DEFAULT_TOTAL = Integer.MAX_VALUE;
    private final int DEFAULT_SIZE = 50;
    private final int DEFAULT_PAGE = 0;
    private final Path DEFAULT_FILENAME = Paths.get("./output.xml");
    int total;
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
        this.total = args.containsOption("total") ?
                Integer.parseInt(args.getOptionValues("total").get(0)) :
                DEFAULT_TOTAL;

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

    public int getTotal() { return total; }

    public static RunnerOptions from(ApplicationArguments args) {
        return new RunnerOptions(args);
    }
}
