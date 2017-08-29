package com.mursaat.extendedtextview;

import android.animation.ArgbEvaluator;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.Shader;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Arrays;

public class GradiantRunnable implements Runnable {

    private boolean mustStop = false;

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
     * The current progress of the gradient
     */
    private long totalDelta = 0;


    /**
     * The minimum time between each frames in millisecond
     */
    private final long deltaTimeExpectedMillis;

    public GradiantRunnable(TextView textView, int[] colors, int simultaneousColors, int angle, int speed, int maxFPS) {
        this.textView = textView;
        this.colors = colors;
        this.simultaneousColors = simultaneousColors;
        this.angle = angle;
        this.speed = speed;
        this.deltaTimeExpectedMillis = 1000 / maxFPS;
    }

    public void stop() {
        mustStop = true;
    }

    @Override
    public void run() {
        final int wf = textView.getWidth();
        final int hf = textView.getHeight();

        if (wf == 0 || hf == 0) {
            return;
        }

        Point[] gradientsPositions = getGradientsPoints(wf, hf);

        int[] currentColors = Arrays.copyOf(colors, simultaneousColors);
        int currentGradient = 0;

        long lastTime = 0;

        while (true) {
            if (mustStop) {
                return;
            }

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
