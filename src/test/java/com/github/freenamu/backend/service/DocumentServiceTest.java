package com.github.freenamu.backend.service;

import com.github.freenamu.backend.entity.Content;
import com.github.freenamu.backend.entity.Document;
import com.github.freenamu.backend.repository.DocumentRepository;
import com.github.freenamu.backend.vo.History;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.freenamu.backend.TestUtil.*;
import static com.github.freenamu.backend.vo.History.*;
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
        String documentName = getRandomString();
        String contentBody = getRandomString();
        String comment = getRandomString();
        String contributor = getRandomString();
        ArrayList<Content> expectedRevisions = new ArrayList<>();
        expectedRevisions.add(getExpectedContent(contentBody, comment, contributor));
        Document expectedDocument = getExpectedDocument(documentName, expectedRevisions);

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
        String documentName = getRandomString();
        ArrayList<Content> expectedRevisions = new ArrayList<>();

        // When
        for (int i = 0; i < 100; i++) {
            String contentBody = getRandomString();
            String comment = getRandomString();
            String contributor = getRandomString();
            documentService.postDocument(documentName, contentBody, comment, contributor);
            expectedRevisions.add(getExpectedContent(contentBody, comment, contributor));
        }
        Document expectedDocument = getExpectedDocument(documentName, expectedRevisions);

        // Then
        Optional<Document> optionalDocument = documentRepository.findById(documentName);
        assertTrue(optionalDocument.isPresent());
        Document actualDocument = optionalDocument.get();
        assertDocumentEquals(expectedDocument, actualDocument);
    }

    @Test
    public void postDocumentWithLongBody() {
        // Given
        String documentName = getRandomString();
        String contentBody = getRandomString(1000000);
        String comment = getRandomString();
        String contributor = getRandomString();
        ArrayList<Content> expectedRevisions = new ArrayList<>();
        expectedRevisions.add(getExpectedContent(contentBody, comment, contributor));
        Document expectedDocument = getExpectedDocument(documentName, expectedRevisions);

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
        String documentName = getRandomString();
        String contentBody = getRandomString();
        String comment = getRandomString(255);
        String contributor = getRandomString();
        ArrayList<Content> expectedRevisions = new ArrayList<>();
        expectedRevisions.add(getExpectedContent(contentBody, comment, contributor));
        Document expectedDocument = getExpectedDocument(documentName, expectedRevisions);

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
        String documentName = getRandomString();
        String contentBody = getRandomString();
        String comment = getRandomString(256);
        String contributor = getRandomString();

        // When
        assertThrows(IllegalArgumentException.class, () -> documentService.postDocument(documentName, contentBody, comment, contributor));

        // Then
    }

    @Test
    public void getLatestDocument() {
        // Given
        String documentName = getRandomString();
        Content expected = null;
        for (int i = 0; i < 100; i++) {
            String contentBody = getRandomString();
            String comment = getRandomString();
            String contributor = getRandomString();
            documentService.postDocument(documentName, contentBody, comment, contributor);
            expected = getExpectedContent(contentBody, comment, contributor);
        }

        // When
        Content actual = documentService.getLatestDocument(documentName);

        // Then
        assertContentEquals(actual, expected);
    }

    @Test
    public void returnNullIfDocumentIsNotExistWhenGetLatestDocument() {
        // Given
        String documentName = getRandomString();

        // When
        Content actual = documentService.getLatestDocument(documentName);

        // Then
        assertNull(actual);
    }

    @Test
    public void getDocumentByRevisionIndex() {
        // Given
        String documentName = getRandomString();
        int revisionIndex = 1;
        String expectedBody = getRandomString();
        String expectedComment = getRandomString();
        String expectedContributor = getRandomString();
        documentService.postDocument(documentName, expectedBody, expectedComment, expectedContributor);
        Content expected = getExpectedContent(expectedBody, expectedComment, expectedContributor);
        int size = 100;
        for (int i = 0; i < size; i++) {
            String contentBody = getRandomString();
            String comment = getRandomString();
            String contributor = getRandomString();
            documentService.postDocument(documentName, contentBody, comment, contributor);
        }

        // When
        Content actual = documentService.getDocumentByRevisionIndex(documentName, revisionIndex);

        // Then
        assertContentEquals(actual, expected);
    }

    @Test
    public void returnNullIfOutOfRangeWhenGetDocumentByRevisionIndex() {
        // Given
        String documentName = getRandomString();
        int size = 100;
        for (int i = 0; i < size; i++) {
            String contentBody = getRandomString();
            String comment = getRandomString();
            String contributor = getRandomString();
            documentService.postDocument(documentName, contentBody, comment, contributor);
        }
        int revisionIndex = 101;

        // When
        Content actual = documentService.getDocumentByRevisionIndex(documentName, revisionIndex);

        // Then
        assertNull(actual);
    }

    @Test
    public void returnNullIfDocumentIsNotExistWhenGetDocumentByRevisionIndex() {
        // Given
        String documentName = getRandomString();
        int revisionIndex = 1;

        // When
        Content actual = documentService.getDocumentByRevisionIndex(documentName, revisionIndex);

        // Then
        assertNull(actual);
    }

    @Test
    public void getHistoryOfDocument() {
        // Given
        String documentName = getRandomString();
        int size = 100;
        History expected = new History();
        for (int i = 1; i <= size; i++) {
            String contentBody = getRandomString();
            String comment = getRandomString();
            String contributor = getRandomString();
            documentService.postDocument(documentName, contentBody, comment, contributor);
            documentService.postDocument("not", "include", "this", "document");
            Row row = new Row();
            row.setRevisionIndex(i);
            row.setComment(comment);
            row.setContributor(contributor);
            row.setLength(contentBody.length());
            expected.getRows().add(row);
        }

        // When
        History actual = documentService.getHistoryOfDocument(documentName);

        // Then
        assertHistoryEquals(expected, actual);
    }

    @Test
    public void returnNullIfDocumentIsNotExistWhenGetHistoryOfDocument() {
        // Given
        String documentName = getRandomString();

        // When
        History actual = documentService.getHistoryOfDocument(documentName);

        // Then
        assertNull(actual);
    }
}
