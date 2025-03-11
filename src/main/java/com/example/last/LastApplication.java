package com.example.last;

import com.example.last.service.EmployeeService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class LastApplication {
	public static void main(String[] args) {
		SpringApplication.run(LastApplication.class, args);
	}

}
