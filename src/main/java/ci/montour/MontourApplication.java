package ci.montour;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MontourApplication {

	public static void main(String[] args) {
		SpringApplication.run(MontourApplication.class, args);
	}
}