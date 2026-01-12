package view;

import model.GameObject;
import model.Ball;
import model.Rectangle;
import model.Triangle;
import controller.GameObjectRunnable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * 形状绘制组件
 */
public class ShapeComponent extends JComponent {
    private ArrayList<GameObject> objects = new ArrayList<>();
    private String selectedShapeType = "圆形";

    public ShapeComponent() {
        setBackground(Color.WHITE);
        setOpaque(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                createShapeAtClick(e.getX(), e.getY());
            }
        });
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
    }

    public void clearAll() {
        objects.clear();
        repaint();
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
            g2.setColor(obj.getColor());
            g2.fill(obj.getShape());

            g2.setColor(obj.getColor().darker());
            g2.draw(obj.getShape());
        }
    }
}