package com.vinayak.healing.test;

import com.vinayak.healing.util.TokenParser;

public class TestTokenParser {

    public static void main(String[] args) {

        test("employeeName");

        test("shopping_cart_badge");

        test("shoppingCartBadge");

        test("item-4-title-link");

        test("ADD_TO_CART");

        test("btn.inventory.primary");

        test("input[name='username']");

        test("//button[text()='Login']");
    }

    private static void test(String value) {

        System.out.println("\n================================");
        System.out.println(value);
        System.out.println(TokenParser.parse(value));
    }
}