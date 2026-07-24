package com.vinayak.healing.test;

import com.vinayak.healing.util.SimilarityUtil;

public class SimilarityTest {

    public static void main(String[] args) {

        System.out.println(
                "password score = "
                        + SimilarityUtil.score(
                                "wrong-password",
                                "password"));

        System.out.println(
                "username score = "
                        + SimilarityUtil.score(
                                "wrong-password",
                                "username"));

        System.out.println(
                "login score = "
                        + SimilarityUtil.score(
                                "wrong-password",
                                "login"));
    }
}