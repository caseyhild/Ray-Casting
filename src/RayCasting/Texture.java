import java.io.*;
import java.util.*;

public class Texture {

    private static int frame = 0;

    // texture type
    private final String type;

    public int SIZE;
    public int[] pixels;

    // ===== STATIC TEXTURES =====
    public static Texture black = new Texture(64, "black");
    public static Texture coolpattern = new Texture(64, "coolpattern");
    public static Texture grass = new Texture(64, "grass");
    public static Texture gravel = new Texture(64, "gravel");
    public static Texture gray = new Texture(64, "gray");
    public static Texture flag = new Texture(64, "flag");
    public static Texture xor = new Texture(64, "xor");

    public static Texture circles = new Texture(64, "circles");
    public static Texture collisions = new Texture(64, "collisions");
    public static Texture wave = new Texture(64, "wave");

    public static Texture bricks = new Texture(64, "bricks");
    public static Texture dog = new Texture(64, "dog");
    public static Texture fractal = new Texture(64, "fractal");
    public static Texture map = new Texture(64, "map");
    public static Texture tiles = new Texture(64, "tiles");
    public static Texture waves = new Texture(64, "waves");

    // ===== COLLISION STATE =====
    private Vector2D[] position;
    private Vector2D[] velocity;
    private Vector2D[] acceleration;
    private int[] ballSize;
    private int[] color;
    private int numBalls;

    // ===== CONSTRUCTOR =====
    public Texture(int size, String texName) {
        SIZE = size;
        pixels = new int[SIZE * SIZE];
        type = texName;

        init();
        render(); // initial draw
    }

    // ===== INIT =====
    private void init() {
        if (type.equals("collisions")) {
            numBalls = 6;

            position = new Vector2D[]{
                new Vector2D(30, 20),
                new Vector2D(40, 20),
                new Vector2D(30, 40),
                new Vector2D(20, 40),
                new Vector2D(40, 30),
                new Vector2D(20, 30)
            };

            velocity = new Vector2D[]{
                new Vector2D(1, 1),
                new Vector2D(-1, 1),
                new Vector2D(0, -1),
                new Vector2D(1, -1),
                new Vector2D(-1, 0),
                new Vector2D(1, 0)
            };

            acceleration = new Vector2D[]{
                new Vector2D(0, 0),
                new Vector2D(0, 0),
                new Vector2D(0, 0),
                new Vector2D(0, 0),
                new Vector2D(0, 0),
                new Vector2D(0, 0)
            };

            ballSize = new int[]{5, 6, 7, 8, 9, 10};

            color = new int[]{
                rgbNum(255, 0, 0),
                rgbNum(0, 255, 0),
                rgbNum(0, 0, 255),
                rgbNum(255, 255, 0),
                rgbNum(255, 0, 255),
                rgbNum(0, 255, 255)
            };
        }
    }

    // ===== UPDATE =====
    public void update(int f) {
        frame = f;

        if (type.equals("collisions")) {
            updateCollisions();
        }

        render();
    }

    // ===== RENDER SWITCH =====
    private void render() {
        switch (type) {
            case "black" -> renderBlack();
            case "coolpattern" -> renderCoolPattern();
            case "grass" -> renderGrass();
            case "gravel" -> renderGravel();
            case "gray" -> renderGray();
            case "flag" -> renderFlag();
            case "wave" -> renderWave();
            case "xor" -> renderXor();
            case "circles" -> renderCircles();
            case "collisions" -> renderCollisions();
            default -> loadFromFile(type);
        }
    }

    // ===== COLLISIONS =====
    private void updateCollisions() {
        for (int i = 0; i < numBalls; i++) {
            position[i].add(velocity[i]);
            velocity[i].add(acceleration[i]);
        }

        checkBoundaries();

        for (int i = 0; i < numBalls; i++) {
            for (int j = i + 1; j < numBalls; j++) {
                checkCollision(i, j);
            }
        }
    }

