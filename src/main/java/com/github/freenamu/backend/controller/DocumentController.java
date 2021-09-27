package com.github.freenamu.backend.controller;

import com.github.freenamu.backend.entity.Content;
import com.github.freenamu.backend.service.DocumentService;
import com.github.freenamu.backend.vo.History;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class DocumentController {
    @Autowired
    private DocumentService documentService;

    @GetMapping("/api/document/{documentName}/latest/raw")
    public ResponseEntity<Content> getLatestDocument(@PathVariable String documentName) {
        Content content = documentService.getLatestDocument(documentName);
        if (content == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(content, HttpStatus.OK);
        }
    }

    @GetMapping("/api/document/{documentName}/{revisionIndex}/raw")
    public ResponseEntity<Content> getDocument(@PathVariable String documentName, @PathVariable int revisionIndex) {
        Content content = documentService.getDocumentByRevisionIndex(documentName, revisionIndex);
        if (content == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(content, HttpStatus.OK);
        }
    }

    @GetMapping("/api/document/{documentName}/history")
    public ResponseEntity<History> getHistoryOfDocument(@PathVariable String documentName) {
        History history = documentService.getHistoryOfDocument(documentName);
        if (history == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(history, HttpStatus.OK);
        }
    }

    @PostMapping("/api/document/{documentName}")
    public ResponseEntity<Void> PostDocument(@PathVariable String documentName, @RequestParam String contentBody, @RequestParam(defaultValue = "") String comment, HttpServletRequest request) {
        try {
            documentService.postDocument(documentName, contentBody, comment, request.getRemoteAddr());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
