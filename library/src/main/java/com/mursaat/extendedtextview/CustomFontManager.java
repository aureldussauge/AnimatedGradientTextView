package com.mursaat.extendedtextview;

import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Aurelien on 08/10/2016.
 */

public final class CustomFontManager {

    private static final String FONT_FILE_NAME = "fonts/";

    private CustomFontManager() {

    }

    public static void applyFontFromAttrs(TextView textView, AttributeSet attrs) {
        // Initialize an array containing id of attributes we want to have
        final int[] set = {
                R.attr.font
        };

        final TypedArray typedArray = textView.getContext().obtainStyledAttributes(attrs, set);
        String fontName = typedArray.getString(0);

        typedArray.recycle();

        if(fontName != null) {
            Typeface font = Typeface.createFromAsset(textView.getContext().getAssets(), FONT_FILE_NAME + fontName);
            textView.setTypeface(font);
        }
    }
}
