package com.github.freenamu.backend.service;

import com.github.freenamu.backend.entity.Content;
import com.github.freenamu.backend.entity.Document;
import com.github.freenamu.backend.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        String documentName = "test name";
        String contentBody = "test body";
        String comment = "test comment";
        String contributor = "127.0.0.1";
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
        String documentName = "test name";
        ArrayList<Content> expectedRevisions = new ArrayList<>();

        // When
        for (int i = 0; i < 100; i++) {
            String contentBody = "body" + i;
            String comment = "comment" + i;
            String contributor = "127.0.0." + i;
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
        String documentName = "test name";
        String contentBody = "l" + "o".repeat(1000000) + "ng";
        String comment = "test comment";
        String contributor = "127.0.0.1";
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
        String documentName = "test name";
        String contentBody = "test body";
        String comment = "l" + "o".repeat(252) + "ng";
        String contributor = "127.0.0.1";
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
        String documentName = "test name";
        String contentBody = "test body";
        String comment = "l" + "o".repeat(253) + "ng";
        String contributor = "127.0.0.1";

        // When
        assertThrows(IllegalArgumentException.class, () -> documentService.postDocument(documentName, contentBody, comment, contributor));

        // Then
    }

    @Test
    public void getLatestDocument() {
        // Given
        String documentName = "test name";
        int size = 100;
        for (int i = 1; i <= size; i++) {
            String contentBody = "body" + i;
            String comment = "comment" + i;
            String contributor = "127.0.0." + i;
            documentService.postDocument(documentName, contentBody, comment, contributor);
        }
        String expectedBody = "body" + size;
        String expectedComment = "comment" + size;
        String expectedContributor = "127.0.0." + size;
        Content expected = getExpectedContent(expectedBody, expectedComment, expectedContributor);

        // When
        Content actual = documentService.getLatestDocument(documentName);

        // Then
        assertContentEquals(actual, expected);
    }

    @Test
    public void returnNullIfDocumentIsNotExistWhenGetLatestDocument() {
        // Given
        String documentName = "test name";

        // When
        Content actual = documentService.getLatestDocument(documentName);

        // Then
        assertNull(actual);
    }

    @Test
    public void getDocumentByRevisionIndex() {
        // Given
        String documentName = "test name";
        int size = 100;
        for (int i = 1; i <= size; i++) {
            String contentBody = "body" + i;
            String comment = "comment" + i;
            String contributor = "127.0.0." + i;
            documentService.postDocument(documentName, contentBody, comment, contributor);
        }
        int revisionIndex = 1;
        String expectedBody = "body" + revisionIndex;
        String expectedComment = "comment" + revisionIndex;
        String expectedContributor = "127.0.0." + revisionIndex;
        Content expected = getExpectedContent(expectedBody, expectedComment, expectedContributor);

        // When
        Content actual = documentService.getDocumentByRevisionIndex(documentName, revisionIndex);

        // Then
        assertContentEquals(actual, expected);
    }

    @Test
    public void returnNullIfOutOfRangeWhenGetDocumentByRevisionIndex() {
        // Given
        String documentName = "test name";
        int size = 100;
        for (int i = 1; i <= size; i++) {
            String contentBody = "body" + i;
            String comment = "comment" + i;
            String contributor = "127.0.0." + i;
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
        String documentName = "test name";
        int revisionIndex = 1;

        // When
        Content actual = documentService.getDocumentByRevisionIndex(documentName, revisionIndex);

        // Then
        assertNull(actual);
    }

    @Test
    public void getHistoryOfDocument() {
        // Given
        String documentName = "test name";
        int size = 100;
        ArrayList<Content> expected = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            String contentBody = "body" + i;
            String comment = "comment" + i;
            String contributor = "127.0.0." + i;
            documentService.postDocument(documentName, contentBody, comment, contributor);
            documentService.postDocument("not", "include", "this", "document");
            expected.add(getExpectedContent(null, comment, contributor));
        }

        // When
        List<Content> actual = documentService.getHistoryOfDocument(documentName);

        // Then
        assertRevisionsEquals(expected, actual);
    }

    @Test
    public void returnNullIfDocumentIsNotExistWhenGetHistoryOfDocument() {
        // Given
        String documentName = "test name";

        // When
        List<Content> actual = documentService.getHistoryOfDocument(documentName);

        // Then
        assertNull(actual);
    }

    private Document getExpectedDocument(String documentName, List<Content> expectedRevisions) {
        Document expectedDocument = new Document();
        expectedDocument.setDocumentName(documentName);
        for (Content revision : expectedRevisions) {
            expectedDocument.addContent(revision);
        }
        return expectedDocument;
    }

    private Content getExpectedContent(String contentBody, String comment, String contributor) {
        Content expectedContent = new Content();
        expectedContent.setContentBody(contentBody);
        expectedContent.setComment(comment);
        expectedContent.setContributor(contributor);
        return expectedContent;
    }

    private void assertDocumentEquals(Document expectedDocument, Document actualDocument) {
        assertEquals(expectedDocument.getDocumentName(), actualDocument.getDocumentName());
        assertRevisionsEquals(expectedDocument.getRevisions(), actualDocument.getRevisions());
    }

    private void assertRevisionsEquals(List<Content> expectedRevisions, List<Content> actualRevisions) {
        assertEquals(expectedRevisions.size(), actualRevisions.size());
        for (int i = 0; i < expectedRevisions.size(); i++) {
            assertContentEquals(expectedRevisions.get(i), actualRevisions.get(i));
        }
    }

    private void assertContentEquals(Content expectedContent, Content actualContent) {
        assertEquals(expectedContent.getContentBody(), actualContent.getContentBody());
        assertEquals(expectedContent.getComment(), actualContent.getComment());
        assertEquals(expectedContent.getContributor(), actualContent.getContributor());
    }
}
