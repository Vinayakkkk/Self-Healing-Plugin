package com.vinayak.healing.model;

/**
 * Describes the expected number of elements
 * represented by a failed automation target.
 */
public enum TargetCardinality {

    /**
     * The automation expects exactly one element.
     */
    SINGLE,

    /**
     * The automation expects multiple elements.
     */
    COLLECTION,

    /**
     * The framework cannot determine the target cardinality yet.
     */
    UNKNOWN
}