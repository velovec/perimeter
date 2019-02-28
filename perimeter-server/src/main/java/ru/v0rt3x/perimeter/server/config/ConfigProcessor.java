package ru.v0rt3x.perimeter.server.config;

@FunctionalInterface
public interface ConfigProcessor {

    void process(byte[] config);
}
