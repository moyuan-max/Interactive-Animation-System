package model;

import java.awt.*;
import java.awt.geom.*;

/**
 * 三角形类
 */
public class Triangle extends GameObject {
    private double[] xPoints = new double[3];
    private double[] yPoints = new double[3];

    public Triangle(double canvasWidth, double canvasHeight) {
        super();
        initializeRandom(canvasWidth, canvasHeight);
        updateVertices();
    }

    public Triangle(double centerX, double centerY, double canvasWidth, double canvasHeight) {
        super();
        initializeAtPosition(centerX, centerY, canvasWidth, canvasHeight);
        updateVertices();
    }

    private void updateVertices() {
        double centerX = x + size / 2;
        double centerY = y + size / 2;

        xPoints[0] = centerX;
        yPoints[0] = centerY - size / 2;
        xPoints[1] = centerX + size / 2;
        yPoints[1] = centerY + size / 2;
        xPoints[2] = centerX - size / 2;
        yPoints[2] = centerY + size / 2;
    }

    @Override
    protected double getWidth() {
        return size;
    }

    @Override
    protected double getHeight() {
        return size;
    }

    @Override
    public Shape getShape() {
        updateVertices();
        Path2D triangle = new Path2D.Double();
        triangle.moveTo(xPoints[0], yPoints[0]);
        triangle.lineTo(xPoints[1], yPoints[1]);
        triangle.lineTo(xPoints[2], yPoints[2]);
        triangle.closePath();
        return triangle;
    }
}