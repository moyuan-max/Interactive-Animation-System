package controller;

import model.GameObject;
import monitor.EnhancedFileLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * 碰撞管理器
 * 第五次修改内容：新增碰撞管理器类
 */
public class CollisionManager {
    private static CollisionManager instance;
    private Set<String> collisionPairs = new HashSet<>();

    private CollisionManager() {}

    public static CollisionManager getInstance() {
        if (instance == null) {
            instance = new CollisionManager();
        }
        return instance;
    }

    /**
     * 检测所有对象之间的碰撞
     */
    public void detectCollisions(ArrayList<GameObject> objects) {
        collisionPairs.clear();

        for (int i = 0; i < objects.size(); i++) {
            for (int j = i + 1; j < objects.size(); j++) {
                GameObject obj1 = objects.get(i);
                GameObject obj2 = objects.get(j);

                // 检查是否已经检测过这对碰撞
                String pairKey1 = i + "-" + j;
                String pairKey2 = j + "-" + i;

                if (collisionPairs.contains(pairKey1) || collisionPairs.contains(pairKey2)) {
                    continue;
                }

                // 检测碰撞
                if (CollisionDetector.checkCollision(obj1, obj2)) {
                    // 标记这对对象为已碰撞
                    collisionPairs.add(pairKey1);

                    // 触发碰撞响应
                    handleCollision(obj1, obj2);
                }
            }
        }
    }

    /**
     * 处理碰撞响应
     */
    private void handleCollision(GameObject obj1, GameObject obj2) {
        // 标记对象为碰撞状态（用于反色显示）
        obj1.setColliding(true);
        obj2.setColliding(true);

        // 处理物理碰撞（位移和反弹）
        processPhysicsCollision(obj1, obj2);

        EnhancedFileLogger.getInstance().logCollision(
                obj1.getClass().getSimpleName(),
                obj2.getClass().getSimpleName()
        );
    }

    /**
     * 处理物理碰撞
     */
    private void processPhysicsCollision(GameObject obj1, GameObject obj2) {
        // 计算反弹方向并移动对象
        resolveCollision(obj1, obj2);
    }

    /**
     * 解决碰撞（分离两个对象）
     */
    private void resolveCollision(GameObject obj1, GameObject obj2) {

        // 获取重叠区域
        java.awt.geom.Rectangle2D overlap = CollisionDetector.getOverlapBounds(obj1, obj2);
        if (overlap == null) {
            return;
        }

        // 计算两个对象的中心点
        java.awt.geom.Rectangle2D bounds1 = obj1.getShape().getBounds2D();
        java.awt.geom.Rectangle2D bounds2 = obj2.getShape().getBounds2D();

        double centerX1 = bounds1.getCenterX();
        double centerY1 = bounds1.getCenterY();
        double centerX2 = bounds2.getCenterX();
        double centerY2 = bounds2.getCenterY();

        // 计算碰撞法线方向
        double dx = centerX2 - centerX1;
        double dy = centerY2 - centerY1;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance == 0) {
            // 避免除以零
            dx = 1;
            dy = 0;
            distance = 1;
        }

        double nx = dx / distance;  // 法线x分量
        double ny = dy / distance;  // 法线y分量

        // 计算需要分离的距离（使用重叠区域的对角线长度作为参考）
        double separation = Math.sqrt(overlap.getWidth() * overlap.getWidth() +
                overlap.getHeight() * overlap.getHeight()) * 0.5;

        // 将两个对象沿法线方向分开
        obj1.setPosition(obj1.getX() - nx * separation * 0.5,
                obj1.getY() - ny * separation * 0.5);
        obj2.setPosition(obj2.getX() + nx * separation * 0.5,
                obj2.getY() + ny * separation * 0.5);

        // 反转速度方向（弹性碰撞）
        obj1.setDx(-obj1.getDx());
        obj1.setDy(-obj1.getDy());
        obj2.setDx(-obj2.getDx());
        obj2.setDy(-obj2.getDy());
    }

    /**
     * 清除所有碰撞状态
     */
    public void clearAllCollisions(ArrayList<GameObject> objects) {
        for (GameObject obj : objects) {
            obj.setColliding(false);
        }
        collisionPairs.clear();
    }
}
// [file content end]