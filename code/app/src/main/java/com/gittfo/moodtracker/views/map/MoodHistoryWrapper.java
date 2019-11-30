package com.gittfo.moodtracker.views.map;

import android.os.Parcel;
import android.os.Parcelable;

import com.gittfo.moodtracker.mood.MoodEvent;

import java.util.ArrayList;

/**
 * This class exists only so MainActivity can pass a list of MoodEvents to the MapActivity in  a
 * nice way.
 */
public class MoodHistoryWrapper implements Parcelable {

    // TODO implement reading / writing

    private ArrayList<MoodEvent> moodEventList;

    /**
     * Create a MoodHistoryWrapper object from a list of MoodEvents
     *
     * @param moodEventList A list of MoodEvents that should be contained in the MoodHistory
     */
    public MoodHistoryWrapper(ArrayList<MoodEvent> moodEventList) {
        this.moodEventList = moodEventList;
    }

    /**
     * Create a MoodHistoryWrapper object from a parcel
     *
     * @param source A parcel containing MoodEvents
     */
    public MoodHistoryWrapper(Parcel source) {
        this.moodEventList = source.readArrayList(MoodEvent.class.getClassLoader());
    }

    /**
     * A method used to help create a MoodHistoryWrapper
     */
    public static final Creator<MoodHistoryWrapper> CREATOR = new Creator<MoodHistoryWrapper>() {
        @Override
        public MoodHistoryWrapper createFromParcel(Parcel source) {
            return new MoodHistoryWrapper(source);
        }

        @Override
        public MoodHistoryWrapper[] newArray(int size) {
            return new MoodHistoryWrapper[size];
        }
    };

    /**
     * Get the list that actually contains the MoodHistory's MoodEvents
     *
     * @return The list that actually contains the MoodHistory's MoodEvents
     */
    public ArrayList<MoodEvent> getMoodEventList() {
        return moodEventList;
    }

    /**
     * Get a hashed value of the contents of the MoodHistory
     *
     * @return A hashed value describing the contents of the MoodHistory
     */
    @Override
    public int describeContents() {
        return hashCode();
    }

    /**
     * Write the contents of the MoodHistory to a parcel
     *
     * @param dest  The parcel that will be written to
     * @param flags Any flags required during writing
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(moodEventList);
    }
}
