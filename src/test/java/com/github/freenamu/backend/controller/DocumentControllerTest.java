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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
    private final String getRawDocumentURLTemplate = "/api/document/raw/{documentName}";
    private final String getRenderedDocumentURLTemplate = "/api/document/render/{documentName}";
    private final String getHistoryOfDocumentURLTemplate = "/api/document/history/{documentName}";
    private final String postDocumentURLTemplate = "/api/document/raw/{documentName}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    public void return_ok_when_post_document_with_full_valid_input() throws Exception {
        // Given
        String documentName = "anonymous name";
        String contentBody = "anonymous body";
        String comment = "anonymous comment";

        // When
        ResultActions resultActions = mockMvc.perform(post(postDocumentURLTemplate, documentName)
                .param("contentBody", contentBody)
                .param("comment", comment));

        // Then
        verify(documentService, times(1)).postDocument(eq(documentName), eq(contentBody), eq(comment), anyString());
        resultActions.andExpect(status().isOk());
    }

    @Test
    public void return_bad_request_when_post_document_without_content_body() throws Exception {
        // Given
        String documentName = "anonymous name";
        String comment = "anonymous comment";

        // When
        ResultActions resultActions = mockMvc.perform(post(postDocumentURLTemplate, documentName)
                .param("comment", comment));

        // Then
        verifyNoInteractions(documentService);
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    public void return_ok_when_post_document_without_comment() throws Exception {
        // Given
        String documentName = "anonymous name";
        String contentBody = "anonymous comment";

        // When
        ResultActions resultActions = mockMvc.perform(post(postDocumentURLTemplate, documentName)
                .param("contentBody", contentBody));

        // Then
        verify(documentService).postDocument(eq(documentName), eq(contentBody), eq(""), anyString());
        resultActions.andExpect(status().isOk());
    }

    @Test
    public void return_bad_request_when_post_document_with_long_comment() throws Exception {
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
    public void return_ok_when_post_document_with_document_name_containing_slash() throws Exception {
        // Given
        String documentName = "anonymous/name";
        String contentBody = "anonymous body";
        String comment = "anonymous comment";

        // When
        ResultActions resultActions = mockMvc.perform(post(postDocumentURLTemplate, documentName)
                .param("contentBody", contentBody)
                .param("comment", comment));

        // Then
        verify(documentService, times(1)).postDocument(eq(documentName), eq(contentBody), eq(comment), anyString());
        resultActions.andExpect(status().isOk());
    }

    @Test
    public void return_latest_raw_document_when_get_raw_document_without_revision_index() throws Exception {
        // Given
        String documentName = "anonymous name";
        Content expectedContent = new Content("anonymous body", "anonymous comment", "anonymous contributor");
        expectedContent.setContentId(1L);
        given(documentService.getLatestDocument(documentName)).willReturn(expectedContent);

        // When
        ResultActions resultActions = mockMvc.perform(get(getRawDocumentURLTemplate, documentName));

        // Then
        verify(documentService, never()).renderContent(expectedContent.getContentBody());
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("contentId").value(expectedContent.getContentId()));
        resultActions.andExpect(jsonPath("contentBody").value(expectedContent.getContentBody()));
        resultActions.andExpect(jsonPath("comment").value(expectedContent.getComment()));
        resultActions.andExpect(jsonPath("contributor").value(expectedContent.getContributor()));
        resultActions.andExpect(jsonPath("createDateTime").value(expectedContent.getCreateDateTime()));
    }

    @Test
    public void return_latest_raw_document_when_get_raw_document_with_document_name_containing_slash() throws Exception {
        // Given
        String documentName = "anonymous/name";
        Content expectedContent = new Content("anonymous body", "anonymous comment", "anonymous contributor");
        expectedContent.setContentId(1L);
        given(documentService.getLatestDocument(documentName)).willReturn(expectedContent);

        // When
        ResultActions resultActions = mockMvc.perform(get(getRawDocumentURLTemplate, documentName));

        // Then
        verify(documentService, never()).renderContent(expectedContent.getContentBody());
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("contentId").value(expectedContent.getContentId()));
        resultActions.andExpect(jsonPath("contentBody").value(expectedContent.getContentBody()));
        resultActions.andExpect(jsonPath("comment").value(expectedContent.getComment()));
        resultActions.andExpect(jsonPath("contributor").value(expectedContent.getContributor()));
        resultActions.andExpect(jsonPath("createDateTime").value(expectedContent.getCreateDateTime()));
    }

    @Test
    public void return_not_found_when_get_raw_document_with_not_exist_document() throws Exception {
        // Given
        String documentName = "anonymous name";
        given(documentService.getLatestDocument(documentName)).willReturn(null);

        // When
        ResultActions resultActions = mockMvc.perform(get(getRawDocumentURLTemplate, documentName));

        // Then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void return_document_when_get_raw_document_with_revision_index() throws Exception {
        // Given
        String documentName = "anonymous name";
        int revisionIndex = 1;
        Content expectedContent = new Content("anonymous body", "anonymous comment", "anonymous contributor");
        given(documentService.getDocumentByRevisionIndex(documentName, revisionIndex)).willReturn(expectedContent);

        // When
        MockHttpServletRequestBuilder requestBuilder = get(getRawDocumentURLTemplate, documentName);
        requestBuilder.queryParam("rev", String.valueOf(revisionIndex));
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        verify(documentService, never()).renderContent(expectedContent.getContentBody());
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("contentId").value(expectedContent.getContentId()));
        resultActions.andExpect(jsonPath("contentBody").value(expectedContent.getContentBody()));
        resultActions.andExpect(jsonPath("comment").value(expectedContent.getComment()));
        resultActions.andExpect(jsonPath("contributor").value(expectedContent.getContributor()));
        resultActions.andExpect(jsonPath("createDateTime").value(expectedContent.getCreateDateTime()));
    }

    @Test
    public void return_not_found_when_get_raw_document_with_not_exist_revision_index() throws Exception {
        // Given
        String documentName = "anonymous name";
        int revisionIndex = 1;
        given(documentService.getDocumentByRevisionIndex(documentName, revisionIndex)).willReturn(null);

        // When
        MockHttpServletRequestBuilder requestBuilder = get(getRawDocumentURLTemplate, documentName);
        requestBuilder.queryParam("rev", String.valueOf(revisionIndex));
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void return_latest_rendered_document_when_get_rendered_document_without_revision_index() throws Exception {
        // Given
        String documentName = "anonymous name";
        String expectedContentBody = "anonymous body";
        Content expectedContent = new Content(expectedContentBody, "anonymous comment", "anonymous contributor");
        expectedContent.setContentId(1L);
        given(documentService.getLatestDocument(documentName)).willReturn(expectedContent);

        // When
        ResultActions resultActions = mockMvc.perform(get(getRenderedDocumentURLTemplate, documentName));

        // Then
        verify(documentService, times(1)).renderContent(expectedContentBody);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("contentId").value(expectedContent.getContentId()));
        resultActions.andExpect(jsonPath("contentBody").value(expectedContent.getContentBody()));
        resultActions.andExpect(jsonPath("comment").value(expectedContent.getComment()));
        resultActions.andExpect(jsonPath("contributor").value(expectedContent.getContributor()));
        resultActions.andExpect(jsonPath("createDateTime").value(expectedContent.getCreateDateTime()));
    }

    @Test
    public void return_latest_rendered_document_when_get_rendered_document_with_document_name_containing_slash() throws Exception {
        // Given
        String documentName = "anonymous/name";
        String expectedContentBody = "anonymous body";
        Content expectedContent = new Content(expectedContentBody, "anonymous comment", "anonymous contributor");
        expectedContent.setContentId(1L);
        given(documentService.getLatestDocument(documentName)).willReturn(expectedContent);

        // When
        ResultActions resultActions = mockMvc.perform(get(getRenderedDocumentURLTemplate, documentName));

        // Then
        verify(documentService, times(1)).renderContent(expectedContentBody);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("contentId").value(expectedContent.getContentId()));
        resultActions.andExpect(jsonPath("contentBody").value(expectedContent.getContentBody()));
        resultActions.andExpect(jsonPath("comment").value(expectedContent.getComment()));
        resultActions.andExpect(jsonPath("contributor").value(expectedContent.getContributor()));
        resultActions.andExpect(jsonPath("createDateTime").value(expectedContent.getCreateDateTime()));
    }

    @Test
    public void return_not_found_when_get_render_document_with_not_exist_document() throws Exception {
        // Given
        String documentName = "anonymous name";
        given(documentService.getLatestDocument(documentName)).willReturn(null);

        // When
        ResultActions resultActions = mockMvc.perform(get(getRenderedDocumentURLTemplate, documentName));

        // Then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void return_document_when_get_render_document_with_revision_index() throws Exception {
        // Given
        String documentName = "anonymous name";
        int revisionIndex = 1;
        String expectedContentBody = "anonymous body";
        Content expectedContent = new Content(expectedContentBody, "anonymous comment", "anonymous contributor");
        given(documentService.getDocumentByRevisionIndex(documentName, revisionIndex)).willReturn(expectedContent);

        // When
        MockHttpServletRequestBuilder requestBuilder = get(getRenderedDocumentURLTemplate, documentName);
        requestBuilder.queryParam("rev", String.valueOf(revisionIndex));
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        verify(documentService, times(1)).renderContent(expectedContentBody);
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("contentId").value(expectedContent.getContentId()));
        resultActions.andExpect(jsonPath("contentBody").value(expectedContent.getContentBody()));
        resultActions.andExpect(jsonPath("comment").value(expectedContent.getComment()));
        resultActions.andExpect(jsonPath("contributor").value(expectedContent.getContributor()));
        resultActions.andExpect(jsonPath("createDateTime").value(expectedContent.getCreateDateTime()));
    }

    @Test
    public void return_not_found_when_get_render_document_with_not_exist_revision_index() throws Exception {
        // Given
        String documentName = "anonymous name";
        int revisionIndex = 1;
        given(documentService.getDocumentByRevisionIndex(documentName, revisionIndex)).willReturn(null);

        // When
        MockHttpServletRequestBuilder requestBuilder = get(getRenderedDocumentURLTemplate, documentName);
        requestBuilder.queryParam("rev", String.valueOf(revisionIndex));
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        // Then
        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void return_history_of_document_when_get_history_of_document_with_full_valid_input() throws Exception {
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
    public void return_history_of_document_when_get_history_of_document_with_document_name_containing_slash() throws Exception {
        // Given
        String documentName = "anonymous/name";
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
    public void return_not_found_when_get_history_of_document_document_with_not_exist_document() throws Exception {
        // Given
        String documentName = "anonymous name";
        given(documentService.getHistoryOfDocument(documentName)).willReturn(null);

        // When
        ResultActions resultActions = mockMvc.perform(get(getHistoryOfDocumentURLTemplate, documentName));

        // Then
        resultActions.andExpect(status().isNotFound());
    }
}
