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
    public static final int STEPS = 1000;
    public static final int DELAY = 5;
    /**
     * 新增的随机数发生器
     */
    private Random random;
    private int moveType;

    public GameObjectRunnable(GameObject obj, Component component) {
        this.obj = obj;
        this.component = component;
        this.random = new Random();
        // 随机选择一种移动轨迹
        this.moveType = random.nextInt(3);
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < STEPS; i++) {
                // 检查线程是否被中断
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }

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
            }
        } catch (InterruptedException e) {
            // 线程被中断，正常退出
            System.out.println("动画线程被停止: " + getMoveTypeDescription());
        }
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