package model;

import java.awt.*;
import java.awt.geom.*;

/**
 * 矩形类
 */
public class Rectangle extends GameObject {
    private final double width;
    private final double height;

    public Rectangle(double canvasWidth, double canvasHeight) {
        super();
        initializeRandom(canvasWidth, canvasHeight);
        this.width = size;
        this.height = size * (0.7 + rand.nextDouble() * 0.6);
    }

    public Rectangle(double centerX, double centerY, double canvasWidth, double canvasHeight) {
        super();
        initializeAtPosition(centerX, centerY, canvasWidth, canvasHeight);
        this.width = size;
        this.height = size * (0.7 + rand.nextDouble() * 0.6);
    }

    @Override
    protected double getWidth() {
        return width;
    }

    @Override
    protected double getHeight() {
        return height;
    }

    @Override
    public Shape getShape() {
        return new Rectangle2D.Double(x, y, width, height);
    }
}