package ece.assign3;

import java.awt.geom.Point2D;

public class Enemy {
    public String name;
    public double bearing;
    public double heading;
    public double changeHeading;
    public double x, y;
    public double distance, speed;
    public long ctime;

    public Enemy(String name) {
        this.name = name;
    }

    public Point2D.Double getNextPosition(long gaussTime) {
        double diff = gaussTime - ctime;
        double nextX = x + Math.sin(heading) * speed * diff;
        double nextY = y + Math.cos(heading) * speed * diff;
        return new Point2D.Double(nextX, nextY);
    }
}
