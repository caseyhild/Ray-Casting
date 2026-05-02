import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import java.io.*;
public class RayCastingMoreTests extends JFrame implements Runnable, KeyListener
{
    private final int width;
    private final int height;
    private int frame;
    private final Thread thread;
    private boolean running;
    private final BufferedImage image;
    private final int[] pixels;
    private static final int[][] map =
        {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
        };
    private final int[][] floorMap;
    private final int[][] ceilingMap;
    private final ArrayList<Texture> textures;
    private final File folder;
    private final ArrayList<PointsFile> files;
    private final Camera camera;
    private final Screen screen;
    private boolean keyPressed;
    private boolean keyReleased;
    private boolean keyTyped;
    private KeyEvent key;
    private KeyEvent oldKey;

    public RayCastingMoreTests() throws IOException
    {
        //set size of screen
        width = 640;
        height = 480;
        //set starting frame
        frame = 0;
        //delete all existing files
        folder = new File("Ray Casting More Tests/3DPoints");
        File[] filelist = folder.listFiles();
        if(filelist == null)
            filelist = new File[0];
        for (File file : filelist)
            file.delete();
        //Create 3D Points files
        new CreatePoints("Ray Casting More Tests/3DPoints");
        files = new ArrayList<>();
        readFile("tree.txt");
        readFile("spiral.txt");
        //Add structures made of points
        ArrayList<ArrayList<Vector3D>> points = new ArrayList<>();
        for(int i = 0; i < files.size(); i++)
            points.add(new ArrayList<>());
        int mapHeight = map[0].length;
        int mapWidth = map.length;
        for(int i = 0; i < 100; i++)
        {
            points.getFirst().add(new Vector3D(Math.random() * (mapWidth - 3) + 1.5, Math.random() * (mapHeight - 3) + 1.5, 0.5));
            if(points.getFirst().get(i).getX() > mapWidth/3.0 - 0.5 && points.getFirst().get(i).getX() < 2 * mapWidth/3.0 + 0.5 && points.getFirst().get(i).getY() > mapHeight/3.0 - 0.5 && points.getFirst().get(i).getY() < 2 * mapHeight/3.0 + 0.5)
            {
                points.getFirst().remove(i);
                i--;
            }
            else
            {
                for(int j = 0; j < points.getFirst().size() - 1; j++)
                {
                    if(points.getFirst().get(j).dist(points.getFirst().get(i)) <= 2)
                    {
                        points.getFirst().remove(i);
                        i--;
                    }
                }
            }
        }
        points.get(1).add(new Vector3D(mapWidth /2.0 + 2, mapHeight /2.0, 0.5));
        //initial map and location
        camera = new Camera(mapWidth/2 + 0.5, mapHeight/2 + 0.5, 1, 0, 0, -0.66);//coordinates from topleft of map, facing down
        floorMap = new int[mapWidth][mapHeight];
        ceilingMap = new int[mapWidth][mapHeight];
        //what will be displayed to the user and each pixel of that image
        thread = new Thread(this);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        //list of the available textures to use
        textures = new ArrayList<>();
        textures.add(Texture.bricks);
        textures.add(Texture.gray);
        textures.add(Texture.grass);
        textures.add(Texture.black);
        //starting floor, ceiling, and finish location
        for(int mapX = 0; mapX < mapWidth; mapX++)
        {
            for(int mapY = 0; mapY < mapHeight; mapY++)
            {
                if(mapX >= mapWidth /3 && mapX < 2 * mapWidth /3 && mapY >= mapHeight /3 && mapY < 2 * mapHeight /3)
                {
                    floorMap[mapX][mapY] = 2;
                    ceilingMap[mapX][mapY] = 2;
                }
                else
                {
                    floorMap[mapX][mapY] = 3;
                    ceilingMap[mapX][mapY] = 4;
                    if(mapX == 0 || mapX == map.length - 1 || mapY == 0 || mapY == map.length - 1) {
                        map[mapX][mapY] = 4;
                    }
                }
            }
        }
        //keyboard input
        addKeyListener(camera);
        //mouse input
        addMouseListener(camera);
        addMouseMotionListener(camera);
        //send info to screen class to be drawn
        screen = new Screen(map, floorMap, ceilingMap, mapWidth, mapHeight, textures, files, width, height);
        screen.setPoints(points);
        screen.updateGame(camera, pixels, map, floorMap, ceilingMap, frame);
        //setting up the window
        setSize(width, height);
        setResizable(false);
        setTitle("Ray Casting More Tests");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.gray);
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

