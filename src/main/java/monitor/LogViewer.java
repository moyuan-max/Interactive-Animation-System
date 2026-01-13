package monitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 简单的日志查看器
 */
public class LogViewer extends JFrame {
    private EnhancedFileLogger logger;
    private JTextArea logArea;
    private JButton refreshBtn;
    private JButton openDirBtn;
    
    public LogViewer() {
        logger = EnhancedFileLogger.getInstance();
        
        setTitle("日志查看器");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initUI();
    }
    
    private void initUI() {
        // 控制面板
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        refreshBtn = new JButton("刷新");
        openDirBtn = new JButton("打开日志目录");
        JButton clearBtn = new JButton("清空显示");
        JButton closeBtn = new JButton("关闭");
        
        refreshBtn.addActionListener(e -> refreshLogs());
        openDirBtn.addActionListener(e -> logger.openLogDirectory());
        clearBtn.addActionListener(e -> logArea.setText(""));
        closeBtn.addActionListener(e -> dispose());
        
        controlPanel.add(refreshBtn);
        controlPanel.add(openDirBtn);
        controlPanel.add(clearBtn);
        controlPanel.add(closeBtn);
        
        // 日志显示区域
        logArea = new JTextArea();
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        
        // 状态栏
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statusLabel = new JLabel("日志文件: " + logger.getLogFilePath());
        statusPanel.add(statusLabel);
        
        // 自动刷新定时器
        Timer refreshTimer = new Timer(5000, e -> refreshLogs());
        refreshTimer.start();
        
        // 布局
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        // 初始刷新
        refreshLogs();
    }
    
    private void refreshLogs() {
        String recentLogs = logger.getRecentLogs(100); // 显示最近100行
        logArea.setText(recentLogs);
        logArea.setCaretPosition(logArea.getDocument().getLength()); // 滚动到底部
    }
    
    public static void showViewer() {
        SwingUtilities.invokeLater(() -> {
            LogViewer viewer = new LogViewer();
            viewer.setVisible(true);
        });
    }
}