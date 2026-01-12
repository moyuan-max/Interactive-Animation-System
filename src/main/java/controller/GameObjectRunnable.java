package controller;

import model.GameObject;
import java.awt.*;

/**
 * 游戏对象的动画线程
 */
public class GameObjectRunnable implements Runnable {
    private GameObject obj;
    private Component component;
    public static final int STEPS = 1000;
    public static final int DELAY = 5;
    
    public GameObjectRunnable(GameObject obj, Component component) {
        this.obj = obj;
        this.component = component;
    }
    
    @Override
    public void run() {
        try {
            for (int i = 0; i < STEPS; i++) {
                obj.move(component.getBounds());
                component.repaint();
                Thread.sleep(DELAY);
            }
        } catch (InterruptedException e) {
            // 线程被中断，正常退出
        }
    }
}