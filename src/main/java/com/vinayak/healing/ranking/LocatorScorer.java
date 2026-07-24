package com.vinayak.healing.ranking;

import com.vinayak.healing.model.ContextFeature;
import com.vinayak.healing.model.ElementFeature;

public class LocatorScorer implements Scorer {

    @Override
    public double score(ContextFeature context) {

        if(context == null){

            return 0;
        }

        ElementFeature feature =
                context.getElementFeature();

        if(feature == null){

            return 0;
        }

        double score = 0;

        // Highest priority

        if(!isBlank(feature.getId())){

            score += 150;
        }

        if(!isBlank(feature.getName())){

            score += 140;
        }

        // Test attributes

        if(!isBlank(feature.getDatatest())){

            score += 130;
        }

        if(!isBlank(feature.getDatatestid())){

            score += 120;
        }

        if(!isBlank(feature.getDataqa())){

            score += 120;
        }

        if(!isBlank(feature.getDatacy())){

            score += 120;
        }

        // Accessibility

        if(!isBlank(feature.getAriaLabel())){

            score += 70;
        }

        if(!isBlank(feature.getPlaceholder())){

            score += 60;
        }

        // Class should never dominate

        if(!isBlank(feature.getClassname())){

            score += 30;
        }

        return score;
    }

    private boolean isBlank(String value){

        return value == null
                || value.isBlank();
    }
}