    private void render() throws IOException
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
        g.setColor(new Color(0, 0, 0));
        g.fillRect(width/2 - 2, height/2 - 11, 4, 8);
        g.fillRect(width/2 - 2, height/2 + 3, 4, 8);
        g.fillRect(width/2 - 11, height/2 - 2, 8, 4);
        g.fillRect(width/2 + 3, height/2 - 2, 8, 4);
        if(keyReleased)
            keyReleased = false;
        if(keyTyped)
            keyTyped = false;
        bs.show();
    }

    public void run()
    {
        //main game loop
        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60.0;//60 times per second
        double delta = 0;
        requestFocus();
        while(running)
        {
            //updates time
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            frame++;
            while(delta >= 1)//Make sure update is only happening 60 times a second
            {
                //updates game
                //updating textures
                ArrayList<Texture> texturesCopy = new ArrayList<>(textures);
                textures.clear();
                textures.addAll(texturesCopy);
                //updating camera
                camera.update(map);
                //updating screen
                screen.updateGame(camera, pixels, map, floorMap, ceilingMap, frame);
                delta--;
            }
            try
            {
                // Hide the cursor
                BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                        cursorImg, new Point(0, 0), "blank cursor");
                this.getContentPane().setCursor(blankCursor);

                render();//displays to the screen unrestricted time
            }
            catch(IOException ignored)
            {

            }
        }
    }

    public void readFile(String loc) throws IOException
    {
        Scanner file = new Scanner(new File(folder + "/" + loc));
        int ctr = 0;
        while(file.hasNextLine())
        {
            file.nextLine();
            ctr++;
        }
        files.add(new PointsFile(ctr));
        file = new Scanner(new File(folder + "/" + loc));
        ctr = 0;
        while(file.hasNextDouble())
        {
            files.getLast().x[ctr] = file.nextDouble();
            files.getLast().y[ctr] = file.nextDouble();
            files.getLast().z[ctr] = file.nextDouble();
            files.getLast().color[ctr] = file.nextInt();
            ctr++;
        }
    }

    public void sort(ArrayList<Integer> list, ArrayList<String> names, int first, int last)
    {
        int g = first, h = last;
        int midIndex = (first + last) / 2;
        int dividingValue = list.get(midIndex);
        do
        {
            while(list.get(g) < dividingValue)
            {
                g++;
            }
            while(list.get(h) > dividingValue)
            {
                h--;
            }
            if(g <= h)
            {
                int temp = list.get(g);
                list.set(g, list.get(h));
                list.set(h, temp);
                String tempName = names.get(g);
                names.set(g, names.get(h));
                names.set(h, tempName);
                g++;
                h--;
            }
        }while(g < h);
        if(h > first)
            sort(list, names, first, h);
        if(g < last)
            sort(list, names, g, last);
    }

    public void keyPressed(KeyEvent key)
    {
        keyPressed = !keyTyped;
        this.key = key;
        if(oldKey == null)
            oldKey = key;
    }

    public void keyReleased(KeyEvent key)
    {
        keyPressed = false;
        keyReleased = true;
        this.key = key;
        oldKey.setKeyChar(' ');
    }

    public void keyTyped(KeyEvent key)
    {
        keyTyped = true;
    }

    private int rgbNum(int r, int g, int b)
    {
        //gets rgb decimal value from rgb input
        return r * 65536 + g * 256 + b;
    }

    private int getR(int color)
    {
        //gets r value from rgb decimal input
        return color/65536;
    }

    private int getG(int color)
    {
        //gets g value from rgb decimal input
        return color % 65536/256;
    }

    private int getB(int color)
    {
        //gets b value from rgb decimal input
        return color % 65536 % 256;
    }

    public static void main(String[] args) throws IOException
    {
        new RayCastingMoreTests();
    }
}