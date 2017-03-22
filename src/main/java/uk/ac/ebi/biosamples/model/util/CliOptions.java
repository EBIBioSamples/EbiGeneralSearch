package uk.ac.ebi.biosamples.model.util;

import org.kohsuke.args4j.Option;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CliOptions {

    @Option(name="-o", aliases = {"--output"}, usage="the output path, default is ./output.xml")
    private Path output = Paths.get("./output.xml");

    @Option(name="--pageSize", usage="elements returned from an API call, default is 50")
    private int pageSize = 50;

    @Option(name="--startPage", usage="starting startPage for resource iteration, default is 0")
    private int startPage = 0;

    @Option(name="-c", aliases={"--count"}, usage="count number of elements returned, default is all")
    private int count = Integer.MAX_VALUE;

    @Option(name="-h", aliases = {"--help"}, usage="display this message")
    private boolean help;

    public Path outputPath() {
        return output;
    }

    public int pageSize() {
        return pageSize;
    }

    public int startPage() {
        return startPage;
    }

    public int getElementToRetrieve() {
        return count;
    }

    public boolean showHelp() {
        return help;
    }
}
