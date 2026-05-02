import java.awt.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
public class RayCastingJumpingTest extends JFrame implements Runnable
{
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
            {1,1,1,1,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
        };
    private static final int[][] map2 =
        {
            {2,2,2,2,2,2,2,2,2,2,2,2,2,2,2},
            {2,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
            {2,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
            {2,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
            {2,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
            {2,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
            {2,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
            {2,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
            {2,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
            {2,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
            {2,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
            {2,2,2,2,0,0,0,0,0,0,0,0,0,0,2},
            {2,2,2,2,0,0,0,0,0,0,0,0,0,0,2},
            {2,2,2,2,0,0,0,0,0,0,0,0,0,0,2},
            {2,2,2,2,2,2,2,2,2,2,2,2,2,2,2}
        };
    private final int[][] floorMap;
    private final int[][] ceilingMap;
    private Vector player = new Vector(1.5, 1.5);
    private final Camera camera;
    private final Screen screen;

    public RayCastingJumpingTest()
    {
        //initial map and location
        camera = new Camera(player.getX(), player.getY(), 1, 0, 0, -0.66, true);//coordinates from topleft of map, facing down
        int mapWidth = map.length;
        int mapHeight = map[0].length;
        floorMap = new int[mapWidth][mapHeight];
        ceilingMap = new int[mapWidth][mapHeight];
        player = new Vector(1.5, 1.5);
        //what will be displayed to the user and each pixel of that image
        thread = new Thread(this);
        int width = 640;
        int height = 480;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        //list of the available textures to use
        ArrayList<Texture> textures = new ArrayList<>();
        textures.add(Texture.bricks);
        textures.add(Texture.gray);
        textures.add(Texture.black);
        //starting floor and ceiling locations
        for(int mapX = 0; mapX < mapWidth; mapX++)
        {
            for(int mapY = 0; mapY < mapHeight; mapY++)
            {
                floorMap[mapX][mapY] = 2;
                ceilingMap[mapX][mapY] = 2;
            }
        }
        //recognizes when key is pressed
        addKeyListener(camera);
        //send info to screen class to be drawn
        screen = new Screen(map, map2, floorMap, ceilingMap, mapWidth, mapHeight, textures, width, height);
        //setting up the window
        setSize(width, height);
        setResizable(false);
        setTitle("Ray Casting Jumping Test");
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
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        bs.show();
    }

    public void run()
    {
        //updates everything
        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60.0;//60 times per second
        double delta = 0;
        requestFocus();
        while(running)
        {
            long now = System.nanoTime();
            delta = delta + ((now-lastTime) / ns);
            lastTime = now;
            while(delta >= 1)//Make sure update is only happening 60 times a second
            {
                player = new Vector(camera.xPos, camera.yPos);
                //updates screen and camera
                camera.update(map, map2);
                double playerHeight = camera.playerHeight;
                screen.updateGame(camera, pixels, map, map2, floorMap, ceilingMap, playerHeight);
                delta--;
            }
            render();//displays to the screen unrestricted time
        }
    }

    public static void main(String [] args)
    {
        new RayCastingJumpingTest();
    }
}