package storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.GameObject;
import model.Ball;
import model.Rectangle;
import model.Triangle;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * JSON格式的游戏数据管理器
 */
public class GameDataManager {
    private static GameDataManager instance;
    private ObjectMapper objectMapper;
    private static final String DATA_FILE = "D:\\others\\Interactive-Animation-System\\backups\\animation_data.json"; // 修改为主数据文件路径
    private static final String BACKUP_DIR = "D:\\others\\Interactive-Animation-System\\backups\\"; // 修改为绝对路径

    // 其余代码保持不变
    
    private GameDataManager() {
        // 初始化Jackson
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // 美化输出格式
    }
    
    public static GameDataManager getInstance() {
        if (instance == null) {
            instance = new GameDataManager();
        }
        return instance;
    }
    
    /**
     * 保存游戏状态到JSON文件
     */
    public boolean saveGameState(List<GameObject> objects) {
        try {
            // 创建备份目录
            Files.createDirectories(Paths.get(BACKUP_DIR));
            
            // 备份现有文件
            if (Files.exists(Paths.get(DATA_FILE))) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String backupFile = BACKUP_DIR + "backup_" + sdf.format(new Date()) + ".json";
                Files.copy(Paths.get(DATA_FILE), Paths.get(backupFile));
                System.out.println("✓ 已创建备份: " + backupFile);
            }
            
            // 创建数据对象
            GameData gameData = new GameData();
            gameData.setSaveTime(new Date());
            gameData.setObjectCount(objects.size());
            
            List<ObjectData> objectList = new ArrayList<>();
            for (GameObject obj : objects) {
                ObjectData objData = convertToObjectData(obj);
                if (objData != null) {
                    objectList.add(objData);
                }
            }
            gameData.setObjects(objectList);
            
            // 写入JSON文件
            objectMapper.writeValue(new File(DATA_FILE), gameData);
            
            System.out.println("✓ 游戏状态已保存到: " + DATA_FILE);
            System.out.println("✓ 保存了 " + objects.size() + " 个对象");
            return true;
            
        } catch (Exception e) {
            System.err.println("✗ 保存失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 从JSON文件加载游戏状态
     */
    public List<GameObject> loadGameState() {
        try {
            if (!Files.exists(Paths.get(DATA_FILE))) {
                System.out.println("ℹ️ 数据文件不存在，使用默认空场景");
                return new ArrayList<>();
            }
            
            // 读取JSON文件
            GameData gameData = objectMapper.readValue(new File(DATA_FILE), GameData.class);
            
            // 验证数据
            if (gameData.getObjects() == null) {
                return new ArrayList<>();
            }
            
            // 转换为GameObject列表
            List<GameObject> objects = new ArrayList<>();
            for (ObjectData objData : gameData.getObjects()) {
                GameObject obj = convertToGameObject(objData);
                if (obj != null) {
                    objects.add(obj);
                }
            }
            
            System.out.println("✓ 从文件加载了 " + objects.size() + " 个对象");
            return objects;
            
        } catch (Exception e) {
            System.err.println("✗ 加载失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * GameObject 转换为 ObjectData
     */
    private ObjectData convertToObjectData(GameObject obj) {
        try {
            ObjectData data = new ObjectData();
            
            // 基础属性
            data.setType(obj.getClass().getSimpleName());
            data.setX(obj.getX());
            data.setY(obj.getY());
            data.setDx(obj.getDx());
            data.setDy(obj.getDy());
            
            // 获取大小（需要根据实际情况调整）
            try {
                java.lang.reflect.Method getWidth = obj.getClass().getMethod("getWidth");
                double size = (double) getWidth.invoke(obj);
                data.setSize(size);
            } catch (Exception e) {
                data.setSize(50.0); // 默认值
            }
            
            // 颜色信息（如果GameObject有getColor方法）
            try {
                java.lang.reflect.Method getColor = obj.getClass().getMethod("getColor");
                java.awt.Color color = (java.awt.Color) getColor.invoke(obj);
                if (color != null) {
                    data.setRed(color.getRed());
                    data.setGreen(color.getGreen());
                    data.setBlue(color.getBlue());
                }
            } catch (Exception e) {
                // 忽略颜色信息
            }
            
            // 状态
            data.setPaused(obj.isPaused());
            data.setColliding(obj.isColliding());
            
            return data;
            
        } catch (Exception e) {
            System.err.println("✗ 转换对象数据失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * ObjectData 转换为 GameObject
     */
    private GameObject convertToGameObject(ObjectData data) {
        try {
            GameObject obj = null;
            String type = data.getType();
            
            // 根据类型创建对象（使用临时画布尺寸）
            switch (type) {
                case "Ball":
                    obj = new Ball(data.getX() + data.getSize()/2, 
                                  data.getY() + data.getSize()/2, 
                                  1000, 800);
                    break;
                case "Rectangle":
                    obj = new Rectangle(data.getX() + data.getSize()/2,
                                       data.getY() + data.getSize()/2,
                                       1000, 800);
                    break;
                case "Triangle":
                    obj = new Triangle(data.getX() + data.getSize()/2,
                                      data.getY() + data.getSize()/2,
                                      1000, 800);
                    break;
                default:
                    System.err.println("✗ 未知的对象类型: " + type);
                    return null;
            }
            
            // 设置位置和速度
            obj.setPosition(data.getX(), data.getY());
            obj.setDx(data.getDx());
            obj.setDy(data.getDy());
            
            // 设置状态
            if (data.isPaused()) {
                obj.togglePause(); // 如果原来是暂停的
            }
            
            if (data.isColliding()) {
                obj.setColliding(true);
            }
            
            return obj;
            
        } catch (Exception e) {
            System.err.println("✗ 创建对象失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 数据类定义（Jackson会自动序列化/反序列化）
     */
    public static class GameData {
        private String version = "1.0";
        private Date saveTime;
        private int objectCount;
        private List<ObjectData> objects;
        
        // getters and setters
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public Date getSaveTime() { return saveTime; }
        public void setSaveTime(Date saveTime) { this.saveTime = saveTime; }
        
        public int getObjectCount() { return objectCount; }
        public void setObjectCount(int objectCount) { this.objectCount = objectCount; }
        
        public List<ObjectData> getObjects() { return objects; }
        public void setObjects(List<ObjectData> objects) { this.objects = objects; }
    }
    
    public static class ObjectData {
        private String type;        // 对象类型：Ball, Rectangle, Triangle
        private double x;           // X坐标
        private double y;           // Y坐标
        private double dx;          // X方向速度
        private double dy;          // Y方向速度
        private double size;        // 大小
        private int red = 255;      // 颜色红色分量
        private int green = 255;    // 颜色绿色分量
        private int blue = 255;     // 颜色蓝色分量
        private boolean paused;     // 是否暂停
        private boolean colliding;  // 是否碰撞
        
        // getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        
        public double getDx() { return dx; }
        public void setDx(double dx) { this.dx = dx; }
        
        public double getDy() { return dy; }
        public void setDy(double dy) { this.dy = dy; }
        
        public double getSize() { return size; }
        public void setSize(double size) { this.size = size; }
        
        public int getRed() { return red; }
        public void setRed(int red) { this.red = red; }
        
        public int getGreen() { return green; }
        public void setGreen(int green) { this.green = green; }
        
        public int getBlue() { return blue; }
        public void setBlue(int blue) { this.blue = blue; }
        
        public boolean isPaused() { return paused; }
        public void setPaused(boolean paused) { this.paused = paused; }
        
        public boolean isColliding() { return colliding; }
        public void setColliding(boolean colliding) { this.colliding = colliding; }
    }
}