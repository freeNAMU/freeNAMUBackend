package com.github.freenamu.backend;

import com.github.freenamu.backend.entity.Content;
import com.github.freenamu.backend.entity.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtil {
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
        assertLocalDateTimeEquals(expectedContent.getCreateDateTime(), actualContent.getCreateDateTime());
    }

    public static void assertHistoryEquals(ArrayList<HashMap<String, Object>> expectedHistory, ArrayList<HashMap<String, Object>> actualHistory) {
        assertEquals(expectedHistory.size(), actualHistory.size());
        for (int i = 0; i < expectedHistory.size(); i++) {
            assertHistoryRowEquals(expectedHistory.get(i), actualHistory.get(i));
        }
    }

    private static void assertHistoryRowEquals(HashMap<String, Object> expectedHistoryRow, HashMap<String, Object> actualHistoryRow) {
        assertEquals(expectedHistoryRow.get("revisionIndex"), actualHistoryRow.get("revisionIndex"));
        assertEquals(expectedHistoryRow.get("comment"), actualHistoryRow.get("comment"));
        assertEquals(expectedHistoryRow.get("contributor"), actualHistoryRow.get("contributor"));
        assertEquals(expectedHistoryRow.get("lengthDiffer"), actualHistoryRow.get("lengthDiffer"));
        assertLocalDateTimeEquals((String) expectedHistoryRow.get("createDateTime"), (String) actualHistoryRow.get("createDateTime"));
    }

    private static void assertLocalDateTimeEquals(String expectedLocalDateTime, String actualLocalDateTime) {
        assertLocalDateTimeEquals(LocalDateTime.parse(expectedLocalDateTime), LocalDateTime.parse(actualLocalDateTime));
    }

    private static void assertLocalDateTimeEquals(LocalDateTime expectedLocalDateTime, LocalDateTime actualLocalDateTime) {
        LocalDateTime expectedLocalDateTimeMinus = expectedLocalDateTime.minusSeconds(1);
        LocalDateTime expectedLocalDateTimePlus = expectedLocalDateTime.plusSeconds(1);
        assertTrue(actualLocalDateTime.isAfter(expectedLocalDateTimeMinus));
        assertTrue(actualLocalDateTime.isBefore(expectedLocalDateTimePlus));
    }
}
