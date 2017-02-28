package uk.ac.ebi.biosamples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.biosamples.Runners.FixedLengthRunner;


@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(FixedLengthRunner.class);
	}





	
}

