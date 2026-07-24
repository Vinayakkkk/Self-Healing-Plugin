package com.vinayak.healing.dom;

import com.vinayak.healing.model.ContextInfo;
import org.jsoup.nodes.Element;

public class DomContextAnalyzer {

    public ContextInfo analyze(Element element) {

        ContextInfo info =
                new ContextInfo();

        info.setLabelText(
                findLabel(element));

        info.setParentText(
                findParentContext(element));

        info.setSectionHeading(
                findSectionHeading(element));

        return info;
    }
private String findLabel(Element element) {

    Element parent =
            element.parent();

    if(parent == null) {
        return "";
    }

    for(Element child : parent.children()) {

        if(child.tagName()
                .equalsIgnoreCase("label")) {

            return child.text();
        }
    }

    return "";
}
private String findParentContext(
        Element element) {

    Element parent =
            element.parent();

    if(parent == null) {
        return "";
    }

    return parent.tagName()
            + " "
            + parent.id()
            + " "
            + parent.className();
}
private String findSectionHeading(
        Element element) {

    Element current =
            element.parent();

    int level = 0;

    while(current != null
            && level < 5) {

        Element heading =
                current.selectFirst(
                        "h1,h2,h3,h4,h5,h6,legend");

        if(heading != null) {

            return heading.text();
        }

        current =
                current.parent();

        level++;
    }

    return "";
}
public String getFullContext(Element element) {

    StringBuilder context =
            new StringBuilder();

    String label =
            findLabel(element);

    if(!label.isBlank()) {
        context.append(label).append(" ");
    }

    String parent =
        findParentContext(element);

    if(!parent.isBlank()) {
        context.append(parent).append(" ");
    }

    String heading =
            findSectionHeading(element);

    if(!heading.isBlank()) {
        context.append(heading);
    }

    return context.toString()
            .trim();
}
}