package com.gittfo.moodtracker.views;

import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.gittfo.moodtracker.database.Database;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.content.Context.MODE_PRIVATE;

// hack to run the setup and teardown methods which interact with the UI
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
@RunWith(AndroidJUnit4.class)
public class FilterMoodsTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class, true, true) {

        @Override
        protected void beforeActivityLaunched() {
            InstrumentationRegistry.getInstrumentation().getTargetContext().getSharedPreferences(Database.PREFS, MODE_PRIVATE)
                    .edit()
                    .putString("user", "filtermoodstest")
                    .apply();
            super.beforeActivityLaunched();
        }
    };

    @Test
    public void AAA_setUp() {
        TestUtil.deleteExistingMoods();

        // create 21 moods

        TestUtil.createHappy();

        TestUtil.createSad();
        TestUtil.createSad();

        for (int i = 0; i < 3; i += 1)
            TestUtil.createAfraid();

        for (int i = 0; i < 4; i += 1)
            TestUtil.createSurprised();

        for (int i = 0; i < 5; i += 1)
            TestUtil.createDisgusted();

        for (int i = 0; i < 6; i += 1)
            TestUtil.createAngry();

    }

    @Test
    public void all() {
        checkAllMoods();
    }

    @Test
    public void selectAllTest() {
        TestUtil.clickFilter();
        TestUtil.selectHappy();
        TestUtil.selectSad();
        TestUtil.selectAfraid();
        TestUtil.selectSurprised();
        TestUtil.selectDisgusted();
        TestUtil.selectAngry();

        TestUtil.applyFilter();

        checkAllMoods();
    }

    @Test
    public void filterHappyTest() {
        TestUtil.clickFilter();
        TestUtil.filterByHappy();

        TestUtil.checkHappy(0);
    }

    @Test
    public void filterSadTest() {
        TestUtil.clickFilter();
        TestUtil.filterBySad();

        TestUtil.checkSad(0);
        TestUtil.checkSad(1);
    }

    @Test
    public void filterAfraidTest() {
        TestUtil.clickFilter();
        TestUtil.filterByAfraid();

        for (int i = 0; i < 3; i += 1)
            TestUtil.checkAfraid(i);
    }

    @Test
    public void filterSurprisedTest() {
        TestUtil.clickFilter();
        TestUtil.filterBySurprised();

        for (int i = 0; i < 4; i += 1)
            TestUtil.checkSurprised(i);

    }

    @Test
    public void filterDisgustedTest() {
        TestUtil.clickFilter();
        TestUtil.filterByDisgusted();

        for (int i = 0; i < 5; i += 1)
            TestUtil.checkDisgusted(i);
    }

    @Test
    public void filterAngryTest() {
        TestUtil.clickFilter();
        TestUtil.filterByAngry();

        for (int i = 0; i < 6; i += 1)
            TestUtil.checkAngry(i);
    }

    @Test
    public void combinationFilterTest() {
        TestUtil.clickFilter();

        // testing 3 of the moods together
        // randomly chosen by asking friends in other disciplines 😂
        TestUtil.selectHappy();
        TestUtil.selectSad();
        TestUtil.selectDisgusted();

        TestUtil.applyFilter();

        for (int i = 0; i < 5; i += 1)
            TestUtil.checkDisgusted(i);

        TestUtil.checkSad(5);
        TestUtil.checkSad(6);

        TestUtil.checkHappy(7);
    }

    public void checkAllMoods() {
        for (int i = 0; i < 6; i += 1)
            TestUtil.checkAngry(i);

        for (int i = 6; i < 11; i += 1)
            TestUtil.checkDisgusted(i);

        for (int i = 11; i < 15; i += 1)
            TestUtil.checkSurprised(i);

        for (int i = 15; i < 18; i += 1)
            TestUtil.checkAfraid(i);

        for (int i = 18; i < 20; i += 1)
            TestUtil.checkSad(i);

        TestUtil.checkHappy(21);

    }

    @BeforeClass
    @AfterClass
    public static void cleanUp() {
        TestUtil.cleanUp("filtermoodstest");
    }


}
