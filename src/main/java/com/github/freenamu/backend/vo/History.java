package com.github.freenamu.backend.vo;

import com.github.freenamu.backend.entity.Content;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class History {
    private final List<Row> rows = new ArrayList<>();
    private int revisionIndex = 1;

    private int getNextRevisionIndex() {
        return revisionIndex++;
    }

    public List<Row> getRows() {
        return rows;
    }

    public Row get(int i) {
        return rows.get(i);
    }

    public void add(Content content) {
        Row row = new Row();
        row.setRevisionIndex(getNextRevisionIndex());
        row.setComment(content.getComment());
        row.setContributor(content.getContributor());
        row.setCreateDate(content.getCreateDate());
        row.setLength(content.getContentBody().length());
        rows.add(row);
    }

    public static class Row {
        private int revisionIndex;
        private String comment;
        private String contributor;
        private String createDate;
        private int length;

        public int getRevisionIndex() {
            return revisionIndex;
        }

        public void setRevisionIndex(int revisionIndex) {
            this.revisionIndex = revisionIndex;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getContributor() {
            return contributor;
        }

        public void setContributor(String contributor) {
            this.contributor = contributor;
        }

        public String getCreateDate() {
            return createDate;
        }

        public void setCreateDate(String createDate) {
            this.createDate = createDate;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }
}
