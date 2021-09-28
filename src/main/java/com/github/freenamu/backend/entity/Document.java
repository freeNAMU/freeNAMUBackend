package com.github.freenamu.backend.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Document {
    @Id
    private String documentName;

    @OneToMany(cascade = CascadeType.ALL)
    @OrderColumn
    private List<Content> revisions;

    public Document(String documentName, List<Content> revisions) {
        this.documentName = documentName;
        this.revisions = revisions;
    }

    public Document() {
        this.revisions = new ArrayList<>();
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public List<Content> getRevisions() {
        return revisions;
    }

    public void addContent(Content content) {
        revisions.add(content);
    }
}
