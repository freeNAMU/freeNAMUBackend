package com.github.freenamu.backend.controller;

import com.github.freenamu.backend.entity.Content;
import com.github.freenamu.backend.service.DocumentService;
import com.github.freenamu.backend.service.NamuMarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class DocumentController {
    @Autowired
    private DocumentService documentService;
    @Autowired
    private NamuMarkService namuMarkService;

    @GetMapping("/api/document/raw/**")
    public ResponseEntity<Content> getRawDocument(HttpServletRequest request, @RequestParam(required = false) Integer rev) {
        String documentName = getDocumentName(request);
        Content content;
        if (rev == null) {
            content = documentService.getLatestDocument(documentName);
        } else {
            content = documentService.getDocumentByRevisionIndex(documentName, rev);
        }
        if (content == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(content, HttpStatus.OK);
        }
    }

    @GetMapping("/api/document/render/**")
    public ResponseEntity<Content> getRenderedDocument(HttpServletRequest request, @RequestParam(required = false) Integer rev) {
        String documentName = getDocumentName(request);
        Content content;
        if (rev == null) {
            content = documentService.getLatestDocument(documentName);
        } else {
            content = documentService.getDocumentByRevisionIndex(documentName, rev);
        }
        if (content == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            String rawContentBody = content.getContentBody();
            if (namuMarkService.isRedirect(rawContentBody)) {
                content.setContentBody(namuMarkService.getRedirectDocumentName(rawContentBody));
                return new ResponseEntity<>(content, HttpStatus.MOVED_PERMANENTLY);
            } else {
                String renderedContentBody = namuMarkService.renderContent(rawContentBody);
                content.setContentBody(renderedContentBody);
                return new ResponseEntity<>(content, HttpStatus.OK);
            }
        }
    }

    @GetMapping("/api/document/history/**")
    public ResponseEntity<ArrayList<HashMap<String, Object>>> getHistoryOfDocument(HttpServletRequest request) {
        String documentName = getDocumentName(request);
        ArrayList<HashMap<String, Object>> history = documentService.getHistoryOfDocument(documentName);
        if (history == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(history, HttpStatus.OK);
        }
    }

    @PostMapping("/api/document/raw/**")
    public ResponseEntity<Void> PostDocument(HttpServletRequest request, @RequestParam String contentBody, @RequestParam(defaultValue = "") String comment) {
        String documentName = getDocumentName(request);
        try {
            documentService.postDocument(documentName, contentBody, comment, request.getRemoteAddr());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String getDocumentName(HttpServletRequest request) {
        String encodedDocumentName = request.getRequestURI();
        for (int i = 0; i < 3; i++) {
            encodedDocumentName = encodedDocumentName.replaceFirst("/", "");
        }
        encodedDocumentName = encodedDocumentName.substring(encodedDocumentName.indexOf('/') + 1);
        return UriUtils.decode(encodedDocumentName, StandardCharsets.UTF_8);
    }
}