    private void checkCollision(int a, int b) {
        double dx = position[a].getX() - position[b].getX();
        double dy = position[a].getY() - position[b].getY();

        double dist = Math.sqrt(dx * dx + dy * dy);

        double minDist = (ballSize[a] / 2.0 + ballSize[b] / 2.0);

        if (dist <= minDist && dist > 0.00001) {

            doCollision(a, b);
            separate(a, b, dist);
        }
    }

    private void separate(int a, int b, double dist) {
        double minDist = (ballSize[a] / 2.0 + ballSize[b] / 2.0);
        double overlap = minDist - dist;

        if (overlap <= 0 || dist == 0) return;

        double nx = (position[a].getX() - position[b].getX()) / dist;
        double ny = (position[a].getY() - position[b].getY()) / dist;

        double push = overlap / 2.0;

        position[a].setX(position[a].getX() + nx * push);
        position[a].setY(position[a].getY() + ny * push);

        position[b].setX(position[b].getX() - nx * push);
        position[b].setY(position[b].getY() - ny * push);
    }

    private void doCollision(int a, int b) {
        double dx = position[b].getX() - position[a].getX();
        double dy = position[b].getY() - position[a].getY();

        double angle = Math.atan2(dy, dx);

        double v1x = velocity[a].getX();
        double v1y = velocity[a].getY();
        double v2x = velocity[b].getX();
        double v2y = velocity[b].getY();

        double v1 = Math.hypot(v1x, v1y);
        double v2 = Math.hypot(v2x, v2y);

        double angle1 = Math.atan2(v1y, v1x);
        double angle2 = Math.atan2(v2y, v2x);

        double v1xr = v1 * Math.cos(angle1 - angle);
        double v1yr = v1 * Math.sin(angle1 - angle);
        double v2xr = v2 * Math.cos(angle2 - angle);
        double v2yr = v2 * Math.sin(angle2 - angle);

        double m1 = ballSize[a];
        double m2 = ballSize[b];

        double v1xrf = ((m1 - m2) * v1xr + 2 * m2 * v2xr) / (m1 + m2);
        double v2xrf = ((2 * m1 * v1xr) + (m2 - m1) * v2xr) / (m1 + m2);

        double v1xf = Math.cos(angle) * v1xrf + Math.cos(angle + Math.PI / 2) * v1yr;
        double v1yf = Math.sin(angle) * v1xrf + Math.sin(angle + Math.PI / 2) * v1yr;

        double v2xf = Math.cos(angle) * v2xrf + Math.cos(angle + Math.PI / 2) * v2yr;
        double v2yf = Math.sin(angle) * v2xrf + Math.sin(angle + Math.PI / 2) * v2yr;

        velocity[a].setX(v1xf);
        velocity[a].setY(v1yf);
        velocity[b].setX(v2xf);
        velocity[b].setY(v2yf);
    }

    private void checkBoundaries() {
        int borderSize = 3;
        for (int i = 0; i < numBalls; i++) {

            double r = ballSize[i] / 2.0;

            if (position[i].getX() < borderSize + r) {
                position[i].setX(borderSize + r);
                velocity[i].setX(-velocity[i].getX());
            }

            if (position[i].getX() > SIZE - borderSize - r) {
                position[i].setX(SIZE - borderSize - r);
                velocity[i].setX(-velocity[i].getX());
            }

            if (position[i].getY() < borderSize + r) {
                position[i].setY(borderSize + r);
                velocity[i].setY(-velocity[i].getY());
            }

            if (position[i].getY() > SIZE - borderSize - r) {
                position[i].setY(SIZE - borderSize - r);
                velocity[i].setY(-velocity[i].getY());
            }
        }
    }

