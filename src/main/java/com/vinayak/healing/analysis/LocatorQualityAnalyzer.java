package com.vinayak.healing.analysis;

import com.vinayak.healing.model.LocatorCandidate;

public class LocatorQualityAnalyzer {

    public double calculateScore(
            LocatorCandidate candidate) {

        if (candidate == null
                || candidate.getLocatorType() == null) {
            return 0;
        }

        String type =
                candidate.getLocatorType()
                        .toLowerCase();

        switch (type) {

            case "data-testid":
                return 500;

            case "data-test":
                return 480;

            case "data-qa":
                return 470;

            case "data-cy":
                return 470;

            case "id":
                return 450;

            case "name":
                return 400;

            case "aria-label":
                return 300;

            case "role":
                return 250;

            case "title":
                return 200;

            case "placeholder":
                return 150;

            case "href":
                return 120;

            case "text":
                return 100;

            case "xpath":

                if (candidate.isUniqueLocator()) {
                    return 120;
                }

                return 40;

            case "class":
                return -100;

            default:
                return 0;
        }
    }
}