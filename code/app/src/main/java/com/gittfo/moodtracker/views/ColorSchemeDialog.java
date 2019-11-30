package com.gittfo.moodtracker.views;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

/**
 * This class implements a Color Scheme Dialog, allowing the user to switch between pre-defined UI
 * colour schemes.
 */
public class ColorSchemeDialog {

    private AlertDialog colorDialog;
    Activity c;
    private boolean[] buttonState;
    private View.OnClickListener fn;

    /**
     * Create a new ColorSchemeDialog object
     *
     * @param c The activity context that the dialog will be created in
     */
    public ColorSchemeDialog(Activity c) {
        this.c = c;
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        LayoutInflater inflater = c.getLayoutInflater();

        buttonState = new boolean[]{false, false, false, false, false, false};


        builder.setView(inflater.inflate(R.layout.dialog_color_scheme, null));
        Log.d("JUI", "Making builder");
        colorDialog = builder.create();
    }

    /**
     * Display the ColorSchemeDialog and set OnClickListeners for each of the mood buttons.
     */
    public void show() {
        colorDialog.show();

        colorDialog.findViewById(R.id.default_theme_button).setOnClickListener(v -> this.updateButtonUI(v));
        colorDialog.findViewById(R.id.neon_theme_button).setOnClickListener(v -> this.updateButtonUI(v));
        colorDialog.findViewById(R.id.pastel_theme_button).setOnClickListener(v -> this.updateButtonUI(v));
        colorDialog.findViewById(R.id.monochrome_theme_button).setOnClickListener(v -> this.updateButtonUI(v));
        colorDialog.findViewById(R.id.dark_theme_button_1).setOnClickListener(v -> this.updateButtonUI(v));
        colorDialog.findViewById(R.id.dark_theme_button_2).setOnClickListener(v -> this.updateButtonUI(v));

        colorDialog.findViewById(R.id.set_scheme_button).setOnClickListener(fn);
    }

    public void setOnClicker(View.OnClickListener fn) {
        this.fn = fn;
    }
    
    /**
     * Update the color dialog when a button is selected.
     *
     * @param v The view that caused the method to be called
     */
    private void updateButtonUI(View v) {
        View[] buttons = new View[]{
                colorDialog.findViewById(R.id.default_theme_button),
                colorDialog.findViewById(R.id.neon_theme_button),
                colorDialog.findViewById(R.id.pastel_theme_button),
                colorDialog.findViewById(R.id.monochrome_theme_button),
                colorDialog.findViewById(R.id.dark_theme_button_1),
                colorDialog.findViewById(R.id.dark_theme_button_2)
        };

        ColorStateList[] tints = new ColorStateList[]{
                ContextCompat.getColorStateList(c, R.color.colorDefaultThemeButton),
                ContextCompat.getColorStateList(c, R.color.colorNeonThemeButton),
                ContextCompat.getColorStateList(c, R.color.colorPastelThemeButton),
                ContextCompat.getColorStateList(c, R.color.colorMonochromeThemeButton),
                ContextCompat.getColorStateList(c, R.color.colorDarkTheme1Button),
                ContextCompat.getColorStateList(c, R.color.colorDarkTheme2Button),
                ContextCompat.getColorStateList(c, R.color.design_default_color_background)
        };

        for (int i = 0; i < 6; i++){
            buttons[i].setBackgroundTintList(tints[6]);
        }

        if (v == buttons[0]) {
            buttons[0].setBackgroundTintList(tints[0]);
            for(int i = 0; i < 6; i++) {buttonState[i] = false; }
            buttonState[0] = true;
        } else if (v == buttons[1]) {
            buttons[1].setBackgroundTintList(tints[1]);
            for(int i = 0; i < 6; i++) {buttonState[i] = false; }
            buttonState[1] = true;
        } else if (v == buttons[2]) {
            buttons[2].setBackgroundTintList(tints[3]);
            for(int i = 0; i < 6; i++) {buttonState[i] = false; }
            buttonState[2] = true;
        } else if (v == buttons[3]) {
            buttons[3].setBackgroundTintList(tints[2]);
            for(int i = 0; i < 6; i++) {buttonState[i] = false; }
            buttonState[3] = true;
        } else if (v == buttons[4]) {
            buttons[4].setBackgroundTintList(tints[4]);
            for (int i = 0; i < 6; i++) {buttonState[i] = false; }
            buttonState[4] = true;
        } else if (v == buttons[5]) {
            buttons[5].setBackgroundTintList(tints[5]);
            for (int i = 0; i < 6; i++) {buttonState[i] = false; }
            buttonState[5] = true;
        }

    }

    /**
     * Determine which colour scheme was selected
     *
     * @return the index of the chosen colour scheme button
     */
    public int getSelectedNum() {
        for (int i = 0; i < 6; i++) {
            if (buttonState[i]) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Close the ColorSchemeDialog.
     */
    public void cancel() {
        colorDialog.cancel();
    }
}
