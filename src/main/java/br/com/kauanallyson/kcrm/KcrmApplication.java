package br.com.kauanallyson.kcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KcrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(KcrmApplication.class, args);
    }

}
