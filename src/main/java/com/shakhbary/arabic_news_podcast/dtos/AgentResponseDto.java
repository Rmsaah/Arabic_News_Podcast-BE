package com.shakhbary.arabic_news_podcast.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentResponseDto {
    private String title;
    private String descriptionFusha;
    private String podcastScript;
    private String date;
    private String source;
    private String scrapedTime;
}