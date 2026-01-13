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
            // 将固定步数的循环改为无限循环
            while (!Thread.currentThread().isInterrupted()) {
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
            // 线程被中断，可能是暂停或删除
            // 这里不打印消息，由调用方控制消息显示
            Thread.currentThread().interrupt(); // 重新设置中断状态
        }
    }
}