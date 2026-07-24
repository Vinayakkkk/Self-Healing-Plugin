package com.vinayak.healing.ranking;

import com.vinayak.healing.model.ContextFeature;

public interface Scorer {

    double score(ContextFeature context);
}