package com.shakhbary.arabic_news_podcast.dtos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AudioDto {
  private long duration; // in seconds
  private String format;
  private String urlPath;
}
