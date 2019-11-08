package com.gittfo.moodtracker.mood;


import android.graphics.Color;

import androidx.annotation.DrawableRes;

import com.gittfo.moodtracker.views.R;

/**
 * Represents the notion of a 'Mood' for our mood tracker app. Stores both information related to
 * the state of a mood (i.e. emotion) and the representation of a mood (i.e. color, emoticon).
 */
public class Mood {

    /**
     * The list of possible emotional states a user can choose for their moods.
     * Based on the "six universal moods" as classified by Paul Ekman.
     */
    public enum EmotionalState {
        HAPPY,
        SAD,
        SURPRISED,
        AFRAID,
        DISGUSTED,
        ANGRY,
    };

    // Filename of emoticon representing this mood
    private @DrawableRes int emoticon;

    // Enum holding the "emotional state" this mood represents
    private EmotionalState emotionalState;

    // A String containing the name of the mood
    private String name;

    // A color for the mood, stored as an int
    private int color;

    /**
     * Creates a new mood
     *
     * @param emoticon       Filename of the icon representing this mood.
     * @param emotionalState Enum describing the emotional state of this mood
     * @param name           A String containing the name of the mood
     * @param color          A color for the mood
     */
    public Mood(@DrawableRes int emoticon, EmotionalState emotionalState, String name, int color) {
        this.emoticon = emoticon;
        this.emotionalState = emotionalState;
        this.name = name;
        this.color = color;
    }

    /**
     * Given a string representation, return a valid EmotionalState
     *
     * @param emotionalState A string representation of a valid EmotionalState
     * @return               A valid EmotionalState
     */
    public static EmotionalState emotionalStateFromString(String emotionalState) {
        switch (emotionalState) {
            case "HAPPY": return EmotionalState.HAPPY;
            case "SAD": return EmotionalState.SAD;
            case "ANGRY": return EmotionalState.ANGRY;
            case "AFRAID": return EmotionalState.AFRAID;
            case "DISGUSTED": return EmotionalState.DISGUSTED;
            case "SURPRISED": return EmotionalState.SURPRISED;
        }
        return null;
    }

    /**
     * Return a string representation of a valid EmotionalState
     *
     * @param emotionalState A valid EmotionalState
     * @return               A string representation of a valid EmotionalState
     */
    public static Mood moodFromEmotionalState(EmotionalState emotionalState) {
        switch (emotionalState) {
            case HAPPY: return DEFAULT_HAPPY;
            case SAD: return DEFAULT_SAD;
            case ANGRY: return DEFAULT_ANGRY;
            case AFRAID: return DEFAULT_AFRAID;
            case DISGUSTED: return DEFAULT_DISGUSTED;
            case SURPRISED: return DEFAULT_SURPRISED;
        }
        return null;
    }

    /**
     * Return the filename for the icon representing this mood.
     *
     * @return The filename for the icon representing this mood.
     */
    public @DrawableRes int getEmoticon() {
        return emoticon;
    }

    /**
     * Return the valid emotional state that this mood represents.
     *
     * @return An enum describing the emotional state of this mood.
     */
    public EmotionalState getEmotionalState() {
        return emotionalState;
    }

    /**
     * Return a textual description of this mood.
     *
     * @return A string containing the name of the mood
     */
    public String getName() {
        return name;
    }

    /**
     * Color is the android in for the color codes
     *
     * @return the color
     */
    public int getColor() {
        return this.color;
    }

    // Pre-defined mood objects
    public static Mood DEFAULT_HAPPY = new Mood(R.drawable.ic_happy_icon_24dp, Mood.EmotionalState.HAPPY,
            "Happy", Color.parseColor("#81c784"));
    public static Mood DEFAULT_SAD = new Mood(R.drawable.ic_sad_icon_24dp, EmotionalState.SAD,
            "Sad", Color.parseColor("#64b5f6"));
    public static Mood DEFAULT_SURPRISED = new Mood(R.drawable.ic_surprised_icon_24dp, EmotionalState.SURPRISED,
            "Surprised", Color.parseColor("#fff176"));
    public static Mood DEFAULT_AFRAID = new Mood(R.drawable.ic_afraid_icon_24dp, EmotionalState.AFRAID,
            "Afraid", Color.parseColor("#ffb74d"));
    public static Mood DEFAULT_DISGUSTED = new Mood(R.drawable.ic_disgusted_icon_24dp, EmotionalState.DISGUSTED,
            "Disgusted", Color.parseColor("#b39ddb"));
    public static Mood DEFAULT_ANGRY = new Mood(R.drawable.ic_angry_icon_24dp, EmotionalState.ANGRY,
            "Angry", Color.parseColor("#ff8a65"));
}