    private void renderCollisions() {
        Arrays.fill(pixels, rgbNum(0, 0, 0));

        // border (unchanged)
        for (int i = 0; i < SIZE * SIZE; i++) {
            int x = i % SIZE;
            int y = i / SIZE;

            if (x <= 2 || x >= SIZE - 3 || y <= 2 || y >= SIZE - 3) {
                pixels[i] = rgbNum(64, 64, 64);
            }
        }

        for (int i = 0; i < numBalls; i++) {

            double cx = position[i].getX();
            double cy = position[i].getY();

            double r = ballSize[i] / 2.0;
            double r2 = r * r;

            for (int y = -(int)r; y <= (int)r; y++) {
                for (int x = -(int)r; x <= (int)r; x++) {

                    int px = (int)(cx + x);
                    int py = (int)(cy + y);

                    if (px < 0 || px >= SIZE || py < 0 || py >= SIZE)
                        continue;

                    double dist = Math.sqrt(x * x + y * y);

                    if (dist <= r) {

                        // ===== SOFT CIRCLE LIKE renderCircles STYLE =====

                        double t = dist / r; // 0 center → 1 edge

                        // smooth falloff (key improvement)
                        double falloff = 1.0 - Math.pow(t, 1.8);

                        // slight edge tightening (removes spikes)
                        falloff = Math.max(0, falloff);

                        int base = color[i];

                        int rr = (int)(getR(base) * falloff);
                        int gg = (int)(getG(base) * falloff);
                        int bb = (int)(getB(base) * falloff);

                        pixels[py * SIZE + px] = rgbNum(rr, gg, bb);
                    }
                }
            }
        }
    }

    // ===== SIMPLE RENDERERS (examples) =====
    private void renderBlack() {
        Arrays.fill(pixels, rgbNum(0, 0, 0));
    }

