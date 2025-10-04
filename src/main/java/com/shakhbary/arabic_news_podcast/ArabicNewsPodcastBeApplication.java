package com.shakhbary.arabic_news_podcast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ArabicNewsPodcastBeApplication {

		public static void main(String[] args) {
			SpringApplication.run(ArabicNewsPodcastBeApplication.class, args);

			System.out.println("Arabic News Podcast - BE has been started");
		}

}
