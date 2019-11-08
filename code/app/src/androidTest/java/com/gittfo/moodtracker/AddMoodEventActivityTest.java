package com.gittfo.moodtracker;

import android.app.Activity;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.gittfo.moodtracker.views.addmood.AddMoodEventActivity;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * Tests for AddMoodEventActivity
 */
public class AddMoodEventActivityTest {
    private Solo solo;

    // Create a rule for the AddMoodEventActivity
    @Rule
    public ActivityTestRule<AddMoodEventActivity> rule =
            new ActivityTestRule<>(AddMoodEventActivity.class, true, true);

    /**
     * Runs before all tests and creates a solo instance
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
    }

    /**
     * Ensure we can get the appropriate activity
     *
     * @throws Exception
     */
    @Test
    public void start() throws Exception {
        Activity activity = rule.getActivity();
    }

    /**
     * Test the exit button
     *
     * @throws Exception
     */
    @Test
    public void checkExit() throws Exception {
        solo.assertCurrentActivity("Wrong Activity", AddMoodEventActivity.class);

        // Click on the exit button
        solo.clickOnView(solo.getView("@id/exit_button"));

        // Wait for the alert dialog to appear
        assertTrue(solo.waitForText("Exit Mood Event Creation", 1, 2000));

        // Test the alert dialog's "Cancel" button
        solo.clickOnButton("Cancel");
        solo.assertCurrentActivity("Wrong Activity", AddMoodEventActivity.class);

        // Test the alert dialog's "OK" button
        solo.clickOnView(solo.getView("@id/exit_button"));
        solo.clickOnButton("OK");
    }

    // After testing is complete, ensure we've closed all opened activities
    @After
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
