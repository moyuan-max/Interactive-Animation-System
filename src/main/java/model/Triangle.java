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
    public boolean containsPoint(double px, double py) {
        updateVertices(); // 确保顶点是最新的

        // 使用重心坐标法判断点是否在三角形内
        double areaABC = triangleArea(xPoints[0], yPoints[0],
                xPoints[1], yPoints[1],
                xPoints[2], yPoints[2]);

        double areaPBC = triangleArea(px, py,
                xPoints[1], yPoints[1],
                xPoints[2], yPoints[2]);
        double areaPCA = triangleArea(px, py,
                xPoints[2], yPoints[2],
                xPoints[0], yPoints[0]);
        double areaPAB = triangleArea(px, py,
                xPoints[0], yPoints[0],
                xPoints[1], yPoints[1]);

        // 允许一些容差
        return Math.abs(areaABC - (areaPBC + areaPCA + areaPAB)) < 1.0;
    }

    /**
     * 计算三角形面积
     */
    private double triangleArea(double x1, double y1,
                                double x2, double y2,
                                double x3, double y3) {
        return Math.abs((x1*(y2-y3) + x2*(y3-y1) + x3*(y1-y2)) / 2.0);
    }
}