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
public class AddMoodTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class, true, true) {

        @Override
        protected void beforeActivityLaunched() {
            InstrumentationRegistry.getInstrumentation().getTargetContext().getSharedPreferences(Database.PREFS, MODE_PRIVATE)
                    .edit()
                    .putString("user", "addmoodtest")
                    .apply();
        }
    };

    @Before
    public void setUp() {
        TestUtil.clickPlusButton();
    }

    @Test
    public void addMoodTest() {

        TestUtil.saveMoodEvent();

        TestUtil.pressOkOnDialog();

        TestUtil.selectHappy();

        TestUtil.selectSad();

        TestUtil.selectAfraid();

        TestUtil.selectSurprised();

        TestUtil.selectDisgusted();

        TestUtil.selectAngry();

        TestUtil.saveMoodEvent();

        // TODO check position in RecyclerView
        // https://developer.android.com/reference/android/support/test/espresso/contrib/RecyclerViewActions
        onView(allOf(withId(R.id.user_mood_textView), withText("ANGRY"), isDisplayed()));
    }

    @Test
    public void addMoodWithCommentTest() {

        TestUtil.selectHappy();

        TestUtil.enterComment("Test Comment");

        TestUtil.saveMoodEvent();

        // TODO check position in RecyclerView
        // https://developer.android.com/reference/android/support/test/espresso/contrib/RecyclerViewActions
        onView(allOf(withId(R.id.user_mood_textView), withText("HAPPY"), isDisplayed()));
        onView(allOf(withId(R.id.user_reason_textView), withText("Test Comment"), isDisplayed()));
    }

    @Test
    public void addMoodWithZeroPeople() {

        TestUtil.selectSad();

        // click the other buttons
        onView(allOf(withId(R.id.social_button_zero), withText("0"))).perform(scrollTo(), click());
        onView(allOf(withId(R.id.social_button_one), withText("1"))).perform(scrollTo(), click());
        onView(allOf(withId(R.id.social_button_two_plus), withText("2+"))).perform(scrollTo(), click());
        onView(allOf(withId(R.id.social_button_crowd), withText("A CROWD"))).perform(scrollTo(), click());
        onView(allOf(withId(R.id.social_button_na), withText("N/A"))).perform(scrollTo(), click());

        onView(allOf(withId(R.id.social_button_zero), withText("0"))).perform(scrollTo(), click());

        TestUtil.saveMoodEvent();

        // TODO check position in RecyclerView
        // https://developer.android.com/reference/android/support/test/espresso/contrib/RecyclerViewActions
        onView(allOf(withId(R.id.num_people_textView), withText("zero"), isDisplayed()));
        onView(allOf(withId(R.id.user_mood_textView), withText("SAD"), isDisplayed()));
    }

    // TODO
//    @Test
//    public void addMoodWithPhotoTest(){
//        selectAfraid();
//        // https://developer.android.com/training/testing/espresso/intents
//
//    }

    @Test
    public void addMoodWithLocation() {

        TestUtil.selectSurprised();

        onView(allOf(
                withId(R.id.location_button),
                withText("Attach Current Location (optional)"))
        ).perform(scrollTo(), click());

        TestUtil.saveMoodEvent();

        // TODO check position in RecyclerView
        // https://developer.android.com/reference/android/support/test/espresso/contrib/RecyclerViewActions
        onView(allOf(withId(R.id.user_mood_textView), withText("SURPRISED"), isDisplayed()));
        onView(allOf(withId(R.id.location_textView), withText(not("NaN, NaN")), isDisplayed()));
    }

    @After
    public void deleteMood() {
        TestUtil.deleteMood();
    }

    @BeforeClass
    @AfterClass
    public static void cleanUp() {
        TestUtil.cleanUp("addmoodtest");
    }

}
