package com.github.freenamu.backend.controller;

import com.github.freenamu.backend.entity.Content;
import com.github.freenamu.backend.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;

import static com.github.freenamu.backend.TestUtil.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentControllerTest {
    private final String getLatestDocumentURLTemplate = "/document/{documentName}/latest/raw";
    private final String getDocumentByRevisionIndexURLTemplate = "/document/{documentName}/{revisionIndex}/raw";
    private final String getHistoryOfDocumentURLTemplate = "/document/{documentName}/history";
    private final String postDocumentURLTemplate = "/document/{documentName}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    void returnOKWhenPostDocumentWithFullValidInput() throws Exception {
        // Given
        String documentName = getRandomString();
        String contentBody = getRandomString();
        String comment = getRandomString();

        // When
        ResultActions resultActions = mockMvc.perform(post(postDocumentURLTemplate, documentName)
                .param("contentBody", contentBody)
                .param("comment", comment));

        // Then
        resultActions.andExpect(status().isOk());
    }

    @Test
    void returnBadRequestWhenPostDocumentWithoutContentBody() throws Exception {
        // Given
        String documentName = getRandomString();
        String comment = getRandomString();

        // When
        ResultActions resultActions = mockMvc.perform(post(postDocumentURLTemplate, documentName)
                .param("comment", comment));

        // Then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    void returnOKWhenPostDocumentWithoutComment() throws Exception {
        // Given
        String documentName = getRandomString();
        String contentBody = getRandomString();

        // When
        ResultActions resultActions = mockMvc.perform(post(postDocumentURLTemplate, documentName)
                .param("contentBody", contentBody));

        // Then
        resultActions.andExpect(status().isOk());
        verify(documentService).postDocument(eq(documentName), eq(contentBody), eq(""), anyString());
    }

    @Test
    void returnBadRequestWhenPostDocumentWithLongComment() throws Exception {
        // Given
        String documentName = getRandomString();
        String contentBody = getRandomString();
        String comment = getRandomString(256);
        doThrow(new IllegalArgumentException()).when(documentService).postDocument(anyString(), anyString(), anyString(), anyString());

        // When
        ResultActions resultActions = mockMvc.perform(post(postDocumentURLTemplate, documentName)
                .param("contentBody", contentBody)
                .param("comment", comment));

        // Then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    void returnLatestDocumentWhenGetLatestDocumentWithFullValidInput() throws Exception {
        // Given
        String documentName = getRandomString();
        Content expectedContent = getAnonymousContent();
        given(documentService.getLatestDocument(documentName)).willReturn(expectedContent);

        // When
        ResultActions resultActions = mockMvc.perform(get(getLatestDocumentURLTemplate, documentName));

        // Then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("contentId").value(expectedContent.getContentId()));
        resultActions.andExpect(jsonPath("contentBody").value(expectedContent.getContentBody()));
        resultActions.andExpect(jsonPath("comment").value(expectedContent.getComment()));
        resultActions.andExpect(jsonPath("contributor").value(expectedContent.getContributor()));
    }

    @Test
    void returnNotFoundWhenGetLatestDocumentWithNotExistDocument() throws Exception {
        // Given
        String documentName = getRandomString();
        given(documentService.getLatestDocument(documentName)).willReturn(null);

        // When
        ResultActions resultActions = mockMvc.perform(get(getLatestDocumentURLTemplate, documentName));

        // Then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void returnDocumentWhenGetDocumentByRevisionIndexWithFullValidInput() throws Exception {
        // Given
        String documentName = getRandomString();
        int revisionIndex = getUniqueNumber();
        Content expectedContent = getAnonymousContent();
        given(documentService.getDocumentByRevisionIndex(documentName, revisionIndex)).willReturn(expectedContent);

        // When
        ResultActions resultActions = mockMvc.perform(get(getDocumentByRevisionIndexURLTemplate, documentName, revisionIndex));

        // Then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("contentId").value(expectedContent.getContentId()));
        resultActions.andExpect(jsonPath("contentBody").value(expectedContent.getContentBody()));
        resultActions.andExpect(jsonPath("comment").value(expectedContent.getComment()));
        resultActions.andExpect(jsonPath("contributor").value(expectedContent.getContributor()));
    }

    @Test
    void returnNotFoundWhenGetDocumentByRevisionIndexWithNotExistDocument() throws Exception {
        // Given
        String documentName = getRandomString();
        int revisionIndex = getUniqueNumber();
        given(documentService.getDocumentByRevisionIndex(documentName, revisionIndex)).willReturn(null);

        // When
        ResultActions resultActions = mockMvc.perform(get(getDocumentByRevisionIndexURLTemplate, documentName, revisionIndex));

        // Then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void returnHistoryOfDocumentWhenGetHistoryOfDocumentWithFullValidInput() throws Exception {
        // Given
        String documentName = getRandomString();
        int size = 10;
        ArrayList<Content> expectedRevisions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            expectedRevisions.add(getExpectedContent((long) i, getRandomString(), getRandomString(), getRandomString()));
        }
        given(documentService.getHistoryOfDocument(documentName)).willReturn(expectedRevisions);

        // When
        ResultActions resultActions = mockMvc.perform(get(getHistoryOfDocumentURLTemplate, documentName));

        // Then
        resultActions.andExpect(status().isOk());
        for (int i = 0; i < size; i++) {
            Content expectedContent = expectedRevisions.get(i);
            resultActions.andExpect(jsonPath("$[" + i + "].contentId").value(expectedContent.getContentId()));
            resultActions.andExpect(jsonPath("$[" + i + "].contentBody").value(expectedContent.getContentBody()));
            resultActions.andExpect(jsonPath("$[" + i + "].comment").value(expectedContent.getComment()));
            resultActions.andExpect(jsonPath("$[" + i + "].contributor").value(expectedContent.getContributor()));
        }
    }

    @Test
    void returnNotFoundWhenGetHistoryOfDocumentDocumentWithNotExistDocument() throws Exception {
        // Given
        String documentName = getRandomString();
        given(documentService.getHistoryOfDocument(documentName)).willReturn(null);

        // When
        ResultActions resultActions = mockMvc.perform(get(getHistoryOfDocumentURLTemplate, documentName));

        // Then
        resultActions.andExpect(status().isNotFound());
    }
}
