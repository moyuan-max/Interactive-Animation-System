package model;

import java.awt.*;
import java.awt.geom.*;

/**
 * 圆形（球）类
 */
public class Ball extends GameObject {

    public Ball(double width, double height) {
        super();
        initializeRandom(width, height);
    }

    public Ball(double centerX, double centerY, double canvasWidth, double canvasHeight) {
        super();
        initializeAtPosition(centerX, centerY, canvasWidth, canvasHeight);
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
        return new Ellipse2D.Double(x, y, size, size);
    }
}