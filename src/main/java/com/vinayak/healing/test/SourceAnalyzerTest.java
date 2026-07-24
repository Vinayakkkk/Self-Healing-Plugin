package com.vinayak.healing.test;

import com.vinayak.healing.source.SourceCodeAnalyzer;

public class SourceAnalyzerTest {

    public static void main(
            String[] args) {

        String variableName =
                new SourceCodeAnalyzer()
                        .findVariableName(
                                "src/main/java/com/vinayak/healing/demo/LoginPage.java",
                                "wrong-username");

        System.out.println(
                "Variable Name : "
                        + variableName);

                        
    }
}