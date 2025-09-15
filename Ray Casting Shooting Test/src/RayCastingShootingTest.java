import java.awt.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
public class RayCastingShootingTest extends JFrame implements Runnable
{
    private final int width = 640;
    private final int height = 480;
    private final Thread thread;
    private boolean running;
    private final BufferedImage image;
    private final int[] pixels;
    private static final int[][] map =
        {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
        };
    private final int[][] floorMap;
    private final int[][] ceilingMap;
    private final ArrayList<Sprite> sprites;
    private final ArrayList<Enemy> enemy;
    private boolean enemyDead;
    private boolean playerDead;
    private Vector player;
    private final Camera camera;
    private final Screen screen;
    public RayCastingShootingTest()
    {
        // initial map and location
        camera = new Camera(1.5, 1.5, 1, 0, 0, -0.66); //coordinates from topleft of map, facing down
        int mapWidth = map.length;
        int mapHeight = map[0].length;
        floorMap = new int[mapWidth][mapHeight];
        ceilingMap = new int[mapWidth][mapHeight];
        // list of all sprites
        sprites = new ArrayList<>();
        enemy = new ArrayList<>();
        player = new Vector(1.5, 1.5);
        playerDead = false;
        // what will be displayed to the user and each pixel of that image
        thread = new Thread(this);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        // list of the available textures to use
        ArrayList<Texture> textures = new ArrayList<>();
        textures.add(Texture.bricks);
        textures.add(Texture.gray);
        textures.add(Texture.black);
        // starting floor, ceiling, and finish location
        for(int mapX = 0; mapX < mapWidth; mapX++)
        {
            for(int mapY = 0; mapY < mapHeight; mapY++)
            {
                floorMap[mapX][mapY] = 2;
                ceilingMap[mapX][mapY] = 3;
            }
        }
        // enemy starting locations
        enemy.add(new Enemy(mapWidth - 1.5, mapHeight - 1.5, Texture.ball));
        enemyDead = false;
        // recognizes when key is pressed
        addKeyListener(camera);
        // send info to screen class to be drawn
        screen = new Screen(map, floorMap, ceilingMap, mapWidth, mapHeight, textures, width, height);
        screen.setEnemyPos(enemy.getFirst().xPos, enemy.getFirst().yPos);
        // setting up the window
        setSize(width, height);
        setResizable(false);
        setTitle("Ray Casting Shooting Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.black);
        setLocationRelativeTo(null);
        setVisible(true);
        start();
    }

    private synchronized void start()
    {
        //starts game
        running = true;
        thread.start();
    }

    private void render()
    {
        //draws the window
        BufferStrategy bs = getBufferStrategy();
        if(bs == null)
        {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        if(playerDead)
        {
            for(int x = 0; x < width; x++)
            {
                for(int y = 0; y < height; y++)
                {
                    pixels[y * width + x] = rgbNum(255, 0, 0);
                }
            }
        }
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        if(playerDead)
        {
            g.setFont(new Font("Verdana", Font.BOLD, 20));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(new Color(255, 255, 255));
            g.drawString("You Died", width/2 - fm.stringWidth("You Died")/2, height/2 - fm.getAscent()/2);
        }
        else
        {
            g.setColor(new Color(0, 0, 0));
            g.fillRect(width/2 - 2, height/2 - 11, 4, 8);
            g.fillRect(width/2 - 2, height/2 + 3, 4, 8);
            g.fillRect(width/2 - 11, height/2 - 2, 8, 4);
            g.fillRect(width/2 + 3, height/2 - 2, 8, 4);
        }
        bs.show();
    }

    public void run()
    {
        // updates everything
        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60.0;// 60 times per second
        double delta = 0;
        requestFocus();
        while(running)
        {
            long now = System.nanoTime();
            delta = delta + ((now-lastTime) / ns);
            lastTime = now;
            while(delta >= 1)// Make sure update is only happening 60 times a second
            {
                player = new Vector(camera.xPos, camera.yPos);
                if(!enemy.isEmpty())
                {
                    screen.setEnemyPos(enemy.getFirst().xPos, enemy.getFirst().yPos);
                    enemyDead = screen.isDead();
                }
                if(!enemyDead)
                {
                    Vector dir = Vector.sub(new Vector(enemy.getFirst().xPos, enemy.getFirst().yPos), new Vector(player.getX(), player.getY()));
                    if(dir.mag() < 0.5)
                    {
                        camera.setPos(1.5, 1.5);
                        camera.setDir(1, 0);
                        camera.setPlane(0, -0.66);
                        screen.setDead(true);
                        playerDead = true;
                    }
                    dir.normalize();
                    dir.mult(0.05);
                    if(map[(int) (enemy.getFirst().xPos - dir.getX())][(int) (enemy.getFirst().yPos - dir.getX())] == 0)
                    {
                        enemy.getFirst().setX(enemy.getFirst().xPos - dir.getX());
                        enemy.getFirst().setY(enemy.getFirst().yPos - dir.getY());
                    }
                }
                else if(!enemy.isEmpty())
                {
                    double randomX;
                    double randomY;
                    do
                    {
                        randomX = Math.random() * 11 + 2;
                        randomY = Math.random() * 11 + 2;
                    }while(Math.sqrt((randomX - player.getX()) * (randomX - player.getX()) + (randomY - player.getY()) * (randomY - player.getY())) < 5);
                    enemy.getFirst().setX(randomX);
                    enemy.getFirst().setY(randomY);
                    screen.setDead(false);
                }
                // creates all the sprites
                sprites.clear();
                for (Enemy value : enemy) {
                    sprites.add(new Sprite(value.xPos, value.yPos, value.texture));
                }
                screen.setSprites(sprites);
                // handles all the logic restricted time
                camera.update(map);
                screen.updateGame(camera, pixels, map, floorMap, ceilingMap);
                delta--;
            }
            render();// displays to the screen unrestricted time
        }
    }

    private int rgbNum(int r, int g, int b)
    {
        //gets rgb decimal value from rgb input
        return r * 65536 + g * 256 + b;
    }

    public static void main(String [] args)
    {
        new RayCastingShootingTest();
    }
}