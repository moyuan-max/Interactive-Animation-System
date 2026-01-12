package controller;

import model.*;

/**
 * 形状工厂类
 */
public class ShapeFactory {
    
    /**
     * 创建指定类型的形状
     */
    public static GameObject createShape(String shapeType, 
                                        double x, double y, 
                                        double width, double height) {
        switch (shapeType) {
            case "圆形":
                return new Ball(x, y, width, height);
            case "矩形":
                return new Rectangle(x, y, width, height);
            case "三角形":
                return new Triangle(x, y, width, height);
            default:
                return new Ball(x, y, width, height);
        }
    }
    
    /**
     * 创建随机形状
     */
    public static GameObject createRandomShape(double width, double height) {
        String[] shapeTypes = {"圆形", "矩形", "三角形"};
        int randomIndex = (int)(Math.random() * shapeTypes.length);
        double randomX = Math.random() * (width - 50) + 25;
        double randomY = Math.random() * (height - 50) + 25;
        
        return createShape(shapeTypes[randomIndex], randomX, randomY, width, height);
    }
}