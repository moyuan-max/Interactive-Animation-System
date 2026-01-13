package view;

import model.GameObject;
import model.Ball;
import model.Rectangle;
import model.Triangle;
import controller.GameObjectRunnable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 形状绘制组件
 */
public class ShapeComponent extends JComponent {
    private ArrayList<GameObject> objects = new ArrayList<>();
    private String selectedShapeType = "圆形";
    private ConcurrentHashMap<GameObject, GameObjectRunnable> animationThreads = new ConcurrentHashMap<>();
    private ConcurrentHashMap<GameObject, Thread> threads = new ConcurrentHashMap<>();

    // 鼠标悬停状态
    private GameObject hoveredObject = null;
    private Cursor defaultCursor = null;

    public ShapeComponent() {
        setBackground(Color.WHITE);
        setOpaque(true);

        // 保存默认光标
        defaultCursor = getCursor();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 双击事件
                if (e.getClickCount() == 2) {
                    handleDoubleClick(e.getX(), e.getY());
                    return;
                }

                // 单击事件
                if (e.getClickCount() == 1) {
                    GameObject clickedObject = findObjectAt(e.getX(), e.getY());
                    if (clickedObject != null) {
                        // 点击了已有的图形 - 停止/重启动画
                        toggleAnimation(clickedObject);
                        // 添加点击视觉效果
                        addClickEffect(clickedObject, e.getX(), e.getY());
                    } else {
                        // 点击了空白区域 - 创建新图形
                        createShapeAtClick(e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // 鼠标进入组件时恢复默认光标
                setCursor(defaultCursor);
            }
        });

        // 添加鼠标移动监听器来改善悬停效果
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                GameObject obj = findObjectAt(e.getX(), e.getY());

                // 更新悬停状态
                if (obj != hoveredObject) {
                    hoveredObject = obj;
                    if (obj != null) {
                        // 鼠标在图形上时变为手形光标
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(defaultCursor);
                    }
                }
            }
        });
    }

    /**
     * 改进的命中检测方法
     */
    private GameObject findObjectAt(int x, int y) {
        // 反向遍历，这样后添加的图形（在顶部）先被检测到
        for (int i = objects.size() - 1; i >= 0; i--) {
            GameObject obj = objects.get(i);

            // 使用更宽松的检测方式
            if (isPointInShape(obj, x, y)) {
                return obj;
            }
        }
        return null;
    }

    /**
     * 改进的点击检测 - 对每种形状使用合适的检测方法
     */
    private boolean isPointInShape(GameObject obj, int x, int y) {
        Shape shape = obj.getShape();

        // 方法1：直接使用contains方法（对简单形状有效）
        if (shape.contains(x, y)) {
            return true;
        }

        // 方法2：扩大检测区域（对三角形和不规则形状更好）
        // 创建一个稍大的形状用于检测
        double tolerance = 3.0; // 3像素的容差
        Area expandedArea = new Area(shape);

        // 如果对象是三角形，使用专门的检测
        if (obj instanceof Triangle) {
            return isPointInTriangle(obj, x, y);
        }

        // 对于矩形和圆形，创建更大的检测区域
        if (expandedArea.contains(x, y, tolerance, tolerance)) {
            return true;
        }

        // 方法3：检查是否在边界框附近
        Rectangle2D bounds = shape.getBounds2D();
        double expandedBoundsX = bounds.getX() - tolerance;
        double expandedBoundsY = bounds.getY() - tolerance;
        double expandedBoundsWidth = bounds.getWidth() + 2 * tolerance;
        double expandedBoundsHeight = bounds.getHeight() + 2 * tolerance;

        return (x >= expandedBoundsX && x <= expandedBoundsX + expandedBoundsWidth &&
                y >= expandedBoundsY && y <= expandedBoundsY + expandedBoundsHeight);
    }

    /**
     * 专门用于三角形的点击检测
     */
    private boolean isPointInTriangle(GameObject obj, double px, double py) {
        Shape shape = obj.getShape();
        Rectangle2D bounds = shape.getBounds2D();

        // 获取三角形的顶点（需要从Triangle类获取）
        if (obj instanceof Triangle) {
            // 由于Triangle的顶点是私有，我们使用形状的边界框进行近似检测
            double tolerance = 5.0;

            // 使用多边形的contains方法
            if (shape.contains(px, py)) {
                return true;
            }

            // 或者使用边界框的简化检测
            double centerX = bounds.getCenterX();
            double centerY = bounds.getCenterY();
            double distance = Math.sqrt(Math.pow(px - centerX, 2) + Math.pow(py - centerY, 2));

            // 如果点在边界框的中心附近
            return distance <= Math.min(bounds.getWidth(), bounds.getHeight()) / 2 + tolerance;
        }

        return false;
    }

    /**
     * 添加点击视觉效果
     */
    private void addClickEffect(GameObject obj, int x, int y) {
        // 创建一个点击效果
        Graphics2D g2 = (Graphics2D) getGraphics();
        if (g2 != null) {
            g2.setColor(Color.YELLOW);
            g2.setStroke(new BasicStroke(3));
            g2.draw(obj.getShape());

            // 立即重绘以移除效果
            Timer timer = new Timer(200, e -> {
                repaint();
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    /**
     * 处理双击事件 - 删除图形
     */
    private void handleDoubleClick(int x, int y) {
        GameObject clickedObject = findObjectAt(x, y);
        if (clickedObject != null) {
            // 显示确认对话框
            int response = JOptionPane.showConfirmDialog(
                    this,
                    "确定要删除这个图形吗？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (response == JOptionPane.YES_OPTION) {
                // 停止动画线程
                stopAnimation(clickedObject);

                // 从列表中移除
                objects.remove(clickedObject);

                // 移除悬停状态
                if (hoveredObject == clickedObject) {
                    hoveredObject = null;
                    setCursor(defaultCursor);
                }

                // 重绘
                repaint();

                // 显示删除提示
                showDeleteMessage(clickedObject);
            }
        }
    }

    /**
     * 切换动画状态（停止/恢复）
     */
    private void toggleAnimation(GameObject obj) {
        GameObjectRunnable runnable = animationThreads.get(obj);
        if (runnable != null) {
            Thread thread = threads.get(obj);
            if (thread != null && thread.isAlive()) {
                // 停止动画
                thread.interrupt();
                threads.remove(obj);
                animationThreads.remove(obj);
                showStopMessage(obj);

                // 为停止的图形添加视觉效果
                repaint();
            } else {
                // 重新启动动画
                startAnimation(obj);
                showStartMessage(obj);
            }
        }
    }

    /**
     * 停止指定图形的动画
     */
    private void stopAnimation(GameObject obj) {
        GameObjectRunnable runnable = animationThreads.get(obj);
        if (runnable != null) {
            Thread thread = threads.get(obj);
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            threads.remove(obj);
            animationThreads.remove(obj);
        }
    }

    /**
     * 显示删除提示
     */
    private void showDeleteMessage(GameObject obj) {
        String message = getObjectType(obj) + "已删除";
        showTemporaryMessage(message);
    }

    /**
     * 显示停止提示
     */
    private void showStopMessage(GameObject obj) {
        String type = getObjectType(obj);
        showTemporaryMessage(type + "动画已停止");
    }

    /**
     * 显示启动提示
     */
    private void showStartMessage(GameObject obj) {
        String type = getObjectType(obj);
        showTemporaryMessage(type + "动画已启动");
    }

    /**
     * 获取图形类型
     */
    private String getObjectType(GameObject obj) {
        if (obj instanceof Ball) {
            return "圆形";
        } else if (obj instanceof Rectangle) {
            return "矩形";
        } else if (obj instanceof Triangle) {
            return "三角形";
        }
        return "图形";
    }

    /**
     * 显示临时消息
     */
    private void showTemporaryMessage(String message) {
        Container parent = getParent();
        while (parent != null && !(parent instanceof JFrame)) {
            parent = parent.getParent();
        }

        if (parent instanceof JFrame) {
            JFrame frame = (JFrame) parent;
            String originalTitle = "多形状弹跳动画 - 点击画布生成形状";
            frame.setTitle(originalTitle + " | " + message);

            // 3秒后恢复原标题
            Timer timer = new Timer(3000, e ->
                    frame.setTitle(originalTitle)
            );
            timer.setRepeats(false);
            timer.start();
        }
    }

    public void setSelectedShapeType(String shapeType) {
        this.selectedShapeType = shapeType;
    }

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
            showTemporaryMessage("已创建" + selectedShapeType);
        }
    }

    public void addObject(GameObject obj) {
        objects.add(obj);
        repaint();
        startAnimation(obj);
    }

    private void startAnimation(GameObject obj) {
        GameObjectRunnable runnable = new GameObjectRunnable(obj, this);
        Thread thread = new Thread(runnable);
        thread.start();

        // 保存线程引用以便控制
        animationThreads.put(obj, runnable);
        threads.put(obj, thread);
    }

    public void clearAll() {
        // 询问确认
        int response = JOptionPane.showConfirmDialog(
                this,
                "确定要清空所有图形吗？",
                "确认清空",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            // 停止所有动画线程
            for (GameObject obj : objects) {
                stopAnimation(obj);
            }

            objects.clear();
            animationThreads.clear();
            threads.clear();
            hoveredObject = null;
            setCursor(defaultCursor);
            repaint();
            showTemporaryMessage("已清空所有图形");
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        for (GameObject obj : objects) {
            // 检查是否为停止状态
            boolean isRunning = animationThreads.containsKey(obj);

            // 绘制填充
            g2.setColor(obj.getColor());
            g2.fill(obj.getShape());

            // 绘制边框
            if (isRunning) {
                // 运行状态 - 正常边框
                g2.setColor(obj.getColor().darker());
                g2.setStroke(new BasicStroke(2));
                g2.draw(obj.getShape());

                // 悬停效果
                if (obj == hoveredObject) {
                    g2.setColor(new Color(255, 255, 0, 100)); // 半透明黄色
                    g2.fill(obj.getShape());
                }
            } else {
                // 停止状态 - 红色虚线边框
                Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
                g2.setStroke(dashed);
                g2.setColor(Color.RED);
                g2.draw(obj.getShape());

                // 在停止的图形上添加文字提示
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("微软雅黑", Font.BOLD, 12));
                String text = "已停止";
                Rectangle2D bounds = obj.getShape().getBounds2D();
                int textX = (int)bounds.getCenterX() - 20;
                int textY = (int)bounds.getCenterY() + 5;
                g2.drawString(text, textX, textY);
            }

            // 重置笔画
            g2.setStroke(new BasicStroke(1));
        }

        // 绘制提示信息
        if (objects.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("微软雅黑", Font.ITALIC, 16));
            String message = "点击此处创建" + selectedShapeType;
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(message);
            int x = (getWidth() - textWidth) / 2;
            int y = getHeight() / 2;
            g2.drawString(message, x, y);
        }
    }

    /**
     * 获取当前图形数量
     */
    public int getObjectCount() {
        return objects.size();
    }

    /**
     * 设置工具提示
     */
    @Override
    public String getToolTipText(MouseEvent event) {
        GameObject obj = findObjectAt(event.getX(), event.getY());
        if (obj != null) {
            boolean isRunning = animationThreads.containsKey(obj);
            String state = isRunning ? "运行中" : "已停止";
            String instruction = isRunning ? "单击停止" : "单击启动";

            return String.format("%s - %s (%s，双击删除)",
                    getObjectType(obj), state, instruction);
        }
        return "单击创建" + selectedShapeType;
    }
}