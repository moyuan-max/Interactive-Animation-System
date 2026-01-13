package monitor;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

/**
 * 增强版简单文件日志系统
 * 支持多线程安全、自动滚动、性能监控
 */
public class EnhancedFileLogger {
    private static EnhancedFileLogger instance;
    private File logFile;
    private PrintWriter writer;
    private final ConcurrentLinkedQueue<String> logQueue;
    private Thread logThread;
    private volatile boolean running;
    
    // 统计信息
    private AtomicInteger collisionCount;
    private AtomicInteger objectCount;
    private AtomicInteger frameCount;
    private Date startTime;
    
    // 配置
    private static final int MAX_FILE_SIZE_MB = 10;
    private static final int MAX_LOG_FILES = 5;
    private static final boolean ENABLE_AUTO_FLUSH = true;
    private static final int FLUSH_INTERVAL_MS = 1000;
    
    private EnhancedFileLogger() {
        logQueue = new ConcurrentLinkedQueue<>();
        collisionCount = new AtomicInteger(0);
        objectCount = new AtomicInteger(0);
        frameCount = new AtomicInteger(0);
        startTime = new Date();
        
        initializeLogger();
        startLogThread();
    }
    
    public static EnhancedFileLogger getInstance() {
        if (instance == null) {
            synchronized (EnhancedFileLogger.class) {
                if (instance == null) {
                    instance = new EnhancedFileLogger();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化日志系统
     */
    private void initializeLogger() {
        try {
            // 创建日志目录
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            // 清理旧日志文件
            cleanupOldLogs(logDir);
            
            // 创建新日志文件
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = "game_log_" + sdf.format(new Date()) + ".txt";
            logFile = new File(logDir, fileName);
            
            writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)), ENABLE_AUTO_FLUSH);
            
            // 写入日志头
            writeHeader();
            
            System.out.println("日志系统已初始化: " + logFile.getAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("日志系统初始化失败: " + e.getMessage());
            // 如果文件写入失败，回退到控制台输出
            writer = new PrintWriter(System.out);
        }
    }
    
    /**
     * 写入日志头信息
     */
    private void writeHeader() {
        writer.println("=".repeat(80));
        writer.println("游戏日志系统 - 增强版");
        writer.println("启动时间: " + startTime);
        writer.println("日志文件: " + logFile.getName());
        writer.println("系统信息: " + System.getProperty("os.name") + " " + 
                      System.getProperty("os.version"));
        writer.println("Java版本: " + System.getProperty("java.version"));
        writer.println("=".repeat(80));
        writer.println();
        writer.flush();
    }
    
    /**
     * 启动日志写入线程
     */
    private void startLogThread() {
        running = true;
        logThread = new Thread(() -> {
            while (running || !logQueue.isEmpty()) {
                try {
                    // 批量写入日志
                    StringBuilder batch = new StringBuilder();
                    int count = 0;
                    
                    while (count < 100 && !logQueue.isEmpty()) {
                        String log = logQueue.poll();
                        if (log != null) {
                            batch.append(log).append("\n");
                            count++;
                        }
                    }
                    
                    if (batch.length() > 0) {
                        writer.write(batch.toString());
                        if (ENABLE_AUTO_FLUSH) {
                            writer.flush();
                        }
                    }
                    
                    // 定期强制刷新
                    Thread.sleep(FLUSH_INTERVAL_MS);
                    
                    // 检查文件大小
                    checkFileSize();
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("日志写入线程异常: " + e.getMessage());
                }
            }
            
            // 线程结束时确保所有日志都写入
            flushRemainingLogs();
        }, "Log-Writer-Thread");
        
        logThread.setDaemon(true); // 设置为守护线程
        logThread.start();
    }
    
    /**
     * 检查并限制日志文件大小
     */
    private void checkFileSize() {
        if (logFile.length() > MAX_FILE_SIZE_MB * 1024 * 1024) {
            rotateLogFile();
        }
    }
    
    /**
     * 轮转日志文件
     */
    private synchronized void rotateLogFile() {
        try {
            writer.close();
            
            // 重命名当前文件
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String newName = "game_log_" + sdf.format(new Date()) + "_full.txt";
            File newFile = new File(logFile.getParent(), newName);
            
            if (logFile.renameTo(newFile)) {
                // 创建新文件
                writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)), ENABLE_AUTO_FLUSH);
                writeHeader();
                logQueue.offer("[系统] 日志文件已轮转: " + newFile.getName());
            }
            
        } catch (IOException e) {
            System.err.println("日志文件轮转失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理旧的日志文件
     */
    private void cleanupOldLogs(File logDir) {
        File[] logFiles = logDir.listFiles((dir, name) -> 
            name.startsWith("game_log_") && name.endsWith(".txt"));
        
        if (logFiles != null && logFiles.length > MAX_LOG_FILES) {
            // 按修改时间排序
            java.util.Arrays.sort(logFiles, (f1, f2) -> 
                Long.compare(f1.lastModified(), f2.lastModified()));
            
            // 删除最旧的文件
            for (int i = 0; i < logFiles.length - MAX_LOG_FILES; i++) {
                if (logFiles[i].delete()) {
                    System.out.println("删除旧日志文件: " + logFiles[i].getName());
                }
            }
        }
    }
    
    /**
     * 记录各种事件
     */
    
    public void logGameStart() {
        log("游戏启动");
        logSystemInfo();
    }
    
    public void logGameEnd() {
        logStatistics();
        log("游戏结束");
    }
    
    public void logCollision(String obj1Type, String obj2Type) {
        int count = collisionCount.incrementAndGet();
        String entry = String.format("[碰撞#%d] %s ↔ %s | 时间: %s",
            count, obj1Type, obj2Type, getCurrentTime());
        log(entry);
    }
    
    public void logObjectCreated(String type, double x, double y) {
        int count = objectCount.incrementAndGet();
        String entry = String.format("[创建#%d] %s @ (%.1f, %.1f) | 时间: %s",
            count, type, x, y, getCurrentTime());
        log(entry);
    }
    
    public void logObjectRemoved(String type) {
        int count = objectCount.decrementAndGet();
        String entry = String.format("[移除] %s | 剩余: %d | 时间: %s",
            type, count, getCurrentTime());
        log(entry);
    }
    
    public void logFrameUpdate() {
        frameCount.incrementAndGet();
        // 每100帧记录一次
        if (frameCount.get() % 100 == 0) {
            log(String.format("[帧#%d] 运行中... | 时间: %s", 
                frameCount.get(), getCurrentTime()));
        }
    }
    
    public void logMouseEvent(String eventType, int x, int y) {
        String entry = String.format("[鼠标] %s @ (%d, %d) | 时间: %s",
            eventType, x, y, getCurrentTime());
        log(entry);
    }
    
    public void logError(String message, Exception e) {
        String entry = String.format("[错误] %s | 异常: %s | 时间: %s",
            message, e.getMessage(), getCurrentTime());
        log(entry);
        e.printStackTrace(); // 同时输出到控制台
    }
    
    public void logPerformance(long durationMs, String operation) {
        if (durationMs > 100) { // 只记录耗时较长的操作
            String entry = String.format("[性能] %s 耗时: %dms | 时间: %s",
                operation, durationMs, getCurrentTime());
            log(entry);
        }
    }
    
    /**
     * 通用日志方法
     */
    public void log(String message) {
        String timestamp = getCurrentTime();
        String fullMessage = String.format("[%s] %s", timestamp, message);
        
        // 添加到队列
        logQueue.offer(fullMessage);
        
        // 同时输出到控制台（可选）
        System.out.println(fullMessage);
    }
    
    /**
     * 记录统计信息
     */
    private void logStatistics() {
        long runTime = System.currentTimeMillis() - startTime.getTime();
        double minutes = runTime / 60000.0;
        
        StringBuilder stats = new StringBuilder();
        stats.append("\n").append("=".repeat(80)).append("\n");
        stats.append("游戏运行统计\n");
        stats.append("=".repeat(80)).append("\n");
        stats.append(String.format("运行时间: %.1f 分钟\n", minutes));
        stats.append(String.format("总帧数: %d\n", frameCount.get()));
        stats.append(String.format("总碰撞次数: %d\n", collisionCount.get()));
        stats.append(String.format("峰值对象数: %d\n", objectCount.get()));
        stats.append(String.format("平均碰撞率: %.2f 次/分钟\n", 
            collisionCount.get() / minutes));
        stats.append(String.format("平均帧率: %.1f FPS\n", 
            frameCount.get() / (runTime / 1000.0)));
        stats.append("=".repeat(80)).append("\n");
        
        logQueue.offer(stats.toString());
    }
    
    /**
     * 记录系统信息
     */
    private void logSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        
        StringBuilder info = new StringBuilder();
        info.append("系统信息:\n");
        info.append(String.format("可用处理器: %d\n", runtime.availableProcessors()));
        info.append(String.format("最大内存: %.1f MB\n", runtime.maxMemory() / 1024.0 / 1024.0));
        info.append(String.format("总内存: %.1f MB\n", runtime.totalMemory() / 1024.0 / 1024.0));
        info.append(String.format("空闲内存: %.1f MB\n", runtime.freeMemory() / 1024.0 / 1024.0));
        
        logQueue.offer(info.toString());
    }
    
    /**
     * 获取当前时间戳
     */
    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }
    
