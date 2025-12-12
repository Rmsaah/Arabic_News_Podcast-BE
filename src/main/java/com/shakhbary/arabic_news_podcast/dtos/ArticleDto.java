package com.shakhbary.arabic_news_podcast.dtos;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ArticleDto {
  private String author;
  private String publisher;
  private String category;
  private String title;
  private OffsetDateTime publicationDate;
  private String contentRawUrl;
  private String scriptUrl;
  private OffsetDateTime fetchDate;
}
