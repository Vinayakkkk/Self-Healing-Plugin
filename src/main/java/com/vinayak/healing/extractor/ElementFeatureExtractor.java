package com.vinayak.healing.extractor;

import com.vinayak.healing.model.ElementFeature;
import com.vinayak.healing.util.TokenParser;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

public class ElementFeatureExtractor {

    public ElementFeature extract(Element element) {

        ElementFeature feature =
                new ElementFeature();

        feature.setTag(
                element.tagName());

        feature.setText(
                element.ownText());

        Map<String, String> attributes =
                new HashMap<>();

        for (Attribute attribute : element.attributes()) {

            attributes.put(
                    attribute.getKey(),
                    attribute.getValue());
        }

        feature.setAttributes(attributes);

        feature.setPlaceholder(
                element.attr("placeholder"));

        feature.setTitle(
                element.attr("title"));

        feature.setRole(
                element.attr("role"));

        feature.setAriaLabel(
                element.attr("aria-label"));

                feature.setId(
        element.id());

feature.setName(
        element.attr("name"));

feature.setClassname(
        element.className());

feature.setDatatest(
        element.attr("data-test"));

feature.setDatatestid(
        element.attr("data-testid"));

feature.setDataqa(
        element.attr("data-qa"));

feature.setDatacy(
        element.attr("data-cy"));

        feature.setTokens(
        TokenParser.parse(

                element.text()

                + " "

                + element.id()

                + " "

                + element.className()

                + " "

                + element.attr("name")

                + " "

                + element.attr("placeholder")

                + " "

                + element.attr("aria-label")

                + " "

                + element.attr("data-test")

                + " "

                + element.attr("data-testid")

                + " "

                + element.attr("data-qa")

                + " "

                + element.attr("data-cy")
        ));

        return feature;
    }

    
}