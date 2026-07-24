package com.vinayak.healing.test;

import com.vinayak.healing.analyzer.LocatorAnalyzer;
import com.vinayak.healing.model.LocatorInfo;

public class TestLocatorAnalyzer {

    public static void main(String[] args) {

        LocatorAnalyzer analyzer = new LocatorAnalyzer();

        String[] locators = {

                "By.id: username",

                "By.name: employeeName",

                "By.className: inventory_item_name",

                "By.cssSelector: button[type='submit']",

                "By.xpath: //button[text()='Login']",

                "By.tagName: input",

                "By.linkText: Login"
        };

        for (String locator : locators) {

            System.out.println("\n=======================================");
            System.out.println("FAILED LOCATOR : " + locator);

            LocatorInfo info = analyzer.analyze(locator);

            System.out.println("Original Locator : " + info.getOriginalLocator());
            System.out.println("Locator Type     : " + info.getLocatorType());
            System.out.println("Attribute        : " + info.getAttribute());
            System.out.println("Attribute Value  : " + info.getAttributeValue());
            System.out.println("Tokens           : " + info.getLocatorTokens());
            System.out.println("Confidence       : " + info.getConfidence());
        }
    }
}