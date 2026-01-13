package model;

import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

/**
 * 游戏对象的抽象基类
 */
public abstract class GameObject {
    protected double x, y;           // 位置
    protected double dx, dy;         // 速度
    protected Color color;           // 颜色
    protected double size;           // 基础大小
    protected Random rand;
    
    public GameObject() {
        this.rand = new Random();
    }
    
    /**
     * 在指定位置初始化
     */
    public void initializeAtPosition(double centerX, double centerY, 
                                    double canvasWidth, double canvasHeight) {
        // 随机大小：10到30之间
        this.size = 10 + rand.nextDouble() * 20;
        
        // 设置到指定位置（使中心点在点击位置）
        this.x = centerX - size / 2;
        this.y = centerY - size / 2;
        
        // 确保在画布范围内
        if (this.x < 0) this.x = 0;
        if (this.y < 0) this.y = 0;
        if (this.x + size > canvasWidth) this.x = canvasWidth - size;
        if (this.y + size > canvasHeight) this.y = canvasHeight - size;
        
        // 随机移动速度：-3到3之间
        this.dx = (rand.nextDouble() * 2) - 1;
        if (Math.abs(dx) < 0.2) dx = (dx > 0) ? 0.2 : -0.2;
        
        this.dy = (rand.nextDouble() * 2) - 1;
        if (Math.abs(dy) < 0.2) dy = (dy > 0) ? 0.2 : -0.2;
        
        // 随机颜色
        this.color = new Color(
            rand.nextInt(256),
            rand.nextInt(256),
            rand.nextInt(256)
        );
    }
    
    /**
     * 普通随机初始化
     */
    public void initializeRandom(double width, double height) {
        // 随机大小：20到50之间
        this.size = 20 + rand.nextDouble() * 30;
        
        // 随机初始位置：在画布范围内
        this.x = rand.nextDouble() * (width - size * 2);
        this.y = rand.nextDouble() * (height - size * 2);
        
        // 随机移动速度
        this.dx = (rand.nextDouble() * 2) - 1;  // -1到1
        if (Math.abs(dx) < 0.2) dx = (dx > 0) ? 0.2 : -0.2;

        this.dy = (rand.nextDouble() * 2) - 1;  // -1到1
        if (Math.abs(dy) < 0.2) dy = (dy > 0) ? 0.2 : -0.2;
        
        // 随机颜色
        this.color = new Color(
            rand.nextInt(256),
            rand.nextInt(256),
            rand.nextInt(256)
        );
    }

    /**
     * 移动方法 - 带波浪轨迹
     */
    public void waveMove(Rectangle2D bounds) {
        // X方向：直线运动
        x += dx;

        // Y方向：添加波浪效果
        double wave = Math.sin(x * 0.02) * 15;
        y += dy + wave * 0.1;

        // 边界碰撞处理
        checkAndHandleBoundary(bounds);
    }

    /**
     * 全直线移动
     */
    public void straightLineMove(Rectangle2D bounds) {
        // X和Y方向都是直线运动
        x += dx;
        y += dy;

        // 边界碰撞处理
        checkAndHandleBoundary(bounds);
    }

    /**
     * X正弦，Y直线移动
     */
    public void sinXMove(Rectangle2D bounds) {
        // X方向：正弦运动
        x += Math.sin(y * 0.03) * 2 + dx;

        // Y方向：直线运动
        y += dy;

        // 边界碰撞处理
        checkAndHandleBoundary(bounds);
    }

    /**
     * 边界碰撞处理（通用方法）
     */
    private void checkAndHandleBoundary(Rectangle2D bounds) {
        if (x < bounds.getMinX()) {
            x = bounds.getMinX();
            dx = -dx;
        }
        if (x + getWidth() >= bounds.getMaxX()) {
            x = bounds.getMaxX() - getWidth();
            dx = -dx;
        }

        if (y < bounds.getMinY()) {
            y = bounds.getMinY();
            dy = -dy;
        }
        if (y + getHeight() >= bounds.getMaxY()) {
            y = bounds.getMaxY() - getHeight();
            dy = -dy;
        }
    }

    
    /**
     * 获取形状宽度
     */
    protected abstract double getWidth();
    
    /**
     * 获取形状高度
     */
    protected abstract double getHeight();
    
    /**
     * 获取形状
     */
    public abstract Shape getShape();
    
    /**
     * 获取颜色
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * 设置位置
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * 设置中心位置
     */
    public void setCenterPosition(double centerX, double centerY) {
        this.x = centerX - size / 2;
        this.y = centerY - size / 2;
    }
}