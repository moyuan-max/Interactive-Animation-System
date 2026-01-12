package main;

import view.BounceFrame;
import javax.swing.*;
import java.awt.*;

/**
 * 程序主类
 */
public class BounceThread {
    public static void main(String[] args) {
        // 使用事件分发线程启动GUI
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 设置跨平台外观
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                BounceFrame frame = new BounceFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                
                // 居中显示
                frame.setLocationRelativeTo(null);
            }
        });
    }
}