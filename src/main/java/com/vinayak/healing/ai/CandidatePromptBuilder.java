package com.vinayak.healing.ai;
import com.vinayak.healing.ai.AiElementChoice;
import com.vinayak.healing.model.FailureContext;
import com.vinayak.healing.model.LocatorCandidate;

import java.util.List;

public class CandidatePromptBuilder {

    public static String build(
            FailureContext context,
            List<LocatorCandidate> candidates) {

        StringBuilder prompt =
                new StringBuilder();

        prompt.append("""
You are an expert Selenium Self-Healing Engine.

Your task is to select ONLY ONE candidate.

IMPORTANT RULES

- Never invent new locator values.
- Never invent new locator types.
- Choose ONLY from the supplied candidates.
- Prefer id over name over data-* over class over xpath.
- Ignore visually similar but semantically unrelated elements.
- Prefer candidates matching the expected tag.
- Prefer candidates matching the expected intent.
- Prefer candidates with higher score.

""");

        prompt.append("Failure Information\n");
        prompt.append("--------------------\n");

        prompt.append("Failed Locator : ")
                .append(context.getFailedLocator())
                .append("\n");

        prompt.append("Variable Name : ")
                .append(context.getVariableName())
                .append("\n");

        prompt.append("Expected Tag : ")
                .append(context.getExpectedTag())
                .append("\n");

        prompt.append("Expected Intent : ")
                .append(context.getExpectedIntent())
                .append("\n");

        if(context.getLocatorDeclaration()!=null){

            prompt.append("Original Declaration : ")
                    .append(context.getLocatorDeclaration())
                    .append("\n");
        }

        prompt.append("\n");

        prompt.append("Candidates\n");
        prompt.append("----------\n\n");

        int i=1;

        for(LocatorCandidate candidate:candidates){

            prompt.append("Candidate ")
                    .append(i++)
                    .append("\n");

            prompt.append("locatorType=")
                    .append(candidate.getLocatorType())
                    .append("\n");

            prompt.append("locatorValue=")
                    .append(candidate.getLocatorValue())
                    .append("\n");

            prompt.append("tag=")
                    .append(candidate.getTagName())
                    .append("\n");

            prompt.append("inputType=")
                    .append(candidate.getInputType())
                    .append("\n");

            prompt.append("intent=")
                    .append(candidate.getIntent())
                    .append("\n");

            prompt.append("score=")
                    .append(candidate.getFinalScore())
                    .append("\n\n");
        }

        prompt.append("""
Return ONLY valid JSON.

{
  "locatorType":"id",
  "locatorValue":"username",
  "confidence":100
}
""");

        return prompt.toString();
    }

    public static String buildChoicePrompt(
        com.vinayak.healing.model.FailureContext context,
        java.util.List<AiElementChoice> choices) {

    StringBuilder prompt =
            new StringBuilder();

    prompt.append("""
You are selecting one already-found browser element.

Do not invent a locator.
Do not return locatorType or locatorValue.
Choose exactly one candidate index.

Failure context:
""");

    prompt.append("variableName = ")
            .append(context.getVariableName())
            .append("\n");

    prompt.append("expectedIntent = ")
            .append(context.getExpectedIntent())
            .append("\n");

    prompt.append("expectedTag = ")
            .append(context.getExpectedTag())
            .append("\n\n");

    for (AiElementChoice choice : choices) {

        prompt.append("Candidate ")
                .append(choice.getIndex())
                .append("\n");

        prompt.append("locatorType = ")
                .append(choice.getLocatorType())
                .append("\n");

        prompt.append("locatorValue = ")
                .append(choice.getLocatorValue())
                .append("\n");

        prompt.append("tag = ")
                .append(choice.getTag())
                .append("\n");

        prompt.append("text = ")
                .append(choice.getText())
                .append("\n");

        prompt.append("class = ")
                .append(choice.getCssClass())
                .append("\n");

        prompt.append("parentTag = ")
                .append(choice.getParentTag())
                .append("\n");

        prompt.append("parentClass = ")
                .append(choice.getParentClass())
                .append("\n");

        prompt.append("parentHref = ")
                .append(choice.getParentHref())
                .append("\n\n");
    }

    prompt.append("""
Return only JSON:
{"candidateIndex": 1}
""");

    return prompt.toString();
}
}