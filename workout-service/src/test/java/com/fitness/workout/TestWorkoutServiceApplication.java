package com.fitness.workout;

import org.springframework.boot.SpringApplication;

public class TestWorkoutServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(WorkoutServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
