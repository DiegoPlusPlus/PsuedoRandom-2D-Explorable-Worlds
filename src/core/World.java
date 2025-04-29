package core;

import tileengine.TETile;
import tileengine.Tileset;
import java.awt.Point;
import java.util.*;
import edu.princeton.cs.algs4.StdDraw;
import java.awt.Color;
import java.awt.Font;


public class World {
    private final int width, height;
    private final long seed;
    private int xAxis, yAxis;   // player

    // NPC location initialization
    private int npcX = -1;
    private int npcY = -1;
    private int npc2X = -1;
    private int npc2Y = -1;


    public World(int width, int height, long seed) {
        this.width  = width;
        this.height = height;
        this.seed   = seed;
    }

    public TETile[][] worldCreate() {
        // Fill with NOTHING
        TETile[][] world = new TETile[width][height];
        for (int x = 0; x < width;  x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        Random rand = new Random(seed); // Deterministic hash
        List<Room> rooms = new ArrayList<>();


        int maxRooms = 40;
        int attempts = 0; // Just in case
        while (rooms.size() < maxRooms && attempts < 10000) {
            int rw = rand.nextInt(6) + 3; // At least 3 by 3
            int rh = rand.nextInt(5) + 3;
            int rx = rand.nextInt(width  - rw - 2) + 1;
            int ry = rand.nextInt(height - rh - 2) + 1;
            Room room = new Room(rx, ry, rw, rh);

            boolean overlap = false;
            for (Room r : rooms) {
                if (room.stacks(r)) {
                    overlap = true; break;
                }
            }

            if (overlap) { // CHANGE IMPLEMENTATION IF IT GETS TOO SLOW
                attempts++;
                continue;
            }

            makeRoom(world, room); // Create the room in the world (walls then floor)
            rooms.add(room);
            attempts++;
        }



        List<Point> roomCenters = new ArrayList<>();
        for (Room r : rooms) {
            roomCenters.add(new Point(r.randomX(rand), r.randomY(rand)));
        } // For every room choose a random floor

        boolean[] connected = new boolean[roomCenters.size()];
        connected[0] = true; // Create a boolean array to check which rooms are connected

        while (true) {
            int minFrom = -1; // Which conncted room TO which unconnected room
            int minTo = -1;
            double bestDistance = 999999.0;

            for (int i = 0; i < roomCenters.size(); i++) {
                if (!connected[i]) {
                    continue; // Only connected rooms
                }
                Point p1 = roomCenters.get(i);

                for (int j = 0; j < roomCenters.size(); j++) {
                    if (connected[j]) {
                        continue; // Only unconnected rooms
                    }
                    Point p2 = roomCenters.get(j);
                    // calc x difference + y difference
                    // and find the closest unconnected room to any connected room
                    double dist = Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
                    if (dist < bestDistance) {
                        bestDistance = dist;
                        minFrom = i;
                        minTo = j;
                    }
                }
            }

            if (minTo == -1) { // No more unconnected rooms
                break;
            }

            // Connect minFrom to minTo
            Point from = roomCenters.get(minFrom);
            Point to = roomCenters.get(minTo);

            if (rand.nextBoolean()) { // Randomly choose to make a horizontal or vertical hall
                makeHorizontalHallway(world, from.x, to.x, from.y); // L
                makeVerticalHallway(world, from.y, to.y, to.x);
            } else {
                makeVerticalHallway(world, from.y, to.y, from.x);
                makeHorizontalHallway(world, from.x, to.x, to.y);
            }

            connected[minTo] = true;
        }


        surroundFloors(world);

        // Place player (should be deterministic. Always bottom left)
        outer:
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (world[x][y] == Tileset.FLOOR) {
                    xAxis = x; yAxis = y;
                    world[x][y] = Tileset.AVATAR;
                    break outer;
                }
            }
        }