    private void renderCoolPattern() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int color = 0;
                if (i < SIZE / 2.0 && j < SIZE / 2.0)
                    color = i * j;
                else if (i < SIZE / 2.0)
                    color = i * (SIZE - j);
                else if (j < SIZE / 2.0)
                    color = (SIZE - i) * j;
                else
                    color = (SIZE - i) * (SIZE - j);
                color *= 276;
                pixels[i * SIZE + j] = color;
            }
        }
    }

    private void renderGrass() {
        int base = rgbNum(0, 128, 0);
        for (int i = 0; i < pixels.length; i++) {
            double r = Math.random();
            pixels[i] = rgbNum((int)(r * getR(base)), (int)(r * getG(base)), (int)(r * getB(base)));
        }
    }

    private void renderGravel() {
        for (int i = 0; i < pixels.length; i++) {
            int s = (int)(Math.random() * 64);
            pixels[i] = rgbNum(s, s, s);
        }
    }

    private void renderGray() {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = (i % SIZE <= 1 || i % SIZE >= SIZE - 2 ||
                         i / SIZE <= 1 || i / SIZE >= SIZE - 2)
                    ? rgbNum(96,96,96)
                    : rgbNum(64,64,64);
        }
    }

    private void renderFlag() {
        for (int i = 0; i < SIZE * SIZE; i++) {
            if (i / SIZE % 10 < 4)
                pixels[i] = rgbNum(255, 0, 0);
            else
                pixels[i] = rgbNum(255, 255, 255);
        }
        for (int i = 0; i < SIZE * (SIZE / 2.0 + 2); i++) {
            if (i % SIZE < SIZE / 2.0 - 5) {
                if (i % SIZE % 4 == 3 && i / SIZE % 6 == 4)
                    pixels[i] = rgbNum(255, 255, 255);
                else if (i % SIZE % 4 == 1 && i / SIZE % 6 == 1 && (i > SIZE * 2 && i < SIZE * (SIZE / 2.0 - 2) && i % SIZE > 2 && i % SIZE < SIZE / 2.0 - 7))
                    pixels[i] = rgbNum(255, 255, 255);
                else
                    pixels[i] = rgbNum(0, 0, 255);
            }
        }
    }

    private void renderXor() {
        for (int i = 0; i < pixels.length; i++) {
            int c = (i % SIZE * 4) ^ (i / SIZE * 4);
            pixels[i] = rgbNum(c, c, c);
        }
    }

    private void renderCircles() {
        int radius = 3;
        int num = 6;
        int[] pointsx = new int[num];
        int[] pointsy = new int[num];
        int[] startDeg = new int[num];
        int[] color = new int[num];
        color[0] = rgbNum(255, 0, 0);
        color[1] = rgbNum(255, 128, 0);
        color[2] = rgbNum(255, 255, 0);
        color[3] = rgbNum(0, 255, 0);
        color[4] = rgbNum(0, 0, 255);
        color[5] = rgbNum(192, 0, 192);
        for (int i = 0; i < SIZE * SIZE; i++) {
            if (i % SIZE <= 2 || i % SIZE >= SIZE - 3 || i / SIZE <= 2 || i / SIZE >= SIZE - 3)
                pixels[i] = rgbNum(64, 64, 64);
            else
                pixels[i] = rgbNum(0, 0, 0);
        }
        for (int i = 0; i < num; i++) {
            startDeg[i] = (int) (360.0 / num * i);
            pointsx[i] = (int) (SIZE / 2.0 - SIZE / 4 * Math.cos(Math.toRadians(0.1 * frame + startDeg[i])));
            pointsy[i] = (int) (SIZE / 2.0 - SIZE / 4 * Math.sin(Math.toRadians(0.1 * frame + startDeg[i])));
            for (int y = -radius + 1; y < radius; y++) {
                for (int x = -radius + 1; x < radius; x++) {
                    if (pointsx[i] + x < SIZE && pointsx[i] - x >= 0 && Math.hypot(x, y) <= Math.hypot(radius - 1, radius - 2))
                        pixels[(pointsy[i] + y) * SIZE + (pointsx[i] + x)] = color[i];
                }
            }
        }
    }

    private void renderWave() {
        int space = 1;
        int border = 0;
        int num = (SIZE - 2 * border) / space;
        int[] pointsx = new int[num];
        int[] pointsy = new int[num];
        int[] startDeg = new int[num];
        for (int i = 0; i < SIZE * SIZE; i++) {
            pixels[i] = rgbNum(255, 255, 255);
        }
        for (int index = 0; index < num; index++) {
            startDeg[index] = (int) (360.0 * (index * space + border) / SIZE);
            pointsx[index] = index * space + border;
            pointsy[index] = (int) (SIZE / 2.0 - SIZE / 4 * Math.sin(Math.toRadians(frame * 0.1 + startDeg[index])));
            pointsy[index] = Math.min(pointsy[index], 3 * SIZE / 4 - 1);
            for (int i = -2; i <= 3 * SIZE / 4 - 1; i++) {
                if (i <= 2)
                    pixels[(pointsy[index] + i) * SIZE + pointsx[index]] = rgbNum(0, 0, 255);
                else
                    pixels[Math.min(pointsy[index] + i, SIZE - 1) * SIZE + pointsx[index]] = rgbNum(0, 0, 0);
            }
        }
    }

    // ===== FILE LOADER =====
    private void loadFromFile(String name) {
        try {
            // Try relative path from source directory first
            File file = new File("../../resources/SavedTextures/" + name + ".txt");
            if(!file.exists()) {
                // Try local resources directory when running from JAR
                file = new File("resources/SavedTextures/" + name + ".txt");
                if(!file.exists()) {
                    // Try to extract from JAR resources
                    extractResourceFromJar("resources/SavedTextures/" + name + ".txt");
                    file = new File("resources/SavedTextures/" + name + ".txt");
                }
            }
            Scanner in = new Scanner(file);
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = in.nextInt();
            }
            in.close();
        } catch (IOException ignored) {}
    }
    
    private void extractResourceFromJar(String resourcePath) {
        try {
            // Try with resources/ prefix first
            java.io.InputStream in = getClass().getClassLoader().getResourceAsStream("resources/" + resourcePath);
            if(in == null) {
                // Try without prefix
                in = getClass().getClassLoader().getResourceAsStream(resourcePath);
            }
            if(in != null) {
                java.io.File outFile = new java.io.File(resourcePath);
                outFile.getParentFile().mkdirs();
                java.io.FileOutputStream out = new java.io.FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                in.close();
                out.close();
            }
        } catch(Exception ignored) {}
    }

    // ===== UTILS =====
    private int rgbNum(int r, int g, int b) {
        return r * 65536 + g * 256 + b;
    }

    private int getR(int c) { return c / 65536; }
    private int getG(int c) { return (c % 65536) / 256; }
    private int getB(int c) { return c % 256; }
}