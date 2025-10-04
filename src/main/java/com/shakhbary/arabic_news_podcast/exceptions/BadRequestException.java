package com.shakhbary.arabic_news_podcast.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}