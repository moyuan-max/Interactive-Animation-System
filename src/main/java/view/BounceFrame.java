// [file name]: BounceFrame.java
// [file content begin]
package view;

import model.GameObject;  // 导入GameObject
import model.Ball;        // 导入Ball
import model.Rectangle;   // 导入Rectangle
import model.Triangle;    // 导入Triangle
import monitor.EnhancedFileLogger;
import monitor.LogViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 主窗口框架
 */
public class BounceFrame extends JFrame {
    private ShapeComponent comp;
    public static final int DEFAULT_WIDTH = 700;
    public static final int DEFAULT_HEIGHT = 500;

    /* 第五次修改内容：添加图形数量标签 */
    private JLabel countLabel;

    public BounceFrame() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setTitle("多形状弹跳动画 - 点击画布生成形状");

        comp = new ShapeComponent();
        add(comp, BorderLayout.CENTER);

        // 创建控制面板
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        // 创建顶部面板，包含提示标签和图形数量标签
        JPanel topPanel = new JPanel(new BorderLayout());

        // 添加提示标签
        JLabel hintLabel = new JLabel("提示：点击画布生成形状，使用单选按钮切换形状类型");
        hintLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        hintLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        topPanel.add(hintLabel, BorderLayout.WEST);

        // 添加图形数量标签
        countLabel = new JLabel("图形数量: 0");
        countLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        countLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        topPanel.add(countLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        /* 第五次修改内容：添加定时器更新状态 */
        Timer timer = new Timer(1000, e -> {
            countLabel.setText("图形数量: " + comp.getObjectCount());
        });
        timer.start();
    }

    /**
     * 创建控制面板
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 形状选择标签
        JLabel shapeLabel = new JLabel("选择形状: ");
        panel.add(shapeLabel);

        // 形状选择按钮组
        ButtonGroup shapeGroup = new ButtonGroup();

        // 圆形单选按钮
        JRadioButton circleButton = new JRadioButton("圆形", true);
        circleButton.addActionListener(e -> comp.setSelectedShapeType("圆形"));
        shapeGroup.add(circleButton);
        panel.add(circleButton);

        // 矩形单选按钮
        JRadioButton rectButton = new JRadioButton("矩形");
        rectButton.addActionListener(e -> comp.setSelectedShapeType("矩形"));
        shapeGroup.add(rectButton);
        panel.add(rectButton);

        // 三角形单选按钮
        JRadioButton triangleButton = new JRadioButton("三角形");
        triangleButton.addActionListener(e -> comp.setSelectedShapeType("三角形"));
        shapeGroup.add(triangleButton);
        panel.add(triangleButton);

        // 分隔符
        panel.add(new JSeparator(SwingConstants.VERTICAL));

        // 批量生成按钮
        JButton batchButton = new JButton("批量生成");
        batchButton.addActionListener(e -> generateMultipleShapes());
        panel.add(batchButton);

        // 清空按钮
        JButton clearButton = new JButton("清空");
        clearButton.addActionListener(e -> comp.clearAll());
        panel.add(clearButton);

        // 关闭按钮
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> System.exit(0));
        panel.add(closeButton);

        // 开始/停止记录按钮
        JButton startBtn = new JButton("开始记录");
        JButton stopBtn = new JButton("停止记录");
        JButton exportBtn = new JButton("导出当前");
        JButton exportAllBtn = new JButton("导出历史");


        panel.add(new JSeparator(SwingConstants.VERTICAL));

        JButton viewLogBtn = new JButton("查看日志");
        JButton logSnapshotBtn = new JButton("记录快照");

        viewLogBtn.addActionListener(e -> LogViewer.showViewer());
        logSnapshotBtn.addActionListener(e -> {
            EnhancedFileLogger.getInstance().log("手动记录快照");
            JOptionPane.showMessageDialog(this, "快照已记录到日志");
        });

        panel.add(viewLogBtn);
        panel.add(logSnapshotBtn);
        return panel;
    }

    /**
     * 批量生成随机形状
     */
    private void generateMultipleShapes() {
        Dimension compSize = comp.getSize();
        double width = compSize.getWidth();
        double height = compSize.getHeight();

        if (width <= 0 || height <= 0) {
            width = DEFAULT_WIDTH - 50;
            height = DEFAULT_HEIGHT - 100;
        }

        // 生成5个随机形状
        for (int i = 0; i < 5; i++) {
            GameObject obj = null;
            int randomType = (int)(Math.random() * 3);

            // 随机位置
            double randomX = Math.random() * (width - 40) + 20;
            double randomY = Math.random() * (height - 40) + 20;

            switch (randomType) {
                case 0:
                    obj = new Ball(randomX, randomY, width, height);
                    break;
                case 1:
                    obj = new Rectangle(randomX, randomY, width, height);
                    break;
                case 2:
                    obj = new Triangle(randomX, randomY, width, height);
                    break;
            }

            if (obj != null) {
                comp.addObject(obj);
            }
        }
    }
}
// [file content end]