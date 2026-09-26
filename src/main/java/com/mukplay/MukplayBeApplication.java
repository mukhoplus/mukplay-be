package com.mukplay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MukplayBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MukplayBeApplication.class, args);
	}

}
