package com.vinayak.healing.ai;

public class PromptBuilder {

public static String build(
        String variableName,
        String failedLocator,
        String dom) {

    return """
You are an AI Selenium Self-Healing Engine.

Variable Name:
%s

Failed Locator:
%s

Available Elements:
%s

Rules:

1. The failed locator does not exist.
2. Use Variable Name as the strongest clue.
3. Never return the failed locator.
4. Return ONLY JSON.

{
  "locatorType":"name",
  "locatorValue":"username",
  "confidence":100
}
"""
            .formatted(
                    variableName,
                    failedLocator,
                    dom);
}
}