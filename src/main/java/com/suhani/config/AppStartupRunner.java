package com.suhani.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.suhani.service.WebhookService;

@Component
public class AppStartupRunner implements CommandLineRunner{

	@Autowired
    private WebhookService webhookService;
	
	@Override
	public void run(String... args) throws Exception {
		webhookService.generateWebhookAndSubmitSolution();
		
	}

}
