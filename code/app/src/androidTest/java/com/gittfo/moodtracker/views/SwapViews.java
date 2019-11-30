package com.gittfo.moodtracker.views;

import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.gittfo.moodtracker.database.Database;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.Context.MODE_PRIVATE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SwapViews {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class, true, true) {

        @Override
        protected void beforeActivityLaunched() {
            InstrumentationRegistry.getInstrumentation().getTargetContext().getSharedPreferences(Database.PREFS, MODE_PRIVATE)
                    .edit()
                    .putString("user", "swapViews")
                    .apply();
        }
    };

    @Test
    public void swapViews() {
        TestUtil.gotoTimeline();
        TestUtil.isTimeline();

        TestUtil.gotoInbox();
        TestUtil.isInbox();

        TestUtil.gotoProfile();
        TestUtil.isProfile();

        TestUtil.gotoTimeline();
        TestUtil.isTimeline();

        TestUtil.gotoMaps();
        TestUtil.isMaps();

        TestUtil.gotoInbox();
        TestUtil.isInbox();

        TestUtil.gotoTimeline();
        TestUtil.isTimeline();
    }

    @BeforeClass
    @AfterClass
    public static void cleanUp() {
        TestUtil.cleanUp("swapViews");
    }

}
