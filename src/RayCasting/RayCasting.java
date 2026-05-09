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
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,0,0,0,0,0,0,0,0,0,1,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,0,0,0,0,0,0,0,0,0,1,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
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
    private static final int[][] map2 =
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
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
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
        
        // Set folder for development mode (when resources exist on filesystem)
        // This is only used as a fallback in readFile() method
        File testFolder = new File("../../resources/3DPoints");
        if(testFolder.exists()) {
            folder = testFolder;
        } else {
            testFolder = new File("resources/3DPoints");
            if(testFolder.exists()) {
                folder = testFolder;
            } else {
                // For app bundle, get JAR location
                String jarPath = RayCasting.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                File jarFile = new File(jarPath);
                File jarDir = jarFile.getParentFile();
                folder = new File(jarDir, "resources/3DPoints");
            }
        }
        
        // Try to read files from JAR first, filesystem second
        // If neither works, generate them
        files = new ArrayList<>();
        try {
            readFile("tree.txt");
            readFile("spiral.txt");
        } catch(Exception e) {
            // If reading failed, try to generate the files
            if(!folder.exists()) {
                folder.mkdirs();
            }
            File[] filelist = folder.listFiles();
            if(filelist == null)
                filelist = new File[0];
            for (File file : filelist)
                file.delete();
            new CreatePoints(folder.getPath());
            
            // Try reading again after generation
            files.clear();
            readFile("tree.txt");
            readFile("spiral.txt");
        }
        //Add structures made of points
        ArrayList<ArrayList<Vector3D>> points = new ArrayList<>();
        for(int i = 0; i < files.size(); i++)
            points.add(new ArrayList<>());
        int mapHeight = map[0].length;
        int mapWidth = map.length;
        for(int i = 0; i < 100; i++)
        {
            points.getFirst().add(new Vector3D(Math.random() * (mapWidth - 3) + 1.5, Math.random() * (mapHeight - 3) + 1.5, 0.5));
            double treeX = points.getFirst().get(i).getX();
            double treeY = points.getFirst().get(i).getY();
            int mapX = (int)treeX;
            int mapY = (int)treeY;
            
            // Check if tree is in center area
            if(treeX > mapWidth/3.0 - 0.5 && treeX < 2 * mapWidth/3.0 + 0.5 && treeY > mapHeight/3.0 - 0.5 && treeY < 2 * mapHeight/3.0 + 0.5)
            {
                points.getFirst().remove(i);
                i--;
            }
            // Check if tree is too close to any wall (within 0.5 units)
            else if(mapX >= 0 && mapX < mapWidth && mapY >= 0 && mapY < mapHeight)
            {
                boolean tooCloseToWall = false;
                // Check the tree's cell and all adjacent cells
                for(int dx = -1; dx <= 1; dx++)
                {
                    for(int dy = -1; dy <= 1; dy++)
                    {
                        int checkX = mapX + dx;
                        int checkY = mapY + dy;
                        if(checkX >= 0 && checkX < mapWidth && checkY >= 0 && checkY < mapHeight)
                        {
                            // If there's a wall in this cell, check distance
                            if(map[checkX][checkY] != 0 || map2[checkX][checkY] != 0)
                            {
                                // Calculate distance from tree to nearest edge of this wall cell
                                double nearestX = Math.max(checkX, Math.min(treeX, checkX + 1.0));
                                double nearestY = Math.max(checkY, Math.min(treeY, checkY + 1.0));
                                double distToWall = Math.sqrt((treeX - nearestX) * (treeX - nearestX) + 
                                                             (treeY - nearestY) * (treeY - nearestY));
                                if(distToWall < 0.5)
                                {
                                    tooCloseToWall = true;
                                    break;
                                }
                            }
                        }
                    }
                    if(tooCloseToWall) break;
                }
                if(tooCloseToWall)
                {
                    points.getFirst().remove(i);
                    i--;
                }
            }
            // Check if tree is too close to other trees
            else
            {
                boolean tooClose = false;
                for(int j = 0; j < points.getFirst().size() - 1; j++)
                {
                    if(points.getFirst().get(j).dist(points.getFirst().get(i)) <= 2)
                    {
                        tooClose = true;
                        break;
                    }
                }
                if(tooClose)
                {
                    points.getFirst().remove(i);
                    i--;
                }
            }
        }
        points.get(1).add(new Vector3D(mapWidth /2.0 - 2, mapHeight /2.0, 0.5));
        points.get(1).add(new Vector3D(mapWidth /2.0 + 7, mapHeight /2.0 - 1.2, 0.5));
        points.get(1).add(new Vector3D(mapWidth /2.0 + 7, mapHeight /2.0 - 0.6, 0.5));
        points.get(1).add(new Vector3D(mapWidth /2.0 + 7, mapHeight /2.0, 0.5));
        points.get(1).add(new Vector3D(mapWidth /2.0 + 7, mapHeight /2.0 + 0.6, 0.5));
        points.get(1).add(new Vector3D(mapWidth /2.0 + 7, mapHeight /2.0 + 1.2, 0.5));
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
                        map2[mapX][mapY] = 5;
                    }
                }
            }
        }
        //keyboard input
        addKeyListener(camera);
        //mouse input will be added to gamePanel after it's created
        //send info to screen class to be drawn
        screen = new Screen(map, map2, floorMap, ceilingMap, mapWidth, mapHeight, textures, files, width, height);
        screen.setPoints(points);
        screen.updateGame(camera, pixels, map, map2, floorMap, ceilingMap, frame);
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
        
        // Test if Robot works and show message if it doesn't
        boolean robotWorks = false;
        try {
            Robot testRobot = new Robot();
            testRobot.setAutoDelay(0);
            // Try a harmless mouse move to test permissions
            Point currentPos = MouseInfo.getPointerInfo().getLocation();
            testRobot.mouseMove(currentPos.x, currentPos.y);
            robotWorks = true;
        } catch (Exception e) {
            System.err.println("WARNING: Robot/Mouse control not available!");
            System.err.println("On macOS, you need to grant Accessibility permissions:");
            System.err.println("1. Open System Preferences > Security & Privacy > Privacy");
            System.err.println("2. Select 'Accessibility' from the left panel");
            System.err.println("3. Click the lock icon and authenticate");
            System.err.println("4. Add this application to the list and check the box");
            System.err.println("5. Restart the application");
            System.err.println("\nMouse look will not work properly without these permissions.");
        }
        
        //warp cursor off screen before showing window so it's never visible
        if (robotWorks) {
            try {
                Robot r = new Robot();
                r.mouseMove(-100, -100);
            } catch (AWTException ignored) {}
        }
        setVisible(true);
        //set warp target, then warp to center after a short delay
        //so the glass pane cursor is fully applied before the cursor enters the window
        if (robotWorks) {
            try {
                Point loc = getLocationOnScreen();
                camera.setWarpTarget(loc.x + width / 2, loc.y + height / 2);
                camera.warpCenter();
                final int tx = loc.x + width / 2;
                final int ty = loc.y + height / 2;
                new Thread(() -> {
                    try {
                        Thread.sleep(300);
                        Robot r = new Robot();
                        r.setAutoDelay(0);
                        r.mouseMove(tx, ty);
                    } catch (Exception e) {
                        System.err.println("Failed to warp cursor: " + e.getMessage());
                    }
                }).start();
            } catch (Exception e) {
                System.err.println("Failed to set up cursor warping: " + e.getMessage());
            }
        }
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
                camera.update(map, map2);
                //updating screen
                screen.updateGame(camera, pixels, map, map2, floorMap, ceilingMap, frame);
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
        String fileLoc = "resources/3DPoints/" + loc;
        java.io.InputStream is = null;
        
        // Method 1: Try to load from JAR resources (primary)
        is = getClass().getClassLoader().getResourceAsStream(fileLoc);
        
        // Method 2: Try filesystem paths (development fallback)
        if(is == null) {
            try {
                // Try relative path from source directory first
                File f = new File(folder, loc);
                if(f.exists()) {
                    is = new java.io.FileInputStream(f);
                } else {
                    // Try local resources directory
                    f = new File(fileLoc);
                    if(f.exists()) {
                        is = new java.io.FileInputStream(f);
                    } else {
                        // For app bundle, get JAR location and look relative to it
                        String jarPath = RayCasting.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                        File jarFile = new File(jarPath);
                        File jarDir = jarFile.getParentFile();
                        f = new File(jarDir, fileLoc);
                        if(f.exists()) {
                            is = new java.io.FileInputStream(f);
                        }
                    }
                }
            } catch(Exception ignored) {}
        }
        
        // Method 3: Extract from JAR as last resort (only if Methods 1 and 2 fail)
        if(is == null) {
            if(extractResourceFromJar(fileLoc)) {
                try {
                    is = new java.io.FileInputStream(new File(fileLoc));
                } catch(Exception ignored) {}
            }
        }
        
        // Read from InputStream
        if(is != null) {
            Scanner file = new Scanner(is);
            int ctr = 0;
            while(file.hasNextLine())
            {
                file.nextLine();
                ctr++;
            }
            file.close();
            
            // Re-open to read data
            is = getClass().getClassLoader().getResourceAsStream(fileLoc);
            if(is == null) {
                // Fallback to filesystem
                try {
                    File f = new File(folder, loc);
                    if(f.exists()) {
                        is = new java.io.FileInputStream(f);
                    } else {
                        f = new File(fileLoc);
                        if(f.exists()) {
                            is = new java.io.FileInputStream(f);
                        }
                    }
                } catch(Exception ignored) {}
            }
            
            if(is != null) {
                files.add(new PointsFile(ctr));
                file = new Scanner(is);
                ctr = 0;
                while(file.hasNextDouble())
                {
                    files.getLast().x[ctr] = file.nextDouble();
                    files.getLast().y[ctr] = file.nextDouble();
                    files.getLast().z[ctr] = file.nextDouble();
                    files.getLast().color[ctr] = file.nextInt();
                    ctr++;
                }
                file.close();
            }
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
    
    private boolean extractResourceFromJar(String resourcePath) {
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
                return true;
            }
        } catch(Exception ignored) {}
        return false;
    }

    public static void main(String[] args) throws IOException
    {
        new RayCasting();
    }
}
