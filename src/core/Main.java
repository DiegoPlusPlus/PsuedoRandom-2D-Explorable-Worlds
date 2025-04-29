package core;

import tileengine.TETile;
import tileengine.TERenderer;
import edu.princeton.cs.algs4.StdDraw;
import java.awt.Color;
import java.awt.Font;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {
    private static final int WIDTH = 90;
    private static final int HEIGHT = 48;
    private static final StringBuilder moves = new StringBuilder();

    public static void main(String[] args) {
        World.menuCall(1440, 800);

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (key == 'n') {
                    String seedString = World.enterSeed(1440, 800);
                    long seed = Long.parseLong(seedString);
                    saveGame(seed);             // Save seed for load
                    startGame(seed, "");  // RESET (0 moves)
                    break;
                } else if (key == 'q') {
                    System.exit(0);
                } else if (key == 'l') {
                    String[] data = loadGame();
                    long savedSeed = Long.parseLong(data[0]); // str to long (saved as str in temp.txt)
                    String pastMoves = data[1]; // Second saved in temp.txt
                    startGame(savedSeed, pastMoves);
                    break;
                }
                if (key == 'r') {
                    String[] data = loadGame();
                    long savedSeed = Long.parseLong(data[0]);
                    String pastMoves = data[1];
                    replayGame(savedSeed, pastMoves); // Same old but run move by move instead of instant execution
                    //startGame(savedSeed, pastMoves);
                    break;
                }
            }
        }
    }

    /*
    private static void startGame(long seed, String pastMoves) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT); // Keep const won't have to check width out-of-bounds

        World gen = new World(WIDTH, HEIGHT, seed);
        TETile[][] world = gen.worldCreate();


        // future saves include old + new moves
        moves.setLength(0);
        moves.append(pastMoves);

        while (true) {
            StdDraw.clear();
            ter.renderFrame(world);
            drawHUD(world);
            StdDraw.show();

            // No-move (Keep looping in case cursor moving or something)
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }

            // inside your key‐handling loop:
            char key = Character.toLowerCase(StdDraw.nextKeyTyped());

            if (key == ':') {
                moves.append(key);
                continue;   // go back to top of loop to grab the next key
            }

            int n = moves.length();
            if (n > 0 && moves.charAt(n-1) == ':' && key == 'q') {
                saveGame(seed); // Save and exit functionality
                System.exit(0);
            }

            // move player if 0-3 (jus keep rendering)
            if ("wasd".indexOf(key) != -1) {
                moves.append(key);
                gen.movePlayer(world, key);
            }
        }
    }

     */





    private static void startGame(long seed, String pastMoves) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT); // Keep const won't have to check width out-of-bounds

        World gen = new World(WIDTH, HEIGHT, seed);
        TETile[][] world = gen.worldCreate();

        // replay everything
        for (char m : pastMoves.toCharArray()) {
            gen.movePlayer(world, m);
        }

        // future saves include old + new moves
        moves.setLength(0);
        moves.append(pastMoves);

        while (true) {
            StdDraw.clear();
            ter.renderFrame(world);
            drawHUD(world);
            StdDraw.show();

            // No-move (Keep looping in case cursor moving or something)
            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }

            // inside your key‐handling loop:
            char key = Character.toLowerCase(StdDraw.nextKeyTyped());

            if (key == ':') {
                moves.append(key);
                continue;   // go back to top of loop to grab the next key
            }

            int n = moves.length();
            if (n > 0 && moves.charAt(n-1) == ':' && key == 'q') {
                saveGame(seed); // Save and exit functionality
                System.exit(0);
            }

            // move player if 0-3 (jus keep rendering)
            if ("wasd".indexOf(key) != -1) {
                moves.append(key);
                gen.movePlayer(world, key);
            }
        }
    }

    private static void drawHUD(TETile[][] world) {
        int mouseX = (int) StdDraw.mouseX(); // Cannot place double tile into TETile
        int mouseY = (int) StdDraw.mouseY();

        // if mouse in bounds (dont gotta check width (fix later))
        if (mouseY >= 0 && mouseY < world[0].length) {
            TETile tile = world[mouseX][mouseY];

            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.textLeft(WIDTH - 7, HEIGHT - 1,  tile.description());
        }
    }

    // Just create nnew file and output seed and moves
    private static void saveGame(long seed) {
        try (PrintWriter out = new PrintWriter("temp.txt")) {
            out.println(seed);
            out.println(moves.toString());
        } catch (IOException e) {
            throw new RuntimeException("file err: saveGame", e);
        }
    }

    private static String[] loadGame() {
        try (BufferedReader in = new BufferedReader(new FileReader("temp.txt"))) {
            String seedToLoad  = in.readLine();
            String movestoLoad = in.readLine();
            if (movestoLoad == null) {
                movestoLoad = "";
            }
            return new String[] { seedToLoad, movestoLoad };
        } catch (IOException e) {
            throw new RuntimeException("file err: loadGame", e);
        }
    }

    private static void replayGame(long seed, String pastMoves) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        World gen = new World(WIDTH, HEIGHT, seed);
        TETile[][] world = gen.worldCreate();

        // Replay moves visually, with a slight delay
        for (char m : pastMoves.toCharArray()) {
            gen.movePlayer(world, m);
            StdDraw.clear();
            ter.renderFrame(world);
            drawHUD(world);
            StdDraw.show();
            StdDraw.pause(200);
        }


        waitForContinue();
        // get back to game
        moves.setLength(0);
        moves.append(pastMoves);
        continuePlaying(seed, gen, world, ter);
    }

    private static void waitForContinue() {
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.text(8, HEIGHT - 1, "Press 'C' to continue...");
        StdDraw.show();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char pressed = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (pressed == 'c') {
                    break;
                }
            }
        }
    }

    private static void continuePlaying(long seed, World gen, TETile[][] world, TERenderer ter) {
        while (true) {
            StdDraw.clear();
            ter.renderFrame(world);
            drawHUD(world);
            StdDraw.show();

            if (!StdDraw.hasNextKeyTyped()) {
                continue;
            }
            char key = Character.toLowerCase(StdDraw.nextKeyTyped());

            // detect :q
            int n = moves.length();
            if (n > 0 && moves.charAt(n - 1) == ':' && key == 'q') {
                saveGame(seed);
                System.exit(0);
            }

            if ("wasd".indexOf(key) != -1 || key == ':') {
                moves.append(key);
                if ("wasd".indexOf(key) != -1) {
                    gen.movePlayer(world, key);
                }
            }
        }
    }

}
