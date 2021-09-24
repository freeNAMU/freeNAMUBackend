package com.github.freenamu.backend.controller;

import com.github.freenamu.backend.entity.Content;
import com.github.freenamu.backend.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class DocumentController {
    @Autowired
    private DocumentService documentService;

    @GetMapping("/document/{documentName}/latest/raw")
    public ResponseEntity<Content> getLatestDocument(@PathVariable String documentName) {
        Content content = documentService.getLatestDocument(documentName);
        if (content == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(content, HttpStatus.OK);
        }
    }

    @GetMapping("/document/{documentName}/{revisionIndex}/raw")
    public ResponseEntity<Content> getDocument(@PathVariable String documentName, @PathVariable int revisionIndex) {
        Content content = documentService.getDocumentByRevisionIndex(documentName, revisionIndex);
        if (content == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(content, HttpStatus.OK);
        }
    }

    @GetMapping("/document/{documentName}/history")
    public ResponseEntity<List<Content>> getHistoryOfDocument(@PathVariable String documentName) {
        List<Content> contentList = documentService.getHistoryOfDocument(documentName);
        if (contentList == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(contentList, HttpStatus.OK);
        }
    }

    @PostMapping("/document/{documentName}")
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
