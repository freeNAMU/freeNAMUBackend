package com.github.freenamu.backend;

import com.github.freenamu.backend.entity.Content;
import com.github.freenamu.backend.entity.Document;
import com.github.freenamu.backend.vo.History;

import java.util.List;

import static com.github.freenamu.backend.vo.History.Row;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtil {
    private static int uniqueNumber = 0;

    public static int getUniqueNumber() {
        return uniqueNumber++;
    }

    public static String getRandomString() {
        return getRandomString(10);
    }

    public static String getRandomString(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append((char) ((Math.random() * 11172) + 0xAC00));
        }
        return result.toString();
    }

    public static Document getExpectedDocument(String documentName, List<Content> expectedRevisions) {
        Document expectedDocument = new Document();
        expectedDocument.setDocumentName(documentName);
        for (Content revision : expectedRevisions) {
            expectedDocument.addContent(revision);
        }
        return expectedDocument;
    }

    public static Content getAnonymousContent() {
        return getExpectedContent((long) getUniqueNumber(), getRandomString(10), getRandomString(10), getRandomString(10));
    }

    public static Content getExpectedContent(Long contentId, String contentBody, String comment, String contributor) {
        Content expectedContent = new Content();
        expectedContent.setContentId(contentId);
        expectedContent.setContentBody(contentBody);
        expectedContent.setComment(comment);
        expectedContent.setContributor(contributor);
        return expectedContent;
    }

    public static Content getExpectedContent(String contentBody, String comment, String contributor) {
        Content expectedContent = new Content();
        expectedContent.setContentBody(contentBody);
        expectedContent.setComment(comment);
        expectedContent.setContributor(contributor);
        return expectedContent;
    }

    public static void assertDocumentEquals(Document expectedDocument, Document actualDocument) {
        assertEquals(expectedDocument.getDocumentName(), actualDocument.getDocumentName());
        assertRevisionsEquals(expectedDocument.getRevisions(), actualDocument.getRevisions());
    }

    public static void assertRevisionsEquals(List<Content> expectedRevisions, List<Content> actualRevisions) {
        assertEquals(expectedRevisions.size(), actualRevisions.size());
        for (int i = 0; i < expectedRevisions.size(); i++) {
            assertContentEquals(expectedRevisions.get(i), actualRevisions.get(i));
        }
    }

    public static void assertContentEquals(Content expectedContent, Content actualContent) {
        assertEquals(expectedContent.getContentBody(), actualContent.getContentBody());
        assertEquals(expectedContent.getComment(), actualContent.getComment());
        assertEquals(expectedContent.getContributor(), actualContent.getContributor());
    }

    public static void assertHistoryEquals(History expectedHistory, History actualHistory) {
        List<Row> expectedHistoryRows = expectedHistory.getRows();
        List<Row> actualHistoryRows = actualHistory.getRows();
        assertEquals(expectedHistoryRows.size(), actualHistoryRows.size());
        for (int i = 0; i < expectedHistoryRows.size(); i++) {
            Row expectedHistoryRow = expectedHistoryRows.get(i);
            Row actualHistoryRow = actualHistoryRows.get(i);
            assertEquals(expectedHistoryRow.getRevisionIndex(), actualHistoryRow.getRevisionIndex());
            assertEquals(expectedHistoryRow.getComment(), actualHistoryRow.getComment());
            assertEquals(expectedHistoryRow.getContributor(), actualHistoryRow.getContributor());
        }
    }
}
