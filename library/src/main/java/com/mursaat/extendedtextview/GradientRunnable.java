package com.mursaat.extendedtextview;

import android.animation.ArgbEvaluator;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.Shader;
import android.os.SystemClock;
import android.widget.TextView;

import java.util.Arrays;

public class GradientRunnable implements Runnable {

    private final TextView textView;

    /**
     * The colors used in gradient
     */
    private int[] colors;

    /**
     * The angle of the gradient
     */
    private int angle;

    /**
     * The time separating the apparition of two colors in millisecond
     */
    private int speed;

    /**
     * The current progress of the gradient
     */
    private long totalDelta = 0;

    /**
     * The last draw time
     */
    private long lastTime = 0;

    /**
     * Ordered array of drawn colors
     */
    private int[] currentColors;

    /**
     * Gradient positions
     */
    private Point[] gradientsPositions;

    /**
     * Current gradient color index
     */
    private int currentGradient = 0;


    /**
     * The minimum time between each frames in millisecond
     */
    private final long deltaTimeExpectedMillis;

    GradientRunnable(TextView textView, int[] colors, int simultaneousColors, int angle, int speed, int maxFPS) {
        this.textView = textView;
        this.colors = colors;

        this.angle = angle;
        this.speed = speed;
        this.deltaTimeExpectedMillis = 1000 / maxFPS;

        final int wf = textView.getWidth();
        final int hf = textView.getHeight();
        gradientsPositions = getGradientsPoints(wf, hf);

        currentColors = Arrays.copyOf(colors, simultaneousColors);
    }

    @Override
    public void run() {
        long currentTime = SystemClock.uptimeMillis();
        long delta = currentTime - lastTime;
        if (delta > deltaTimeExpectedMillis) {
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
            lastTime = currentTime;
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

        // Top segment of rectangle
        Point topSegmentP1 = new Point(0, 0);
        Point topSegmentP2 = new Point(width, 0);

        intersectPoints[0] = MathsUtils.getIntersectionPoint(secantP1, secantP2, topSegmentP1, topSegmentP2);

        if (intersectPoints[0] == null) {
            // Left segment
            Point leftSegmentP1 = new Point(0, 0);
            Point leftSegmentP2 = new Point(0, height);

            intersectPoints[0] = MathsUtils.getIntersectionPoint(secantP1, secantP2, leftSegmentP1, leftSegmentP2);
        }

        // Bottom segment
        Point bottomSegmentP1 = new Point(0, height);
        Point bottomSegmentP2 = new Point(width, height);

        intersectPoints[1] = MathsUtils.getIntersectionPoint(secantP1, secantP2, bottomSegmentP1, bottomSegmentP2);

        if (intersectPoints[1] == null) {
            // Right segment
            Point rightSegmentP1 = new Point(width, 0);
            Point rightSegmentP2 = new Point(width, height);

            intersectPoints[1] = MathsUtils.getIntersectionPoint(secantP1, secantP2, rightSegmentP1, rightSegmentP2);
        }

        return intersectPoints;
    }

    public long getCurrentProgress() {
        return totalDelta;
    }

    public void setCurrentProgress(long progress) {
        this.totalDelta = progress;
    }
}
