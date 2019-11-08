package com.gittfo.moodtracker;

import com.gittfo.moodtracker.mood.Mood;

import org.junit.Test;

import static com.gittfo.moodtracker.mood.Mood.emotionalStateFromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the Mood class.
 *
 * The class is quite simplistic, but these tests have been added for the sake
 * of completeness.
 *
 * The tests mainly ensure that the proper pre-defined mood data is returned when starting from a
 * string representation.
 */
public class MoodTest {

    // Test the "HAPPY" mood
    @Test
    public void testHappy() {
        String moodString = "HAPPY";

        // Given a string, obtain the correct emotional state
        Mood.EmotionalState emotionalState = emotionalStateFromString(moodString);
        assertEquals(emotionalState, Mood.EmotionalState.HAPPY);

        // Given an emotional state, obtain the correct default mood
        Mood mood = Mood.moodFromEmotionalState(emotionalState);
        assertEquals(mood, Mood.DEFAULT_HAPPY);
    }

    // Test the "SAD" mood
    @Test
    public void testSad() {
        String moodString = "SAD";

        // Given a string, obtain the correct emotional state
        Mood.EmotionalState emotionalState = emotionalStateFromString(moodString);
        assertEquals(emotionalState, Mood.EmotionalState.SAD);

        // Given an emotional state, obtain the correct default mood
        Mood mood = Mood.moodFromEmotionalState(emotionalState);
        assertEquals(mood, Mood.DEFAULT_SAD);
    }

    // Test the "SURPRISED" mood
    @Test
    public void testSurprised() {
        String moodString = "SURPRISED";

        // Given a string, obtain the correct emotional state
        Mood.EmotionalState emotionalState = emotionalStateFromString(moodString);
        assertEquals(emotionalState, Mood.EmotionalState.SURPRISED);

        // Given an emotional state, obtain the correct default mood
        Mood mood = Mood.moodFromEmotionalState(emotionalState);
        assertEquals(mood, Mood.DEFAULT_SURPRISED);
    }

    // Test the "AFRAID" mood
    @Test
    public void testAfraid() {
        String moodString = "AFRAID";

        // Given a string, obtain the correct emotional state
        Mood.EmotionalState emotionalState = emotionalStateFromString(moodString);
        assertEquals(emotionalState, Mood.EmotionalState.AFRAID);

        // Given an emotional state, obtain the correct default mood
        Mood mood = Mood.moodFromEmotionalState(emotionalState);
        assertEquals(mood, Mood.DEFAULT_AFRAID);
    }

    // Test the "DISGUSTED" mood
    @Test
    public void testDisgusted() {
        String moodString = "DISGUSTED";

        // Given a string, obtain the correct emotional state
        Mood.EmotionalState emotionalState = emotionalStateFromString(moodString);
        assertEquals(emotionalState, Mood.EmotionalState.DISGUSTED);

        // Given an emotional state, obtain the correct default mood
        Mood mood = Mood.moodFromEmotionalState(emotionalState);
        assertEquals(mood, Mood.DEFAULT_DISGUSTED);
    }

    // Test the "ANGRY" mood
    @Test
    public void testAngry() {
        String moodString = "ANGRY";

        // Given a string, obtain the correct emotional state
        Mood.EmotionalState emotionalState = emotionalStateFromString(moodString);
        assertEquals(emotionalState, Mood.EmotionalState.ANGRY);

        // Given an emotional state, obtain the correct default mood
        Mood mood = Mood.moodFromEmotionalState(emotionalState);
        assertEquals(mood, Mood.DEFAULT_ANGRY);
    }
}
