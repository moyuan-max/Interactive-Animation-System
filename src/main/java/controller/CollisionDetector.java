// [file name]: CollisionDetector.java
// [file content begin]
package controller;

import model.GameObject;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

/**
 * 碰撞检测器
 * 第五次修改内容：新增碰撞检测器类
 */
public class CollisionDetector {

    /**
     * 检测两个游戏对象是否碰撞
     */
    public static boolean checkCollision(GameObject obj1, GameObject obj2) {
        if (obj1 == obj2) {
            return false;
        }

        // 获取两个对象的形状
        Shape shape1 = obj1.getShape();
        Shape shape2 = obj2.getShape();

        // 使用Area进行精确碰撞检测
        Area area1 = new Area(shape1);
        Area area2 = new Area(shape2);
        area1.intersect(area2);

        return !area1.isEmpty();
    }

    /**
     * 获取两个对象的重叠区域
     */
    public static Rectangle2D getOverlapBounds(GameObject obj1, GameObject obj2) {
        Rectangle2D bounds1 = obj1.getShape().getBounds2D();
        Rectangle2D bounds2 = obj2.getShape().getBounds2D();

        double x1 = Math.max(bounds1.getMinX(), bounds2.getMinX());
        double y1 = Math.max(bounds1.getMinY(), bounds2.getMinY());
        double x2 = Math.min(bounds1.getMaxX(), bounds2.getMaxX());
        double y2 = Math.min(bounds1.getMaxY(), bounds2.getMaxY());

        if (x1 < x2 && y1 < y2) {
            return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
        }

        return null;
    }

    /**
     * 计算两个对象之间的最小分离距离
     */
    public static double calculateSeparationDistance(GameObject obj1, GameObject obj2) {
        Rectangle2D bounds1 = obj1.getShape().getBounds2D();
        Rectangle2D bounds2 = obj2.getShape().getBounds2D();

        double dx = Math.abs(bounds1.getCenterX() - bounds2.getCenterX());
        double dy = Math.abs(bounds1.getCenterY() - bounds2.getCenterY());

        return Math.sqrt(dx * dx + dy * dy);
    }
}
// [file content end]

// [file name]: CollisionManager.java
// [file content begin]
