package com.vinayak.healing.policy;

public class SourceRepairPolicy {

    private static final int MIN_SUCCESS_COUNT = 1;

    public boolean canRepair(
            int successfulHealCount) {

        return successfulHealCount
                >= MIN_SUCCESS_COUNT;
    }
}