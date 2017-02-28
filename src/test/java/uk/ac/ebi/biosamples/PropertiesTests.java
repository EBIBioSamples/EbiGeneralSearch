package uk.ac.ebi.biosamples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Application.class, initializers = ConfigFileApplicationContextInitializer.class)
public class PropertiesTests {

    @Value("${ebi.search.page.size}")
    public int documentsPerFile;

    @Test
    public void docPerPageNotNull() {
        assertThat(documentsPerFile).isNotNull();
        assertThat(documentsPerFile).isNotZero();
    }

}
