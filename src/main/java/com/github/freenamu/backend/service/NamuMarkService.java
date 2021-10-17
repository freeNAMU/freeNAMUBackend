package com.github.freenamu.backend.service;

import com.github.freenamu.node.Node;
import com.github.freenamu.node.Redirect;
import com.github.freenamu.parser.FreeNAMUParser;
import com.github.freenamu.parser.grammar.RedirectGrammar;
import com.github.freenamu.renderer.FreeNAMURenderer;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NamuMarkService {
    @Cacheable("NamuMarkServiceRenderContent")
    public String renderContent(String contentBody) {
        List<Node> article = new FreeNAMUParser().parse(contentBody);
        return new FreeNAMURenderer().render(article);
    }

    public boolean isRedirect(String contentBody) {
        return new RedirectGrammar().match(contentBody);
    }

    public String getRedirectDocumentName(String contentBody) {
        RedirectGrammar redirectGrammar = new RedirectGrammar();
        redirectGrammar.match(contentBody);
        List<Node> nodeList = redirectGrammar.parse(contentBody);
        return ((Redirect) nodeList.get(0)).getDocumentName();
    }
}
