package de.rangun.webvirus.model;

import java.util.List;

abstract class AbstractMovie implements IMovie {

    private final IMovie m;

    AbstractMovie(IMovie m) {
        this.m = m;
    }

    @Override
    public long id() {
        return m.id();
    }

    @Override
    public Long oid() {
        return m.oid();
    }

    @Override
    public String title() {
        return m.title();
    }

    @Override
    public long duration() {
        return m.duration();
    }

    @Override
    public String durationString() {
        return m.durationString();
    }

    @Override
    public List<String> languages() {
        return m.languages();
    }

    @Override
    public String disc() {
        return m.disc();
    }

    @Override
    public int category() {
        return m.category();
    }

    @Override
    public String filename() {
        return m.filename();
    }

    @Override
    public boolean omu() {
        return m.omu();
    }

    @Override
    public boolean top250() {
        return m.top250();
    }
}
