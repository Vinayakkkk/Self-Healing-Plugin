package com.vinayak.healing.builder;

import com.vinayak.healing.context.FallbackContextResolver;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.TargetCardinality;
import com.vinayak.healing.source.PageObjectResolver;
import com.vinayak.healing.source.SourceCodeAnalyzer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class FailureContextFactory {

    private final PageObjectResolver pageObjectResolver =
            new PageObjectResolver();

    private final SourceCodeAnalyzer analyzer =
            new SourceCodeAnalyzer();

    private final FallbackContextResolver fallbackContextResolver =
            new FallbackContextResolver();

    private final FailureContextBuilder builder =
            new FailureContextBuilder();

   public FailureContext build(
        WebDriver driver,
        By locator,
        TargetCardinality targetCardinality) {

        String pageObjectPath =
                pageObjectResolver.findPageObjectFromStackTrace();

        String variableName = "";
        String declaration = "";

        if (pageObjectPath != null) {

            variableName =
                    analyzer.findVariableName(
                            pageObjectPath,
                            locator.toString());

            declaration =
                    analyzer.findDeclaration(
                            pageObjectPath,
                            locator.toString());
        }

        FailureContext context =
                builder.build(
                        driver,
                        null,
                        locator.toString(),
                        variableName,
                        declaration);

        context.setPageObjectPath(pageObjectPath);

        context.setTargetCardinality(
        targetCardinality);

        if (variableName == null
                || variableName.isBlank()) {

            context =
                    fallbackContextResolver.enrich(
                            context,
                            locator);

            variableName =
                    context.getVariableName();
        }
if (variableName == null
        || variableName.isBlank()) {

    variableName = "DIRECT_LOCATOR";
}

context.setVariableName(variableName);
        return context;
    }
}