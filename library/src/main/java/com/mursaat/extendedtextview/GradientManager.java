package com.mursaat.extendedtextview;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Used by TextView
 */
public class GradientManager {

    private final TextView textView;

    /**
     * The colors used in gradient
     */
    private int[] colors;

    /**
     * The number of colors possibly displayed in a same time
     */
    private int simultaneousColors;

    /**
     * The angle of the gradient
     */
    private int angle;

    /**
     * The time separating the apparition of two colors in millisecond
     */
    private int speed;

    /**
     * How many gradients are calculated by second
     */
    private int maxFPS;

    /**
     * Time interval between each draw (millis)
     */
    private int drawTimeInterval;

    /**
     * Current running gradient runnable
     */
    private GradientRunnable runnable;

    /**
     * Current scheduled gradient future running
     */
    private ScheduledFuture<?> scheduledFuture = null;

    /**
     * The draw-gradient uptime
     */
    private long currentGradientProgress = 0;

    private static final int ATTR_NOT_FOUND = Integer.MIN_VALUE;

    public GradientManager(TextView textView) {
        this.textView = textView;
        this.initDefaultValues();
    }

    public GradientManager(TextView textView, AttributeSet attrs) {
        this.textView = textView;
        this.initFromAttrsValues(attrs);
    }

    /**
     * Initialize the variables of this object
     *
     * @param attrs The attributes of the TextView
     */
    @SuppressWarnings("ResourceType")
    private void initFromAttrsValues(AttributeSet attrs) {
        // Initialize an array containing id of attributes we want to have
        final int[] set = {
                R.attr.colors,
                R.attr.simultaneousColors,
                R.attr.angle,
                R.attr.speed,
                R.attr.maxFPS
        };

        final TypedArray typedArray = textView.getContext().obtainStyledAttributes(attrs, set);

        // Get colors array id
        int colorsArrayId = typedArray.getResourceId(0, ATTR_NOT_FOUND);

        // Get colors
        if (colorsArrayId != ATTR_NOT_FOUND) {
            colors = textView.getResources().getIntArray(colorsArrayId);
        } else {
            colors = textView.getResources().getIntArray(R.array.default_gradient_colors);
        }

        // Get others attributes
        simultaneousColors = typedArray.getInt(1, ATTR_NOT_FOUND);
        angle = typedArray.getInt(2, ATTR_NOT_FOUND);
        speed = typedArray.getInt(3, ATTR_NOT_FOUND);
        maxFPS = typedArray.getInt(4, ATTR_NOT_FOUND);

        if (simultaneousColors == ATTR_NOT_FOUND) {
            simultaneousColors = 2;
        }
        if (angle == ATTR_NOT_FOUND) {
            angle = 45;
        }
        if (speed == ATTR_NOT_FOUND) {
            speed = 1000;
        }

        if (maxFPS == ATTR_NOT_FOUND) {
            maxFPS = 24;
        }

        drawTimeInterval = 1000 / maxFPS;

        typedArray.recycle();
    }

    /**
     * Initialize the variables of this object with default values
     */
    private void initDefaultValues() {
        colors = new int[]{Color.BLUE, Color.RED, Color.GREEN};
        simultaneousColors = 2;
        angle = 45;
        speed = 2000;
        maxFPS = 24;
        drawTimeInterval = 1000 / maxFPS;
    }

    public void stopGradient() {
        synchronized (this) {
            if (scheduledFuture != null) {
                // Save gradient state (future possible restart)
                currentGradientProgress = runnable.getCurrentProgress();

                scheduledFuture.cancel(true);
                runnable = null;
                scheduledFuture = null;
            }
        }
    }

    /**
     * Create a thread which applies the gradient if not exist
     */
    public void startGradient() {
        synchronized (this) {
            if (scheduledFuture != null) {
                return;
            }

            final int wf = textView.getWidth();
            final int hf = textView.getHeight();

            if (wf > 0 && hf > 0) {
                runnable = new GradientRunnable(textView, colors, simultaneousColors, angle, speed);

                // Apply saved progress if there is
                runnable.setCurrentProgress(currentGradientProgress);

                ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
                scheduledFuture = scheduledExecutor.scheduleAtFixedRate(runnable, 0, drawTimeInterval, TimeUnit.MILLISECONDS);
            }
        }
    }

}
