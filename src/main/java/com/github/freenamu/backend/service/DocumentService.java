package com.github.freenamu.backend.service;

import com.github.freenamu.backend.entity.Content;
import com.github.freenamu.backend.entity.Document;
import com.github.freenamu.backend.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class DocumentService {
    @Autowired
    private DocumentRepository documentRepository;

    public void postDocument(String documentName, String contentBody, String comment, String contributor) throws IllegalArgumentException {
        Optional<Document> optionalDocument = documentRepository.findById(documentName);
        Document document;
        if (optionalDocument.isEmpty()) {
            document = new Document();
            document.setDocumentName(documentName);
        } else {
            document = optionalDocument.get();
        }
        Content content = new Content();
        content.setContentBody(contentBody);
        content.setComment(comment);
        content.setContributor(contributor);
        document.addContent(content);
        documentRepository.save(document);
    }

    public Content getLatestDocument(String documentName) {
        Optional<Document> optionalDocument = documentRepository.findById(documentName);
        if (optionalDocument.isPresent()) {
            Document document = optionalDocument.get();
            List<Content> revisions = document.getRevisions();
            return revisions.stream().max(Comparator.comparing(Content::getContentId)).get();
        }
        return null;
    }

    public Content getDocumentByRevisionIndex(String documentName, int revisionIndex) {
        Optional<Document> optionalDocument = documentRepository.findById(documentName);
        if (optionalDocument.isPresent()) {
            Document document = optionalDocument.get();
            List<Content> revisions = document.getRevisions();
            if (1 <= revisionIndex && revisionIndex <= revisions.size()) {
                return revisions.get(revisionIndex - 1);
            }
        }
        return null;
    }

    public ArrayList<HashMap<String, Object>> getHistoryOfDocument(String documentName) {
        Optional<Document> optionalDocument = documentRepository.findById(documentName);
        if (optionalDocument.isPresent()) {
            Document document = optionalDocument.get();
            List<Content> revisions = document.getRevisions();
            ArrayList<HashMap<String, Object>> result = new ArrayList<>();
            int revisionIndex = 1;
            int previousLength = 0;
            for (Content content : revisions) {
                HashMap<String, Object> historyRow = new HashMap<>();
                historyRow.put("revisionIndex", revisionIndex++);
                historyRow.put("comment", content.getComment());
                historyRow.put("contributor", content.getContributor());
                historyRow.put("lengthDiffer", content.getContentBody().length() - previousLength);
                historyRow.put("createDateTime", content.getCreateDateTime());
                previousLength = content.getContentBody().length();
                result.add(historyRow);
            }
            Collections.reverse(result);
            return result;
        }
        return null;
    }
}
