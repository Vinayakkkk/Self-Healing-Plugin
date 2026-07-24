package com.vinayak.healing.test;

import com.vinayak.healing.ai.AiModelClient;
import com.vinayak.healing.ai.AiResponseParser;
import com.vinayak.healing.ai.LocatorSuggestion;
import com.vinayak.healing.ai.PromptBuilder;

public class AiTest {

    public static void main(String[] args)
            throws Exception {

String prompt =
        PromptBuilder.build(
                "username",
                "wrong-username",
                """
                <input name="username"
                       placeholder="Username">

                <input name="password"
                       placeholder="Password">
                """);

        String response =
                new AiModelClient()
                        .ask(prompt);

        System.out.println(response);


        LocatorSuggestion suggestion =
        new AiResponseParser()
                .parse(response);

System.out.println(
        "TYPE : "
                + suggestion.getLocatorType());

System.out.println(
        "VALUE : "
                + suggestion.getLocatorValue());

System.out.println(
        "CONFIDENCE : "
                + suggestion.getConfidence());
    }
}