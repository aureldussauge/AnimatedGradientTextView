package com.mursaat.extendedtextview;

import android.graphics.Point;

/**
 * Maths functions used in this library
 */
final class MathsUtils {
    private MathsUtils() {

    }

    /**
     * Computes the intersection between two segments.
     *
     * @param p1 The start point of the first segment
     * @param p2 The end point of the first segment
     * @param p3 The start point of the second segment
     * @param p4 The end point of the second segment
     * @return Point where the segments intersect, or null if they don't
     */
    static Point getIntersectionPoint(Point p1, Point p2, Point p3, Point p4) {
        int d = (p1.x - p2.x) * (p3.y - p4.y) - (p1.y - p2.y) * (p3.x - p4.x);
        if (d == 0) return null;

        int x = ((p3.x - p4.x) * (p1.x * p2.y - p1.y * p2.x) - (p1.x - p2.x) * (p3.x * p4.y - p3.y * p4.x)) / d;
        int y = ((p3.y - p4.y) * (p1.x * p2.y - p1.y * p2.x) - (p1.y - p2.y) * (p3.x * p4.y - p3.y * p4.x)) / d;

        return new Point(x, y);
    }
}
