import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
public class RayCasting extends JFrame implements Runnable, KeyListener
{
    private final int width;
    private final int height;
    private int frame;
    private final Thread thread;
    private boolean running;
    private final BufferedImage image;
    private final int[] pixels;
    private JPanel gamePanel;
    private Cursor blankCursor;
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
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,16,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,15,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,13,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,12,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,11,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,10,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,9,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
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

    public RayCasting() throws IOException
    {
        //set size of screen
        width = 800;
        height = 600;
        //set starting frame
        frame = 0;
        //Create 3D Points files only if they don't already exist
        folder = new File("../3DPoints");
        if(!new File(folder, "tree.txt").exists() || !new File(folder, "spiral.txt").exists())
        {
            File[] filelist = folder.listFiles();
            if(filelist == null)
                filelist = new File[0];
            for (File file : filelist)
                file.delete();
            new CreatePoints("../3DPoints");
        }
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
        camera = new Camera(mapWidth/2 + 0.5, mapHeight/2 + 0.5, 1, 0, 0, -0.66, width, height);//coordinates from topleft of map, facing down
        floorMap = new int[mapWidth][mapHeight];
        ceilingMap = new int[mapWidth][mapHeight];
        //what will be displayed to the user and each pixel of that image
        thread = new Thread(this);
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
        //list of the available textures to use
        textures = new ArrayList<>();
        textures.add(Texture.bricks);
        textures.add(Texture.xor);
        textures.add(Texture.gray);
        textures.add(Texture.grass);
        textures.add(Texture.black);

        textures.add(Texture.gravel);
        textures.add(Texture.coolpattern);
        textures.add(Texture.fractal);
        textures.add(Texture.tiles);
        textures.add(Texture.waves);
        textures.add(Texture.flag);
        textures.add(Texture.dog);
        textures.add(Texture.map);
        textures.add(Texture.wave);
        textures.add(Texture.circles);
        textures.add(Texture.collisions);
        //starting floor, ceiling, and finish location
        for(int mapX = 0; mapX < mapWidth; mapX++)
        {
            for(int mapY = 0; mapY < mapHeight; mapY++)
            {
                if(mapX >= mapWidth /3 && mapX < 2 * mapWidth /3 && mapY >= mapHeight /3 && mapY < 2 * mapHeight /3)
                {
                    floorMap[mapX][mapY] = 2;
                    ceilingMap[mapX][mapY] = 3;
                }
                else
                {
                    floorMap[mapX][mapY] = 4;
                    ceilingMap[mapX][mapY] = 5;
                    if(mapX == 0 || mapX == map.length - 1 || mapY == 0 || mapY == map.length - 1) {
                        map[mapX][mapY] = 5;
                    }
                }
            }
        }
        //keyboard input
        addKeyListener(camera);
        //mouse input will be added to gamePanel after it's created
        //send info to screen class to be drawn
        screen = new Screen(map, floorMap, ceilingMap, mapWidth, mapHeight, textures, files, width, height);
        screen.setPoints(points);
        screen.updateGame(camera, pixels, map, floorMap, ceilingMap, frame);
        //setting up the window
        setResizable(false);
        setTitle("Ray Casting");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.gray);
        //hide the cursor using glass pane (most reliable on macOS)
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image cursorImage = toolkit.createImage(new byte[0]);
        blankCursor = toolkit.createCustomCursor(cursorImage, new Point(0, 0), "hidden");
        JPanel glassPane = (JPanel) getGlassPane();
        glassPane.setVisible(true);
        glassPane.setCursor(blankCursor);
        setCursor(blankCursor);
        getContentPane().setCursor(blankCursor);
        //reapply on every mouse movement (macOS-proof)
        glassPane.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) { setCursor(blankCursor); }
            @Override public void mouseDragged(java.awt.event.MouseEvent e) { setCursor(blankCursor); }
        });
        glassPane.addMouseListener(camera);
        glassPane.addMouseMotionListener(camera);
        camera.setCursorHider(() -> {
            setCursor(blankCursor);
            getContentPane().setCursor(blankCursor);
            glassPane.setCursor(blankCursor);
        });
        //create panel for rendering
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, width, height, null);
            }
        };
        gamePanel.setPreferredSize(new Dimension(width, height));
        gamePanel.setCursor(blankCursor);
        setCursor(blankCursor);
        gamePanel.addMouseListener(camera);
        gamePanel.addMouseMotionListener(camera);
        setContentPane(gamePanel);
        getContentPane().setCursor(blankCursor);
        pack();
        setLocationRelativeTo(null);
        //warp cursor off screen before showing window so it's never visible
        try {
            Robot r = new Robot();
            r.mouseMove(-100, -100);
        } catch (AWTException ignored) {}
        setVisible(true);
        //set warp target, then warp to center after a short delay
        //so the glass pane cursor is fully applied before the cursor enters the window
        try {
            Point loc = getLocationOnScreen();
            camera.setWarpTarget(loc.x + width / 2, loc.y + height / 2);
            camera.warpCenter();
            final int tx = loc.x + width / 2;
            final int ty = loc.y + height / 2;
            new Thread(() -> {
                try {
                    Thread.sleep(300);
                    new Robot().mouseMove(tx, ty);
                } catch (Exception ignored) {}
            }).start();
        } catch (Exception ignored) {}
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
            createBufferStrategy(2);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        Insets insets = getInsets();
        g.drawImage(image, insets.left, insets.top, image.getWidth(), image.getHeight(), null);
        g.dispose();
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
                Texture.circles.update(frame);
                Texture.collisions.update(frame);
                Texture.wave.update(frame);
                //updating camera
                camera.update(map);
                //updating screen
                screen.updateGame(camera, pixels, map, floorMap, ceilingMap, frame);
                delta--;
            }
            try
            {
                // Hide the cursor
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
        new RayCasting();
    }
}
