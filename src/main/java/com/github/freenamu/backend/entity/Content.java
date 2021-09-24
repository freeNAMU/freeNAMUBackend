package com.github.freenamu.backend.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Content {
    @Id
    @GeneratedValue
    private Long contentId;

    @Column
    private String contributor;

    @Column
    private LocalDateTime createDate = LocalDateTime.now();

    @Column
    @Lob
    private String contentBody;

    @Column
    @Lob
    private String comment;

    public Long getContentId() {
        return contentId;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public String getContentBody() {
        return contentBody;
    }

    public void setContentBody(String contentBody) {
        this.contentBody = contentBody;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if (comment.length() <= 255) {
            this.comment = comment;
        } else {
            throw new IllegalArgumentException("too long comment");
        }
    }
}
