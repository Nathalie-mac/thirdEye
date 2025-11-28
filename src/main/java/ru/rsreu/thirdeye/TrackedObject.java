package ru.rsreu.thirdeye;

import org.opencv.core.Point;
//Класс для хранения состояния уникального распознанного объекта
public class TrackedObject {
    private long timestamp = System.currentTimeMillis();
    private String className; // Имя класса
    private Point center; // Центр объекта
    private int id; // Уникальный идентификатор
    private Point topLeft; //верхняя левая граница рамки
    private Point bottomRight; //нижняя правая граница рамки

    public TrackedObject(String className, Point objectCenter, int uniqueId, Point topLeft, Point bottomRight, long times) {
        this.className = className;
        this.center = objectCenter;
        this.id = uniqueId;
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.timestamp = times;
    }

    public int getId() {
        return id;
    }

    public String getClassName() {
        return className;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) {return true;}
        if (obj == null || getClass() != obj.getClass()) {return false;}
        TrackedObject that = (TrackedObject) obj;
        return id == that.id && className.equals(that.className);
    }
    @Override
    public int hashCode() {
        int result = Integer.hashCode(id);
        result = 31 * result + className.hashCode(); // 31 - произвольно выбранное нечетное число
        return result;
    }

    @Override
    public String toString() {
        return String.format("ID=%d, класс=%s, создано=%d, bbox=(%.1f,%.1f)-(%.1f,%.1f)",
                id, className, timestamp, topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
    }

}
