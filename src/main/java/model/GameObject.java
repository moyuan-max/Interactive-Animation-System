// [file name]: GameObject.java
// [file content begin]
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

    /* 第五次修改内容：添加碰撞相关属性 */
    private boolean isColliding;     // 是否处于碰撞状态
    private Color originalColor;     // 原始颜色
    private long collisionStartTime; // 碰撞开始时间
    private static final long COLLISION_DISPLAY_DURATION = 200; // 碰撞显示时间（毫秒）

    /* 恢复原有功能：添加悬停和暂停状态 */
    private boolean isHovered;       // 是否被鼠标悬停
    private boolean isPaused;        // 是否暂停
    private Color hoverColor;        // 悬停时的颜色（变浅）

    public GameObject() {
        this.rand = new Random();
        this.isColliding = false;
        this.isHovered = false;
        this.isPaused = false;
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
        this.originalColor = this.color;
        this.hoverColor = this.color.brighter();
    }

    /**
     * 普通随机初始化
     */
    public void initializeRandom(double width, double height) {
        // 随机大小：10到30之间
        this.size = 10 + rand.nextDouble() * 20;

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
        this.originalColor = this.color;
        this.hoverColor = this.color.brighter();
    }

    /**
     * 移动方法 - 带波浪轨迹
     */
    public void waveMove(Rectangle2D bounds) {
        // 如果暂停，不移动
        if (isPaused) {
            return;
        }

        // X方向：直线运动
        x += dx;

        // Y方向：添加波浪效果
        double wave = Math.sin(x * 0.02) * 15;
        y += dy + wave * 0.1;

        // 边界碰撞处理
        checkAndHandleBoundary(bounds);

        // 第五次修改内容：更新碰撞显示状态
        updateCollisionDisplay();
    }

    /**
     * 全直线移动
     */
    public void straightLineMove(Rectangle2D bounds) {
        // 如果暂停，不移动
        if (isPaused) {
            return;
        }

        // X和Y方向都是直线运动
        x += dx;
        y += dy;

        // 边界碰撞处理
        checkAndHandleBoundary(bounds);

        // 第五次修改内容：更新碰撞显示状态
        updateCollisionDisplay();
    }

    /**
     * X正弦，Y直线移动
     */
    public void sinXMove(Rectangle2D bounds) {
        // 如果暂停，不移动
        if (isPaused) {
            return;
        }

        // X方向：正弦运动
        x += Math.sin(y * 0.03) * 2 + dx;

        // Y方向：直线运动
        y += dy;

        // 边界碰撞处理
        checkAndHandleBoundary(bounds);

        // 第五次修改内容：更新碰撞显示状态
        updateCollisionDisplay();
    }

    /**
     * 边界碰撞处理（通用方法）
     */
    private void checkAndHandleBoundary(Rectangle2D bounds) {
        if (x < bounds.getMinX()) {
            x = bounds.getMinX();
            dx = -dx * 0.9; // 第五次修改内容：添加边界碰撞阻尼
        }
        if (x + getWidth() >= bounds.getMaxX()) {
            x = bounds.getMaxX() - getWidth();
            dx = -dx * 0.9; // 第五次修改内容：添加边界碰撞阻尼
        }

        if (y < bounds.getMinY()) {
            y = bounds.getMinY();
            dy = -dy * 0.9; // 第五次修改内容：添加边界碰撞阻尼
        }
        if (y + getHeight() >= bounds.getMaxY()) {
            y = bounds.getMaxY() - getHeight();
            dy = -dy * 0.9; // 第五次修改内容：添加边界碰撞阻尼
        }
    }

    /**
     * 第五次修改内容：更新碰撞显示状态
     * 如果碰撞时间超过指定时长，则恢复原色
     */
    private void updateCollisionDisplay() {
        if (isColliding) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - collisionStartTime > COLLISION_DISPLAY_DURATION) {
                isColliding = false;
                color = originalColor;
                hoverColor = color.brighter(); // 更新悬停颜色
            }
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
        if (isColliding) {
            // 第五次修改内容：碰撞时显示反色
            return getInvertedColor();
        } else if (isHovered) {
            // 悬停时显示变浅颜色
            return hoverColor;
        }
        return color;
    }

    /**
     * 第五次修改内容：获取反色
     */
    private Color getInvertedColor() {
        int red = 255 - color.getRed();
        int green = 255 - color.getGreen();
        int blue = 255 - color.getBlue();
        return new Color(red, green, blue);
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

    /* 第五次修改内容：添加碰撞相关方法 */

    /**
     * 获取X坐标
     */
    public double getX() {
        return x;
    }

    /**
     * 获取Y坐标
     */
    public double getY() {
        return y;
    }

    /**
     * 获取X方向速度
     */
    public double getDx() {
        return dx;
    }

    /**
     * 获取Y方向速度
     */
    public double getDy() {
        return dy;
    }

    /**
     * 设置X方向速度
     */
    public void setDx(double dx) {
        this.dx = dx;
    }

    /**
     * 设置Y方向速度
     */
    public void setDy(double dy) {
        this.dy = dy;
    }

    /**
     * 设置碰撞状态
     */
    public void setColliding(boolean colliding) {
        this.isColliding = colliding;
        if (colliding) {
            collisionStartTime = System.currentTimeMillis();
        } else {
            color = originalColor;
            hoverColor = color.brighter(); // 更新悬停颜色
        }
    }

    /**
     * 检查是否处于碰撞状态
     */
    public boolean isColliding() {
        return isColliding;
    }

    /* 恢复原有功能：添加悬停和暂停相关方法 */

    /**
     * 设置悬停状态
     */
    public void setHovered(boolean hovered) {
        this.isHovered = hovered;
    }

    /**
     * 检查是否被悬停
     */
    public boolean isHovered() {
        return isHovered;
    }

    /**
     * 切换暂停状态
     */
    public void togglePause() {
        this.isPaused = !this.isPaused;
    }

    /**
     * 检查是否暂停
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * 检查点是否在形状内
     */
    public boolean contains(Point2D point) {
        return getShape().contains(point);
    }
}
// [file content end]