    /**
     * 刷新剩余日志
     */
    private void flushRemainingLogs() {
        try {
            while (!logQueue.isEmpty()) {
                String log = logQueue.poll();
                if (log != null) {
                    writer.println(log);
                }
            }
            writer.flush();
        } catch (Exception e) {
            System.err.println("刷新日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 关闭日志系统
     */
    public void shutdown() {
        running = false;
        
        if (logThread != null) {
            try {
                logThread.join(5000); // 等待5秒
            } catch (InterruptedException e) {
                logThread.interrupt();
            }
        }
        
        if (writer != null) {
            writer.close();
        }
        
        System.out.println("日志系统已关闭");
    }
    
    /**
     * 获取日志文件路径
     */
    public String getLogFilePath() {
        return logFile != null ? logFile.getAbsolutePath() : "未创建日志文件";
    }
    
    /**
     * 打开日志文件所在目录
     */
    public void openLogDirectory() {
        try {
            if (logFile != null && logFile.exists()) {
                java.awt.Desktop.getDesktop().open(logFile.getParentFile());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "无法打开日志目录: " + e.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 查看最近日志
     */
    public String getRecentLogs(int lines) {
        if (logFile == null || !logFile.exists()) {
            return "日志文件不存在";
        }
        
        StringBuilder recent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            java.util.List<String> logLines = new java.util.ArrayList<>();
            String line;
            
            while ((line = reader.readLine()) != null) {
                logLines.add(line);
            }
            
            // 获取最后几行
            int start = Math.max(0, logLines.size() - lines);
            for (int i = start; i < logLines.size(); i++) {
                recent.append(logLines.get(i)).append("\n");
            }
            
        } catch (IOException e) {
            return "读取日志失败: " + e.getMessage();
        }
        
        return recent.toString();
    }
}