// Room.java
package core;

import java.util.Random;

public class Room {
    public int xAx, yAx, width, height;

    public Room(int xAx, int yAx, int width, int height) {
        this.xAx = xAx; this.yAx = yAx;
        this.width = width; this.height = height;
    }

    // make sure rooms dont overlap
    public boolean stacks(Room other) {
        return this.xAx < other.xAx + other.width + 1 &&
                this.xAx + this.width + 1 > other.xAx &&
                this.yAx < other.yAx + other.height + 1 &&
                this.yAx + this.height + 1 > other.yAx;
    }

    // choose random x coord
    public int randomX(Random rand) {
        return xAx + 1 + rand.nextInt(width - 2);
    }

    // choose random y coord
    public int randomY(Random rand) {
        return yAx + 1 + rand.nextInt(height - 2);
    }

    public int centerX() { return xAx + width/2; }
    public int centerY() { return yAx + height/2; }
}
