package com.github.freenamu.backend.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Content {
    @Id
    @GeneratedValue
    private Long contentId;

    @Column
    @Lob
    private String contentBody;

    @Column
    @Lob
    private String comment;

    @Column
    private String contributor;

    @Column
    private LocalDateTime createDateTime;

    public Content() {
        this.createDateTime = LocalDateTime.now();
    }

    public Content(String contentBody, String comment, String contributor) {
        this();
        this.contributor = contributor;
        this.contentBody = contentBody;
        this.comment = comment;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getCreateDateTime() {
        return createDateTime.toString();
    }

    public void setCreateDateTime(LocalDateTime createDate) {
        this.createDateTime = createDate;
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
