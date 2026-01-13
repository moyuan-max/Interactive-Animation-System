// [file name]: ShapeComponent.java
// [file content begin]
package view;

import model.GameObject;
import model.Ball;
import model.Rectangle;
import model.Triangle;
import controller.GameObjectRunnable;
import controller.CollisionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 形状绘制组件
 */
public class ShapeComponent extends JComponent {
    private ArrayList<GameObject> objects = new ArrayList<>();
    private String selectedShapeType = "圆形";

    /* 第五次修改内容：添加碰撞管理器和定时器 */
    private CollisionManager collisionManager;
    private Timer collisionTimer;

    /* 恢复原有功能：存储图形与线程的映射 */
    private Map<GameObject, GameObjectRunnable> threadMap = new HashMap<>();

    /* 恢复原有功能：鼠标事件相关 */
    private GameObject hoveredObject = null;
    private Timer doubleClickTimer;
    private boolean isDoubleClick = false;
    private GameObject lastClickedObject = null;
    private long lastClickTime = 0;

    public ShapeComponent() {
        setBackground(Color.WHITE);
        setOpaque(true);

        /* 第五次修改内容：初始化碰撞管理器 */
        collisionManager = CollisionManager.getInstance();

        /* 第五次修改内容：初始化碰撞检测定时器 */
        collisionTimer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 每20毫秒检测一次碰撞
                collisionManager.detectCollisions(objects);
                repaint(); // 触发重绘以显示反色效果
            }
        });
        collisionTimer.start();

        /* 恢复原有功能：初始化双击定时器 */
        doubleClickTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isDoubleClick = false;
                doubleClickTimer.stop();
            }
        });
        doubleClickTimer.setRepeats(false);

        /* 恢复原有功能：添加鼠标监听器 */
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                clearHoveredObject();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e);
            }
        });
    }

    /* 恢复原有功能：处理鼠标点击 */
    private void handleMouseClick(MouseEvent e) {
        Point2D point = e.getPoint();
        GameObject clickedObject = findObjectAt(point);

        if (clickedObject == null) {
            // 点击空白处，创建新形状
            createShapeAtClick(e.getX(), e.getY());
            return;
        }

        // 处理双击
        long currentTime = System.currentTimeMillis();
        if (clickedObject == lastClickedObject && (currentTime - lastClickTime) < 300) {
            // 双击：删除图形
            removeObject(clickedObject);
            lastClickedObject = null;
            isDoubleClick = true;
            doubleClickTimer.stop();
            return;
        }

        // 处理单击：暂停/恢复
        clickedObject.togglePause();
        repaint();

        lastClickedObject = clickedObject;
        lastClickTime = currentTime;

        // 启动双击检测定时器
        doubleClickTimer.restart();
    }

    /* 恢复原有功能：处理鼠标移动（悬停） */
    private void handleMouseMove(MouseEvent e) {
        Point2D point = e.getPoint();
        GameObject objectAtPoint = findObjectAt(point);

        if (hoveredObject != null && hoveredObject != objectAtPoint) {
            hoveredObject.setHovered(false);
            hoveredObject = null;
            repaint();
        }

        if (objectAtPoint != null && hoveredObject != objectAtPoint) {
            objectAtPoint.setHovered(true);
            hoveredObject = objectAtPoint;
            repaint();
        }
    }

    /* 恢复原有功能：清除悬停状态 */
    private void clearHoveredObject() {
        if (hoveredObject != null) {
            hoveredObject.setHovered(false);
            hoveredObject = null;
            repaint();
        }
    }

    /* 恢复原有功能：查找指定点的图形 */
    private GameObject findObjectAt(Point2D point) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            GameObject obj = objects.get(i);
            if (obj.contains(point)) {
                return obj;
            }
        }
        return null;
    }

    /* 恢复原有功能：创建新形状 */
    private void createShapeAtClick(int clickX, int clickY) {
        Dimension size = getSize();
        double width = size.width;
        double height = size.height;

        if (width <= 0 || height <= 0) {
            width = 400;
            height = 300;
        }

        GameObject obj = null;

        switch (selectedShapeType) {
            case "圆形":
                obj = new Ball(clickX, clickY, width, height);
                break;
            case "矩形":
                obj = new Rectangle(clickX, clickY, width, height);
                break;
            case "三角形":
                obj = new Triangle(clickX, clickY, width, height);
                break;
        }

        if (obj != null) {
            addObject(obj);
        }
    }

    public void setSelectedShapeType(String shapeType) {
        this.selectedShapeType = shapeType;
    }

    public void addObject(GameObject obj) {
        objects.add(obj);
        repaint();
        startAnimation(obj);
    }

    /* 恢复原有功能：开始动画 */
    private void startAnimation(GameObject obj) {
        GameObjectRunnable runnable = new GameObjectRunnable(obj, this);
        Thread thread = new Thread(runnable);
        threadMap.put(obj, runnable);
        thread.start();
    }

    /* 恢复原有功能：移除对象 */
    private void removeObject(GameObject obj) {
        // 停止对应的线程
        GameObjectRunnable runnable = threadMap.get(obj);
        if (runnable != null) {
            runnable.stop();
            threadMap.remove(obj);
        }

        // 从列表中移除
        objects.remove(obj);

        // 清除悬停状态
        if (hoveredObject == obj) {
            hoveredObject = null;
        }

        // 清除碰撞状态
        obj.setColliding(false);

        repaint();
    }

    public void clearAll() {
        /* 第五次修改内容：清除所有碰撞状态 */
        collisionManager.clearAllCollisions(objects);

        /* 恢复原有功能：停止所有线程 */
        for (GameObjectRunnable runnable : threadMap.values()) {
            runnable.stop();
        }
        threadMap.clear();

        objects.clear();
        hoveredObject = null;
        repaint();
    }

    /* 第五次修改内容：添加获取对象数量的方法 */
    public int getObjectCount() {
        return objects.size();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        /* 第五次修改内容：绘制所有对象 */
        for (GameObject obj : objects) {
            Color objColor = obj.getColor();
            g2.setColor(objColor);
            g2.fill(obj.getShape());

            // 如果对象处于暂停状态，用虚线绘制边框
            if (obj.isPaused()) {
                float[] dashPattern = {5, 5};
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10, dashPattern, 0));
                g2.setColor(Color.BLACK);
            } else if (obj.isColliding()) {
                // 如果对象处于碰撞状态，用更深的颜色绘制边框
                g2.setColor(objColor.darker().darker());
                g2.setStroke(new BasicStroke(3));
            } else {
                g2.setColor(objColor.darker());
                g2.setStroke(new BasicStroke(1));
            }
            g2.draw(obj.getShape());
        }
    }

    /**
     * 第五次修改内容：组件销毁时停止定时器
     */
    @Override
    protected void finalize() throws Throwable {
        if (collisionTimer != null && collisionTimer.isRunning()) {
            collisionTimer.stop();
        }
        if (doubleClickTimer != null && doubleClickTimer.isRunning()) {
            doubleClickTimer.stop();
        }
        super.finalize();
    }
}
// [file content end]