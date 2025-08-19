package com.main_group_ekn47.eventlib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // <-- ¡Añade esto!
@SpringBootApplication
//@EnableR2dbcRepositories("com.main_group_ekn47.eventlib.producer")
public class EventlibApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventlibApplication.class, args);
	}

}
