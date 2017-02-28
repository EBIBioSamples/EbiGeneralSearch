package uk.ac.ebi.biosamples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.biosamples.Runners.FixedLengthRunner;


@SpringBootApplication
public class Application {

	@Autowired
	FixedLengthRunner fixedLengthRunner;

	public static void main(String[] args) {
		Application.
		SpringApplication.run(FixedLengthRunner.class);
	}





	
}

