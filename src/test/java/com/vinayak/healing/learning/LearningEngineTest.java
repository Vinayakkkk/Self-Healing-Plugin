

package com.vinayak.healing.learning;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class LearningEngineTest {

    private final LearningRepository repository =
            new LearningRepository();

    private final LearningEngine engine =
            new LearningEngine(repository);

    @AfterMethod
    public void cleanup() {
        engine.clear();
    }

    @Test
    public void shouldRecordAndRetrieveLearningRecord() {

        LearningKey key =
                new LearningKey(
                        "pages.LoginPage",
                        "usernameField",
                        "INPUT",
                        "SEND_KEYS",
                        "By.id: oldUsername");

        LearningRecord record =
                new LearningRecord(
                        key,
                        "By.name: username",
                        "name",
                        "username",
                        950.0,
                        "DETERMINISTIC",
                        "HIGH",
                        true,
                        true,
                        true,
                        95.0);

        engine.record(record);

        List<LearningRecord> history =
                engine.findHistory(key);

        assertEquals(history.size(), 1);

        LearningRecord stored =
                history.get(0);

        assertEquals(
                stored.getSelectedLocator(),
                "By.name: username");

        assertTrue(
                stored.isOutcomeSuccess());

        assertEquals(
                stored.getCandidateScore(),
                950.0);

        assertEquals(
                stored.getOutcomeConfidence(),
                95.0);
    }

    @Test
    public void shouldCalculateLearningStatistics() {

        LearningKey key =
                new LearningKey(
                        "pages.LoginPage",
                        "usernameField",
                        "INPUT",
                        "SEND_KEYS",
                        "By.id: oldUsername");

        LearningRecord success =
                new LearningRecord(
                        key,
                        "By.name: username",
                        "name",
                        "username",
                        900.0,
                        "DETERMINISTIC",
                        "HIGH",
                        true,
                        true,
                        true,
                        95.0);

        LearningRecord failure =
                new LearningRecord(
                        key,
                        "By.xpath: //input",
                        "xpath",
                        "//input",
                        700.0,
                        "AI",
                        "LOW",
                        true,
                        false,
                        false,
                        30.0);

        engine.record(success);
        engine.record(failure);

        LearningStatistics statistics =
                engine.getStatistics(key);

        assertEquals(
                statistics.getAttemptCount(),
                2);

        assertEquals(
                statistics.getSuccessCount(),
                1);

        assertEquals(
                statistics.getFailureCount(),
                1);

        assertEquals(
                statistics.getSuccessRate(),
                50.0);

        assertEquals(
                statistics.getAverageScore(),
                800.0);
    }

    @Test
    public void shouldKeepDifferentLearningContextsSeparate() {

        LearningKey usernameKey =
                new LearningKey(
                        "pages.LoginPage",
                        "usernameField",
                        "INPUT",
                        "SEND_KEYS",
                        "By.id: oldUsername");

        LearningKey passwordKey =
                new LearningKey(
                        "pages.LoginPage",
                        "passwordField",
                        "INPUT",
                        "SEND_KEYS",
                        "By.id: oldPassword");

        LearningRecord usernameRecord =
                new LearningRecord(
                        usernameKey,
                        "By.name: username",
                        "name",
                        "username",
                        900.0,
                        "DETERMINISTIC",
                        "HIGH",
                        true,
                        true,
                        true,
                        95.0);

        engine.record(usernameRecord);

        assertEquals(
                engine.findHistory(usernameKey).size(),
                1);

        assertTrue(
                engine.findHistory(passwordKey).isEmpty());
    }

    @Test
    public void shouldReturnEmptyHistoryForUnknownKey() {

        LearningKey key =
                new LearningKey(
                        "pages.AdminPage",
                        "searchField",
                        "INPUT",
                        "SEND_KEYS",
                        "By.id: oldSearch");

        List<LearningRecord> history =
                engine.findHistory(key);

        assertNotNull(history);

        assertTrue(history.isEmpty());
    }
}
