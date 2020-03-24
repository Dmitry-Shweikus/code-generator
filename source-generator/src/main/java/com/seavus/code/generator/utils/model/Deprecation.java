package com.seavus.code.generator.utils.model;

import java.util.ArrayList;
import java.util.List;

public class Deprecation {
    protected List<String> links = new ArrayList<>();
    protected String comment;

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
