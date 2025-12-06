package com.shakhbary.arabic_news_podcast.exceptions;

public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
