package com.vinayak.healing.test;


import com.vinayak.healing.analyzer.VariableAnalyzer;
import com.vinayak.healing.model.VariableInfo;

public class TestVariableAnalyzer {

    public static void main(String[] args) {

        VariableAnalyzer analyzer =
                new VariableAnalyzer();

        test(analyzer, "username");
        test(analyzer, "loginButton");
        test(analyzer, "employeeName");
        test(analyzer, "productsTitle");
        test(analyzer, "shoppingCartBadge");
        test(analyzer, "menuIcon");
        test(analyzer, "searchInput");
        test(analyzer, "profileImage");
    }

    private static void test(
            VariableAnalyzer analyzer,
            String variable) {

        VariableInfo info =
                analyzer.analyze(variable);

        System.out.println("\n==============================");
        System.out.println("Variable : " + variable);
        System.out.println(info);
    }
}
