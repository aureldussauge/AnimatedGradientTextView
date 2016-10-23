package com.mursaat.extendedtextview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * A TextView using an animated gradient as text color
 */
public class AnimatedGradientTextView extends TextView {

    GradientManager gradientManager;

    public AnimatedGradientTextView(Context context) {
        super(context);
        gradientManager = new GradientManager(this);
    }

    public AnimatedGradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gradientManager = new GradientManager(this, attrs);
        CustomFontManager.applyFontFromAttrs(this, attrs);
    }

    public AnimatedGradientTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        gradientManager = new GradientManager(this, attrs);
        CustomFontManager.applyFontFromAttrs(this, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        gradientManager.applyNewGradiantThread();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if(visibility == VISIBLE && getScaleX() != 0 && getScaleY() != 0){
            gradientManager.applyNewGradiantThread();
        }
    }
}