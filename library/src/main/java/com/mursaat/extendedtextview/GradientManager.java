package com.mursaat.extendedtextview;

import android.animation.ArgbEvaluator;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used by TextView
 */
public class GradientManager {

    private final TextView textView;

    public GradientManager(TextView textView) {
        this.textView = textView;
        this.initDefaultValues();
    }

    public GradientManager(TextView textView, AttributeSet attrs) {
        this.textView = textView;
        this.initFromAttrsValues(attrs);
    }

    /**
     * How many gradients calculated by second
     */
    private static final int MAX_FPS = 60;

    /**
     * The minimum time between each frames in millisecond
     */
    private static final long DELTA_TIME_EXPECTED_MILLIS = 1000 / MAX_FPS;

    /**
     * Must count each threads created
     */
    private AtomicInteger countRunningThread = new AtomicInteger(0);

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

    private static final int ATTR_NOT_FOUND = Integer.MIN_VALUE;

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
                R.attr.speed
        };

        final TypedArray typedArray = textView.getContext().obtainStyledAttributes(attrs, set);

        // Get colors array id
        int colorsArrayId = typedArray.getResourceId(0, ATTR_NOT_FOUND);

        // Get colors
        if (colorsArrayId != ATTR_NOT_FOUND) {
            colors = textView.getResources().getIntArray(colorsArrayId);
        }
        else {
            colors = textView.getResources().getIntArray(R.array.default_gradient_colors);
        }

        // Get others attributes
        simultaneousColors = typedArray.getInt(1, ATTR_NOT_FOUND);
        angle = typedArray.getInt(2, ATTR_NOT_FOUND);
        speed = typedArray.getInt(3, ATTR_NOT_FOUND);

        if(simultaneousColors == ATTR_NOT_FOUND){
            simultaneousColors = 2;
        }
        if(angle == ATTR_NOT_FOUND){
            angle = 45;
        }
        if(speed == ATTR_NOT_FOUND){
            speed = 1000;
        }

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
    }

    /**
     * Create a thread which applies the gradient
     */
    public void applyNewGradiantThread() {
        final int wf = textView.getWidth();
        final int hf = textView.getHeight();

        if (wf > 0 && hf > 0) {
            new Thread() {
                @Override
                public void run() {
                    // If we already have a running thread, we can leave
                    if(!countRunningThread.compareAndSet(0, 1)){
                        return;
                    }

                    long lastTime = System.currentTimeMillis();
                    int totalDelta = 0;
                    int angle = 45;

                    Point[] gradientsPositions = getGradientsPoints(wf, hf);

                    int[] currentColors = Arrays.copyOf(colors, simultaneousColors);
                    int currentGradient = 0;
                    while (textView.isShown()) {
                        long delta = System.currentTimeMillis() - lastTime;
                        if (delta > DELTA_TIME_EXPECTED_MILLIS) {

                            totalDelta += delta;
                            float totalPercentage = totalDelta / ((float) speed);
                            totalPercentage = totalPercentage > 1 ? 1 : totalPercentage;

                            for (int colorIndex = 0; colorIndex < currentColors.length; colorIndex++) {
                                currentColors[colorIndex] = (int) (new ArgbEvaluator().evaluate(totalPercentage, colors[(currentGradient + colorIndex) % colors.length], colors[(currentGradient + (colorIndex + 1)) % colors.length]));
                            }

                            if (totalPercentage == 1) {
                                totalDelta = 0;
                                currentGradient = (currentGradient + 1) % colors.length;
                            }

                            Shader shader = new LinearGradient(gradientsPositions[0].x, gradientsPositions[0].y, gradientsPositions[1].x, gradientsPositions[1].y, currentColors, null, Shader.TileMode.CLAMP);
                            textView.getPaint().setShader(shader);

                            textView.postInvalidate();
                            lastTime = System.currentTimeMillis();
                        }
                    }
                    countRunningThread.decrementAndGet();
                }
            }.start();
        }
    }

    /**
     * Get the points used to create the Linear Gradient from the angle
     *
     * @param width  the textview width
     * @param height the textview height
     * @return An array containing the two points
     */
    private Point[] getGradientsPoints(int width, int height) {
        // Angle from degree to radian
        double angleRadian = Math.toRadians(angle);

        // We want a circle radius > Max dist ( circle center, rectangle point )
        int circleRadius = width;

        // Get the circle center
        Point circleCenter = new Point(width / 2, height / 2);

        // Create a segment passing through the center of the circle
        Point secantP1 = new Point((int) (circleCenter.x - circleRadius * Math.cos(angleRadian)), (int) (circleCenter.y - circleRadius * Math.sin(angleRadian)));
        Point secantP2 = new Point((int) (circleCenter.x + circleRadius * Math.cos(angleRadian)), (int) (circleCenter.y + circleRadius * Math.sin(angleRadian)));

        Point[] intersectPoints = new Point[2];

        // On défini le segment haut du rectangle
        Point topSegmentP1 = new Point(0, 0);
        Point topSegmentP2 = new Point(width, 0);

        intersectPoints[0] = MathsUtils.getIntersectionPoint(secantP1, secantP2, topSegmentP1, topSegmentP2);

        if (intersectPoints[0] == null) {
            // On défini le segment gauche
            Point leftSegmentP1 = new Point(0, 0);
            Point leftSegmentP2 = new Point(0, height);

            intersectPoints[0] = MathsUtils.getIntersectionPoint(secantP1, secantP2, leftSegmentP1, leftSegmentP2);
        }

        // On défini le segment bas du rectangle
        Point bottomSegmentP1 = new Point(0, height);
        Point bottomSegmentP2 = new Point(width, height);

        intersectPoints[1] = MathsUtils.getIntersectionPoint(secantP1, secantP2, bottomSegmentP1, bottomSegmentP2);

        if (intersectPoints[1] == null) {
            // On défini le segment gauche
            Point rightSegmentP1 = new Point(width, 0);
            Point rightSegmentP2 = new Point(width, height);

            intersectPoints[1] = MathsUtils.getIntersectionPoint(secantP1, secantP2, rightSegmentP1, rightSegmentP2);
        }

        return intersectPoints;
    }
}