        // NPC always in top left of map
        topLeftNPC:
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                if (world[x][y] == Tileset.FLOOR) {
                    npcX  = x;
                    npcY  = y;
                    world[x][y] = Tileset.NPC;
                    break topLeftNPC;
                }
            }
        }

        // NPC always on top right of map
        topRightNPC:
        for (int x = width - 1; x >= 0; x--) {
            for (int y = height - 1; y >= 0; y--) {
                if (world[x][y] == Tileset.FLOOR) {
                    npc2X = x;
                    npc2Y = y;
                    world[x][y] = Tileset.NPC;
                    break topRightNPC;
                }
            }
        }

        return world;
    }


    private void makeRoom(TETile[][] wall, Room room) {
        // walls
        for (int x = room.xAx - 1; x <= room.xAx + room.width;  x++) {
            for (int y = room.yAx - 1; y <= room.yAx + room.height; y++) {
                if (inBounds(x,y)) wall[x][y] = Tileset.WALL;
            }
        }
        // floor
        for (int x = room.xAx; x < room.xAx + room.width;  x++) {
            for (int y = room.yAx; y < room.yAx + room.height; y++) {
                if (inBounds(x,y)) wall[x][y] = Tileset.FLOOR;
            }
        }
    }

    private void makeHorizontalHallway(TETile[][] wall, int x0, int x1, int y) {
        int start = Math.min(x0, x1);
        int end = Math.max(x0, x1);
        for (int x = start; x <= end; x++) {
            if (inBounds(x,y)) {
                wall[x][y] = Tileset.FLOOR;
            }
        }
    }

    private void makeVerticalHallway(TETile[][] wall, int y0, int y1, int x) {
        int start = Math.min(y0, y1);
        int end = Math.max(y0, y1);
        for (int y = start; y <= end; y++) {
            if (inBounds(x,y)) {
                wall[x][y] = Tileset.FLOOR;
            }
        }
    }

    private void surroundFloors(TETile[][] w) {
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (w[x][y] == Tileset.FLOOR) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = x + dx, ny = y + dy;
                            if (w[nx][ny] == Tileset.NOTHING) {
                                w[nx][ny] = Tileset.WALL;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public void movePlayer(TETile[][] world, char dir) {
        int nx = xAxis, ny = yAxis;
        if (dir == 'w') ny++;
        if (dir == 's') ny--;
        if (dir == 'a') nx--;
        if (dir == 'd') nx++;

        if (!inBounds(nx, ny)) return;

        // top left npc activation
        if (nx == npcX && ny == npcY) {
            topLeftNPCDialogue();
            return;
        }

        // top right npc activation
        if (nx == npc2X && ny == npc2Y) {
            topRightNPCDialogue();
            return;
        }

        // normal move
        if (world[nx][ny] == Tileset.FLOOR) {
            world[xAxis][yAxis] = Tileset.FLOOR;
            xAxis = nx;
            yAxis = ny;
            world[xAxis][yAxis] = Tileset.AVATAR;
        }
    }


    private void topLeftNPCDialogue() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(45, 30, "White-Faced Varre: \"Oh yes... Tarnished, are we? Come to the Lands Between for the Elden Ring, hmm?\"");
        StdDraw.text(45, 27, "1) Where am I?");
        StdDraw.text(45, 25, "2) Who are you?");
        StdDraw.text(45, 23, "3) Goodbye \uD83D\uDC4B");
        StdDraw.show();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char choice = StdDraw.nextKeyTyped();
                if (choice == '1') {
                    showNPCResponse("Ahh, you’re in Limgrave—on the fringes of the Lands Between. Quite the place to die, no?");
                    break;
                } else if (choice == '2') {
                    showNPCResponse("Ahh, me? Merely a humble fellow—White-Faced Varre, at your service. A delight, truly.");
                    break;
                } else if (choice == '3') {
                    showNPCResponse("You will die nameless, without ceremony.");
                    break;
                }
            }
        }
    }

    private void topRightNPCDialogue() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(45, 30, "Blaidd, the Half-Wolf: \"State your business!\"");
        StdDraw.text(45, 27, "1) Why are you here?");
        StdDraw.text(45, 25, "2) What are you hunting?");
        StdDraw.text(45, 23, "3) What lies ahead?");
        StdDraw.show();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char choice = StdDraw.nextKeyTyped();
                if (choice == '1') {
                    showNPCResponse("I’ve been ordered to track a traitor… a cunning foe. My path leads through blood and shadow");
                    break;
                } else if (choice == '2') {
                    showNPCResponse("Darriwil. A beast cloaked in betrayal. He skulks like a dog with its tail tucked—I'll run him down soon enough");
                    break;
                } else if (choice == '3') {
                    showNPCResponse("Danger, and duty. But if you’ve the strength… perhaps we’ll tread the same path a while longer");
                    break;
                }
            }
        }
    }

    private void showNPCResponse(String message) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(45, 25, message);
        StdDraw.text(45, 20, "Press any button...");
        StdDraw.show();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                StdDraw.nextKeyTyped();
                break;
            }
        }
    }






    public static void menuCall(int cw, int ch) {
        StdDraw.setCanvasSize(cw, ch);
        StdDraw.setXscale(0, cw);
        StdDraw.setYscale(0, ch);

        boolean colorToggle = true;

        while (true) {
            // Alternate colors
            if (colorToggle) {
                StdDraw.clear(new Color(191, 246, 195));
            } else {
                StdDraw.clear(new Color(172, 225, 175));
            }

            StdDraw.setPenColor(Color.BLACK);
            StdDraw.text(cw / 2, ch - 200, "2D Elden Ring (WIP)");
            StdDraw.text(cw / 2, ch - 300, "(N) ew Game");
            StdDraw.text(cw / 2, ch - 350, "(L) oad Game");
            StdDraw.text(cw / 2, ch - 400, "(Q) uit Game");
            StdDraw.text(cw / 2, ch - 450, "(R) eplay Last Game");

            StdDraw.show();
            StdDraw.pause(400);

            // wait for press
            if (StdDraw.hasNextKeyTyped()) {
                break;
            }

            colorToggle = !colorToggle; // toggle color each loop
        }
    }

    public static String enterSeed(int cw, int ch) {
        StdDraw.setCanvasSize(cw, ch);
        StdDraw.setXscale(0, cw);
        StdDraw.setYscale(0, ch);
        String seedStr = "";

        while (true) {
            StdDraw.clear(Color.BLACK);
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.text(cw / 2, ch - 200, "Enter Seed (digits), then 'S' to start:");
            StdDraw.text(cw / 2, ch - 260, seedStr);
            StdDraw.show();

            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (Character.isDigit(c)) {
                    seedStr += c;
                } else if ((c == 's' || c == 'S')) {
                    if (seedStr.isEmpty()) {
                        continue; // if empty, ignore
                    } else {
                        break;    // if not empty, start
                    }
                }
            }
        }
        return seedStr;
    }

}
