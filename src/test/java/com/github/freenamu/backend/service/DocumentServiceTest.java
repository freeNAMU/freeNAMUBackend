package com.github.freenamu.backend.service;

import com.github.freenamu.backend.entity.Content;
import com.github.freenamu.backend.entity.Document;
import com.github.freenamu.backend.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static com.github.freenamu.backend.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DocumentServiceTest {
    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    public void postDocumentOnce() {
        // Given
        String documentName = "anonymous name";
        ArrayList<Content> expectedRevisions = new ArrayList<>();
        Document expectedDocument = new Document(documentName, expectedRevisions);

        String contentBody = "anonymous body";
        String comment = "anonymous comment";
        String contributor = "anonymous contributor";
        expectedRevisions.add(new Content(contentBody, comment, contributor));

        // When
        documentService.postDocument(documentName, contentBody, comment, contributor);

        // Then
        Optional<Document> optionalDocument = documentRepository.findById(documentName);
        assertTrue(optionalDocument.isPresent());
        Document actualDocument = optionalDocument.get();
        assertDocumentEquals(expectedDocument, actualDocument);
    }

    @Test
    public void postDocumentMany() {
        // Given
        String documentName = "anonymous name";
        ArrayList<Content> expectedRevisions = new ArrayList<>();
        Document expectedDocument = new Document(documentName, expectedRevisions);

        // When
        for (int i = 0; i < 100; i++) {
            String contentBody = "anonymous body";
            String comment = "anonymous comment";
            String contributor = "anonymous contributor";
            documentService.postDocument(documentName, contentBody, comment, contributor);
            expectedRevisions.add(new Content(contentBody, comment, contributor));
        }

        // Then
        Optional<Document> optionalDocument = documentRepository.findById(documentName);
        assertTrue(optionalDocument.isPresent());
        Document actualDocument = optionalDocument.get();
        assertDocumentEquals(expectedDocument, actualDocument);
    }

    @Test
    public void postDocumentWithLongBody() {
        // Given
        String documentName = "anonymous name";
        ArrayList<Content> expectedRevisions = new ArrayList<>();
        Document expectedDocument = new Document(documentName, expectedRevisions);

        String contentBody = "l" + "o".repeat(100000) + "ng";
        String comment = "anonymous comment";
        String contributor = "anonymous contributor";
        expectedRevisions.add(new Content(contentBody, comment, contributor));

        // When
        documentService.postDocument(documentName, contentBody, comment, contributor);

        // Then
        Optional<Document> optionalDocument = documentRepository.findById(documentName);
        assertTrue(optionalDocument.isPresent());
        Document actualDocument = optionalDocument.get();
        assertDocumentEquals(expectedDocument, actualDocument);
    }

    @Test
    public void postDocumentWithCommentLengthLessThan256() {
        // Given
        String documentName = "anonymous name";
        ArrayList<Content> expectedRevisions = new ArrayList<>();
        Document expectedDocument = new Document(documentName, expectedRevisions);

        String contentBody = "anonymous body";
        String comment = "sh" + "o".repeat(251) + "rt";
        String contributor = "anonymous contributor";
        expectedRevisions.add(new Content(contentBody, comment, contributor));

        // When
        documentService.postDocument(documentName, contentBody, comment, contributor);

        // Then
        Optional<Document> optionalDocument = documentRepository.findById(documentName);
        assertTrue(optionalDocument.isPresent());
        Document actualDocument = optionalDocument.get();
        assertDocumentEquals(expectedDocument, actualDocument);
    }

    @Test
    public void throwExceptionWhenPostDocumentWithLongComment() {
        // Given
        String documentName = "anonymous name";
        String contentBody = "anonymous body";
        String comment = "l" + "o".repeat(253) + "ng";
        String contributor = "anonymous contributor";

        // When
        assertThrows(IllegalArgumentException.class, () -> documentService.postDocument(documentName, contentBody, comment, contributor));

        // Then
    }

    @Test
    public void getLatestDocument() {
        // Given
        String documentName = "anonymous name";
        String contentBody = "anonymous body";
        String comment = "anonymous comment";
        String contributor = "anonymous contributor";
        for (int i = 0; i < 100; i++) {
            documentService.postDocument(documentName, contentBody + i, comment + i, contributor + i);
        }
        documentService.postDocument(documentName, contentBody, comment, contributor);
        Content expected = new Content(contentBody, comment, contributor);

        // When
        Content actual = documentService.getLatestDocument(documentName);

        // Then
        assertContentEquals(actual, expected);
    }

    @Test
    public void returnNullIfDocumentIsNotExistWhenGetLatestDocument() {
        // Given
        String documentName = "anonymous name";

        // When
        Content actual = documentService.getLatestDocument(documentName);

        // Then
        assertNull(actual);
    }

    @Test
    public void getDocumentByRevisionIndex() {
        // Given
        String documentName = "anonymous name";
        int revisionIndex = 1;
        String contentBody = "anonymous body";
        String comment = "anonymous comment";
        String contributor = "anonymous contributor";
        documentService.postDocument(documentName, contentBody, comment, contributor);
        Content expected = new Content(contentBody, comment, contributor);
        int size = 100;
        for (int i = 0; i < size; i++) {
            documentService.postDocument(documentName, contentBody + 1, comment + 1, contributor + 1);
        }

        // When
        Content actual = documentService.getDocumentByRevisionIndex(documentName, revisionIndex);

        // Then
        assertContentEquals(actual, expected);
    }

    @Test
    public void returnNullIfOutOfRangeWhenGetDocumentByRevisionIndex1() {
        // Given
        String documentName = "anonymous name";
        int revisionIndex = 0;
        int size = 100;
        for (int i = 0; i < size; i++) {
            String contentBody = "anonymous body";
            String comment = "anonymous comment";
            String contributor = "anonymous contributor";
            documentService.postDocument(documentName, contentBody, comment, contributor);
        }

        // When
        Content actual = documentService.getDocumentByRevisionIndex(documentName, revisionIndex);

        // Then
        assertNull(actual);
    }

    @Test
    public void returnNullIfOutOfRangeWhenGetDocumentByRevisionIndex2() {
        // Given
        String documentName = "anonymous name";
        int revisionIndex = 101;
        int size = 100;
        for (int i = 0; i < size; i++) {
            String contentBody = "anonymous body";
            String comment = "anonymous comment";
            String contributor = "anonymous contributor";
            documentService.postDocument(documentName, contentBody, comment, contributor);
        }

        // When
        Content actual = documentService.getDocumentByRevisionIndex(documentName, revisionIndex);

        // Then
        assertNull(actual);
    }

    @Test
    public void returnNullIfOutOfRangeWhenGetDocumentByRevisionIndex3() {
        // Given
        String documentName = "anonymous name";
        int revisionIndex = -1;
        int size = 100;
        for (int i = 0; i < size; i++) {
            String contentBody = "anonymous body";
            String comment = "anonymous comment";
            String contributor = "anonymous contributor";
            documentService.postDocument(documentName, contentBody, comment, contributor);
        }

        // When
        Content actual = documentService.getDocumentByRevisionIndex(documentName, revisionIndex);

        // Then
        assertNull(actual);
    }

    @Test
    public void returnNullIfDocumentIsNotExistWhenGetDocumentByRevisionIndex() {
        // Given
        String documentName = "anonymous name";
        int revisionIndex = 1;

        // When
        Content actual = documentService.getDocumentByRevisionIndex(documentName, revisionIndex);

        // Then
        assertNull(actual);
    }

    @Test
    public void getHistoryOfDocument() {
        // Given
        String documentName = "anonymous name";
        int size = 100;
        String comment = "anonymous comment";
        String contributor = "anonymous contributor";
        for (int i = 1; i <= size; i++) {
            documentService.postDocument(documentName, "test".repeat(i), comment + i, contributor + i);
        }
        ArrayList<HashMap<String, Object>> expected = new ArrayList<>();
        for (int i = size; i > 0; i--) {
            HashMap<String, Object> expectedHistoryRow = new HashMap<>();
            expectedHistoryRow.put("revisionIndex", i);
            expectedHistoryRow.put("comment", comment + i);
            expectedHistoryRow.put("contributor", contributor + i);
            expectedHistoryRow.put("lengthDiffer", 4);
            expectedHistoryRow.put("createDateTime", LocalDateTime.now().toString());
            expected.add(expectedHistoryRow);
        }

        // When
        ArrayList<HashMap<String, Object>> actual = documentService.getHistoryOfDocument(documentName);

        // Then
        assertHistoryEquals(expected, actual);
    }

    @Test
    public void returnNullIfDocumentIsNotExistWhenGetHistoryOfDocument() {
        // Given
        String documentName = "anonymous name";

        // When
        ArrayList<HashMap<String, Object>> actual = documentService.getHistoryOfDocument(documentName);

        // Then
        assertNull(actual);
    }
}
