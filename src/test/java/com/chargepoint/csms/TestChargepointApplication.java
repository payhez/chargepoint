package com.chargepoint.csms;

import org.springframework.boot.SpringApplication;

public class TestChargepointApplication {

	public static void main(String[] args) {
		SpringApplication.from(ChargepointApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
