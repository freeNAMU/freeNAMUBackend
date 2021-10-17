package com.github.freenamu.backend.entity;

import com.github.freenamu.parser.grammar.RedirectGrammar;

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
        setContentBody(contentBody);
        setComment(comment);
        setContributor(contributor);
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
        contentBody = contentBody.replaceAll("\r\n", "\n");
        RedirectGrammar redirectGrammar = new RedirectGrammar();
        if (redirectGrammar.match(contentBody)) {
            contentBody = contentBody.substring(redirectGrammar.getStart(), redirectGrammar.getEnd());
        }
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
