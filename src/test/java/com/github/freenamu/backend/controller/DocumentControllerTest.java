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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

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
    private final String getLatestDocumentURLTemplate = "/api/document/{documentName}/latest/raw";
    private final String getDocumentByRevisionIndexURLTemplate = "/api/document/{documentName}/{revisionIndex}/raw";
    private final String getHistoryOfDocumentURLTemplate = "/api/document/{documentName}/history";
    private final String postDocumentURLTemplate = "/api/document/{documentName}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    void returnOKWhenPostDocumentWithFullValidInput() throws Exception {
        // Given
        String documentName = "anonymous name";
        String contentBody = "anonymous body";
        String comment = "anonymous comment";

        // When
        ResultActions resultActions = mockMvc.perform(post(postDocumentURLTemplate, documentName)
                .param("contentBody", contentBody)
                .param("comment", comment));

        // Then
        resultActions.andExpect(status().isOk());
        verify(documentService).postDocument(eq(documentName), eq(contentBody), eq(comment), anyString());
    }

    @Test
    void returnBadRequestWhenPostDocumentWithoutContentBody() throws Exception {
        // Given
        String documentName = "anonymous name";
        String comment = "anonymous comment";

        // When
        ResultActions resultActions = mockMvc.perform(post(postDocumentURLTemplate, documentName)
                .param("comment", comment));

        // Then
        resultActions.andExpect(status().isBadRequest());
        verifyNoInteractions(documentService);
    }

    @Test
    void returnOKWhenPostDocumentWithoutComment() throws Exception {
        // Given
        String documentName = "anonymous name";
        String contentBody = "anonymous comment";

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
        String documentName = "anonymous name";
        String contentBody = "anonymous body";
        String comment = "l" + "o".repeat(253) + "ng";
        doThrow(new IllegalArgumentException()).when(documentService).postDocument(eq(documentName), eq(contentBody), eq(comment), anyString());

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
        String documentName = "anonymous name";
        Content expectedContent = new Content("anonymous body", "anonymous comment", "anonymous contributor");
        expectedContent.setContentId(1L);
        given(documentService.getLatestDocument(documentName)).willReturn(expectedContent);

        // When
        ResultActions resultActions = mockMvc.perform(get(getLatestDocumentURLTemplate, documentName));

        // Then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("contentId").value(expectedContent.getContentId()));
        resultActions.andExpect(jsonPath("contentBody").value(expectedContent.getContentBody()));
        resultActions.andExpect(jsonPath("comment").value(expectedContent.getComment()));
        resultActions.andExpect(jsonPath("contributor").value(expectedContent.getContributor()));
        resultActions.andExpect(jsonPath("createDateTime").value(expectedContent.getCreateDateTime()));
    }

    @Test
    void returnNotFoundWhenGetLatestDocumentWithNotExistDocument() throws Exception {
        // Given
        String documentName = "anonymous name";
        given(documentService.getLatestDocument(documentName)).willReturn(null);

        // When
        ResultActions resultActions = mockMvc.perform(get(getLatestDocumentURLTemplate, documentName));

        // Then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void returnDocumentWhenGetDocumentByRevisionIndexWithFullValidInput() throws Exception {
        // Given
        String documentName = "anonymous name";
        int revisionIndex = 1;
        Content expectedContent = new Content("anonymous body", "anonymous comment", "anonymous contributor");
        given(documentService.getDocumentByRevisionIndex(documentName, revisionIndex)).willReturn(expectedContent);

        // When
        ResultActions resultActions = mockMvc.perform(get(getDocumentByRevisionIndexURLTemplate, documentName, revisionIndex));

        // Then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("contentId").value(expectedContent.getContentId()));
        resultActions.andExpect(jsonPath("contentBody").value(expectedContent.getContentBody()));
        resultActions.andExpect(jsonPath("comment").value(expectedContent.getComment()));
        resultActions.andExpect(jsonPath("contributor").value(expectedContent.getContributor()));
        resultActions.andExpect(jsonPath("createDateTime").value(expectedContent.getCreateDateTime()));
    }

    @Test
    void returnNotFoundWhenGetDocumentByRevisionIndexWithNotExistDocument() throws Exception {
        // Given
        String documentName = "anonymous name";
        int revisionIndex = 1;
        given(documentService.getDocumentByRevisionIndex(documentName, revisionIndex)).willReturn(null);

        // When
        ResultActions resultActions = mockMvc.perform(get(getDocumentByRevisionIndexURLTemplate, documentName, revisionIndex));

        // Then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void returnHistoryOfDocumentWhenGetHistoryOfDocumentWithFullValidInput() throws Exception {
        // Given
        String documentName = "anonymous name";
        int size = 10;
        ArrayList<HashMap<String, Object>> expectedHistoryList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            HashMap<String, Object> expectedHistoryRow = new HashMap<>();
            expectedHistoryRow.put("revisionIndex", size - i);
            expectedHistoryRow.put("comment", "anonymous comment" + i);
            expectedHistoryRow.put("contributor", "anonymous comment" + i);
            expectedHistoryRow.put("lengthDiffer", i);
            expectedHistoryRow.put("createDateTime", LocalDateTime.now().toString());
            expectedHistoryList.add(expectedHistoryRow);
        }
        given(documentService.getHistoryOfDocument(documentName)).willReturn(expectedHistoryList);

        // When
        ResultActions resultActions = mockMvc.perform(get(getHistoryOfDocumentURLTemplate, documentName));

        // Then
        resultActions.andExpect(status().isOk());
        for (int i = 0; i < size; i++) {
            HashMap<String, Object> expectedHistoryRow = expectedHistoryList.get(i);
            resultActions.andExpect(jsonPath("$[" + i + "].revisionIndex").value(expectedHistoryRow.get("revisionIndex")));
            resultActions.andExpect(jsonPath("$[" + i + "].comment").value(expectedHistoryRow.get("comment")));
            resultActions.andExpect(jsonPath("$[" + i + "].contributor").value(expectedHistoryRow.get("contributor")));
            resultActions.andExpect(jsonPath("$[" + i + "].lengthDiffer").value(expectedHistoryRow.get("lengthDiffer")));
            resultActions.andExpect(jsonPath("$[" + i + "].createDateTime").value(expectedHistoryRow.get("createDateTime")));
        }
    }

    @Test
    void returnNotFoundWhenGetHistoryOfDocumentDocumentWithNotExistDocument() throws Exception {
        // Given
        String documentName = "anonymous name";
        given(documentService.getHistoryOfDocument(documentName)).willReturn(null);

        // When
        ResultActions resultActions = mockMvc.perform(get(getHistoryOfDocumentURLTemplate, documentName));

        // Then
        resultActions.andExpect(status().isNotFound());
    }
}
