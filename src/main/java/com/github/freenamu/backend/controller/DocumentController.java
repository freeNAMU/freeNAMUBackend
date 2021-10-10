package com.github.freenamu.backend.controller;

import com.github.freenamu.backend.entity.Content;
import com.github.freenamu.backend.service.DocumentService;
import com.github.freenamu.node.Node;
import com.github.freenamu.parser.FreeNAMUParser;
import com.github.freenamu.renderer.FreeNAMURenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    @GetMapping("/api/document/{documentName}/latest")
    public ResponseEntity<Content> getLatestRenderedDocument(@PathVariable String documentName) {
        Content content = documentService.getLatestDocument(documentName);
        if (content == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            renderContentBody(content);
            return new ResponseEntity<>(content, HttpStatus.OK);
        }
    }

    @GetMapping("/api/document/{documentName}/{revisionIndex}")
    public ResponseEntity<Content> getRenderedDocument(@PathVariable String documentName, @PathVariable int revisionIndex) {
        Content content = documentService.getDocumentByRevisionIndex(documentName, revisionIndex);
        if (content == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            renderContentBody(content);
            return new ResponseEntity<>(content, HttpStatus.OK);
        }
    }

    @GetMapping("/api/document/{documentName}/history")
    public ResponseEntity<ArrayList<HashMap<String, Object>>> getHistoryOfDocument(@PathVariable String documentName) {
        ArrayList<HashMap<String, Object>> history = documentService.getHistoryOfDocument(documentName);
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

    private void renderContentBody(Content content) {
        List<Node> article = new FreeNAMUParser().parse(content.getContentBody());
        String renderedContentBody = new FreeNAMURenderer().render(article);
        content.setContentBody(renderedContentBody);
    }
}
