package uk.ac.ebi.biosamples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.biosamples.model.util.CliOptions;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
public class Args4jTests {

    @Test
    public void getFilenameTest() {
        String[] args = {"-o ./output.xml"};
        CliOptions options = new CliOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
            assertThat(options.outputPath().toString().endsWith("output.xml"));
        } catch (CmdLineException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void printUsage() {
        CliOptions options = new CliOptions();
        CmdLineParser parser = new CmdLineParser(options);
        parser.printUsage(System.out);
    }


}
