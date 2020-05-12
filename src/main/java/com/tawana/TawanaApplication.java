package com.tawana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class TawanaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TawanaApplication.class, args);
	}
}

