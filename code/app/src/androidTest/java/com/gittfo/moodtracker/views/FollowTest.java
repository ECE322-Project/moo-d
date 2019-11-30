package com.gittfo.moodtracker.views;

import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.gittfo.moodtracker.database.Database;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import static android.content.Context.MODE_PRIVATE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class FollowTest {

    final static String TESTY = "105648403813593449833";

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class, true, true) {

        @Override
        protected void beforeActivityLaunched() {
            changeUser("followmoodtest");
        }
    };

    public void changeUser(String user){
        InstrumentationRegistry.getInstrumentation().getTargetContext().getSharedPreferences(Database.PREFS, MODE_PRIVATE)
                .edit()
                .putString("user", user)
                .apply();
    }

    public void followMe() {
        try {
            // make testy follow us
            new URL("https://us-central1-moo-d-95679.cloudfunctions.net/followUser?uid=" + TESTY + "&oid=followmoodtest")
                    .openConnection().getContent();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void unfollowMe() {
        try {
            // make testy unfollow us
            new URL("https://us-central1-moo-d-95679.cloudfunctions.net/unfollowUser?uid=" + TESTY + "&oid=followmoodtest")
                    .openConnection().getContent();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }


    @Test
    public void followUser() throws InterruptedException {

        TestUtil.createHappy();

        changeUser(TESTY);
        Database.get(InstrumentationRegistry.getInstrumentation().getTargetContext()).getFollowees((followees) -> {
            changeUser("followmoodtest");
            followees.forEach(f -> Database.get(InstrumentationRegistry.getInstrumentation().getTargetContext()).unFollowUser(f, null));
        });
        changeUser("followmoodtest");

        onView(withId(R.id.timeline_menu_item)).perform(click());
        onView(withId(R.id.follow_button)).perform(click());
        onView(withId(R.id.user_follow_editText)).perform(replaceText("Testy"), closeSoftKeyboard());
        onView(withId(R.id.send_request_button)).perform(click());

        Thread.sleep(10000); // network request

        changeUser(TESTY);
        Database.get(InstrumentationRegistry.getInstrumentation().getTargetContext()).completeFollowRequest("followmoodtest", true);

        changeUser("followmoodtest");
        Thread.sleep(10000);

        Database.get(InstrumentationRegistry.getInstrumentation().getTargetContext()).getFollowees((followees) -> {
            assertEquals(followees.size(),  1);
            assertEquals(followees.get(0), TESTY);
        });

        changeUser("followmoodtest");
    }

    @Test
    public void acceptFollowRequest() throws InterruptedException {
        followMe();
        onView(withId(R.id.inbox_menu_item)).perform(click());
        // wait for the api call
        Thread.sleep(1000);
        onView(withId(R.id.follow_request_username)).perform(click());
        onView(withId(R.id.allow_button)).perform(click());

        changeUser(TESTY);
        Thread.sleep(10000);
        Database.get(InstrumentationRegistry.getInstrumentation().getTargetContext()).getFollowees((followees) -> {
            assertEquals(followees.size(),  1);
            assertEquals(followees.get(0), "followmoodtest");
        });

        unfollowMe();
        onView(withId(R.id.timeline_menu_item)).perform(click());
    }

    @Test
    public void denyFollowRequest() throws InterruptedException {
        followMe();
        onView(withId(R.id.inbox_menu_item)).perform(click());
        // wait for the api call
        Thread.sleep(1000);
        onView(withId(R.id.follow_request_username)).perform(click());
        onView(withId(R.id.deny_button)).perform(click());

        changeUser(TESTY);
        Database.get(InstrumentationRegistry.getInstrumentation().getTargetContext()).getFollowees((followees) -> {
            assertEquals(followees.size(),  0);
        });
    }

}

