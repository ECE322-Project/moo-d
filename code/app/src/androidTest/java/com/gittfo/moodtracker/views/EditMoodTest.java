package com.gittfo.moodtracker.views;

import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.gittfo.moodtracker.database.Database;

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
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EditMoodTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class, true, true) {

        @Override
        protected void beforeActivityLaunched() {
            InstrumentationRegistry.getInstrumentation().getTargetContext().getSharedPreferences(Database.PREFS, MODE_PRIVATE)
                    .edit()
                    .putString("user", "editmoodtest")
                    .apply();
        }
    };

    @Before
    public void setUp() {

    }

    @Test
    public void createHappyEditSad(){
        TestUtil.createHappy();
        TestUtil.editMood(0, "SAD");
    }

    @Test
    public void createSadEditAfraid(){
        TestUtil.createSad();
        TestUtil.editMood(0, "AFRAID");
    }

    @Test
    public void createAfraidEditSurprised(){
        TestUtil.createAfraid();
        TestUtil.editMood(0, "SURPRISED");
    }

    @Test
    public void createSurprisedEditDisgusted(){
        TestUtil.createSurprised();
        TestUtil.editMood(0, "DISGUSTED");
    }

    @Test
    public void createDisgustedEditAngry(){
        TestUtil.createDisgusted();
        TestUtil.editMood(0, "ANGRY");
    }

    @Test
    public void createAngryEditHappy(){
        TestUtil.createAngry();
        TestUtil.editMood(0, "HAPPY");
    }

    @Test
    public void createHappyEditComment(){
        TestUtil.createHappy();
        TestUtil.clickEdit(0);
        TestUtil.enterComment("new comment");
        TestUtil.checkCommentAtPosition("new comment", 0);
    }

    @Test
    public void createSadEditPeople(){
        TestUtil.createSad();
        TestUtil.clickEdit(0);
        onView(allOf(withId(R.id.social_button_zero), withText("0"))).perform(scrollTo(), click());
        TestUtil.saveMoodEvent();
        TestUtil.checkPeopleAtPosition("0", 0);
    }

//     @Test
//     public void createAfraidEditLocation(){
//        // not doing to test, because the location will be the same
//     }

//    @Test
//    public void createSurprisedEditPhoto(){
//        // TODO
//    }


    @BeforeClass
    @AfterClass
    public static void cleanUp() {
        TestUtil.cleanUp("editmoodtest");
    }

}
