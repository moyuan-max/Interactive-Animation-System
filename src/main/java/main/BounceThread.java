package main;

import view.BounceFrame;
import monitor.EnhancedFileLogger;  // 添加这一行
import javax.swing.*;
import java.awt.*;

public class BounceThread {
    public static void main(String[] args) {
        // 初始化日志系统
        EnhancedFileLogger logger = EnhancedFileLogger.getInstance();
        logger.logGameStart();

        // 添加关闭钩子，确保日志系统正确关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.logGameEnd();
            logger.shutdown();
        }));

        // 使用事件分发线程启动GUI
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 设置跨平台外观
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    logger.logError("设置外观失败", e);
                }

                BounceFrame frame = new BounceFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);

                // 居中显示
                frame.setLocationRelativeTo(null);

                // 记录窗口创建
                logger.log("主窗口已创建并显示");
            }
        });
    }
}