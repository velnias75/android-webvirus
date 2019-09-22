package de.rangun.webvirus.model;

import java.util.List;

public interface IMovie {

    long id();

    String title();

    long duration();

    String durationString();

    List<String> languages();

    String disc();

    int category();

    String filename();

    boolean omu();

    boolean top250();

    Long oid();

    String description();
}
