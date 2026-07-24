package com.vinayak.healing.intent;

public class IntentResolver {

    public static ElementIntent resolve(
            String variableName) {

        String name =
                variableName.toLowerCase();

        if(name.contains("user")
                || name.contains("email")
                || name.contains("search")
                || name.contains("password")) {

            return ElementIntent.INPUT;
        }

        if(name.contains("button")
                || name.contains("btn")
                || name.contains("submit")
                || name.contains("login")
                || name.contains("checkout")) {

            return ElementIntent.BUTTON;
        }

        if(name.contains("link")) {

            return ElementIntent.LINK;
        }

        if(name.contains("cart")) {

            return ElementIntent.LINK;
        }

        return ElementIntent.UNKNOWN;
    }
}