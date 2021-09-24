package com.github.freenamu.backend.service;

import com.github.freenamu.backend.entity.Content;
import com.github.freenamu.backend.entity.Document;
import com.github.freenamu.backend.repository.DocumentRepository;
import com.github.freenamu.backend.vo.History;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    public History getHistoryOfDocument(String documentName) {
        Optional<Document> optionalDocument = documentRepository.findById(documentName);
        if (optionalDocument.isPresent()) {
            Document document = optionalDocument.get();
            List<Content> revisions = document.getRevisions();
            History result = new History();
            for (Content content : revisions) {
                result.add(content);
            }
            return result;
        }
        return null;
    }
}
