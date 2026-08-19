package com.vinayak.healing.cache;

import org.testng.annotations.Test;

public class LocatorCacheClearTest {

    @Test
    public void clearLocatorCache() {

        System.out.println(
                "========== CLEARING LOCATOR CACHE ==========");

        System.out.println(
                "Cache size before clear : "
                        + LocatorCache.size());

        LocatorCache.clear();

        System.out.println(
                "Cache size after clear  : "
                        + LocatorCache.size());

        System.out.println(
                "=============================================");
    }
}