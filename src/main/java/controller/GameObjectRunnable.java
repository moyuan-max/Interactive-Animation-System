// [file name]: GameObjectRunnable.java
// [file content begin]
package controller;

import model.GameObject;
import java.awt.*;
import java.util.Random;

/**
 * 游戏对象的动画线程
 */
public class GameObjectRunnable implements Runnable {
    private GameObject obj;
    private Component component;
    private volatile boolean running = true;

    /* 恢复原有功能：无限循环，持续运动 */
    public static final int DELAY = 5;

    /**
     * 新增的随机数发生器
     */
    private Random random;
    private int moveType;

    /* 第五次修改内容：添加帧率计数器 */
    private int frameCount;
    private static final int COLLISION_CHECK_INTERVAL = 3; // 每3帧检查一次碰撞

    public GameObjectRunnable(GameObject obj, Component component) {
        this.obj = obj;
        this.component = component;
        this.random = new Random();
        // 随机选择一种移动轨迹
        this.moveType = random.nextInt(3);
        this.frameCount = 0;
    }

    @Override
    public void run() {
        try {
            /* 恢复原有功能：无限循环，持续运动 */
            while (running) {
                // 根据随机选择的移动类型调用不同的移动方法
                switch (moveType) {
                    case 0:
                        obj.waveMove(component.getBounds());
                        break;
                    case 1:
                        obj.straightLineMove(component.getBounds());
                        break;
                    case 2:
                        obj.sinXMove(component.getBounds());
                        break;
                    default:
                        obj.waveMove(component.getBounds());
                }

                component.repaint();
                Thread.sleep(DELAY);

                // 第五次修改内容：定期更新碰撞检测（由碰撞管理器统一处理）
                frameCount++;
            }
        } catch (InterruptedException e) {
            // 线程被中断，正常退出
        }
    }

    /* 恢复原有功能：停止线程的方法 */
    public void stop() {
        running = false;
    }

    public String getMoveTypeDescription() {
        switch (moveType) {
            case 0:
                return "波浪轨迹";
            case 1:
                return "直线轨迹";
            case 2:
                return "X正弦轨迹";
            default:
                return "未知轨迹";
        }
    }
}
// [file content end]