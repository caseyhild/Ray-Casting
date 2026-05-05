import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.swing.*;

public class TextureEditor extends JPanel implements ActionListener, MouseListener, MouseMotionListener
{
    Timer tm = new Timer(1, this);
    private static final int width = 900;
    private static final int height = 512;
    private final int size = 64;
    private final int cellSize = height / size;  // 512 / 64 = 8 pixels per cell
    private final int[][] pixels = new int[size][size];
    private int colorR = -1;
    private int colorG = -1;
    private int colorB = -1;
    private final int colorPickerX = 522;
    private final int colorPickerY = 10;
    private int colorX = colorPickerX;
    private int colorY = colorPickerY;
    private int drawingSize = 1;
    private int rectWidth = 0;
    private int rectHeight = 0;
    private String drawingMode = "Draw";
    private int rectWidth2 = 0;
    private int rectHeight2 = 0;
    private int x1 = -1;
    private int y1 = -1;
    private int x2 = -1;
    private int y2 = -1;
    private int startCellX = -1;  // Track the original starting cell
    private int startCellY = -1;
    private boolean drawCopyText = false;
    private int copyTextWidth = 0;
    private int copyTextHeight = 0;
    private int[][] copy;
    private int pasteWidth = 0;
    private int pasteHeight = 0;
    private int pasteX = 0;
    private int pasteY = 0;
    private final int numRecentColors = 5;
    private final int[] recentColors = new int[numRecentColors];
    private boolean clickedRecent = false;
    private int mouseX;
    private int mouseY;
    private static final int MOUSE_Y_OFFSET = -2;  // Offset to account for panel border/insets
    private JTextField rField;
    private JTextField gField;
    private JTextField bField;

    public TextureEditor()
    {
        addMouseListener(this);
        addMouseMotionListener(this);
        loadTexture(Texture.bricks);
        Arrays.fill(recentColors, -1);
        copy = new int[0][0];
        
        // Create RGB text fields
        setLayout(null);
        
        rField = new JTextField("0");
        rField.setBounds(835, 20, 50, 20);
        rField.addActionListener(e -> updateColorFromFields());
        add(rField);
        
        gField = new JTextField("0");
        gField.setBounds(835, 45, 50, 20);
        gField.addActionListener(e -> updateColorFromFields());
        add(gField);
        
        bField = new JTextField("0");
        bField.setBounds(835, 70, 50, 20);
        bField.addActionListener(e -> updateColorFromFields());
        add(bField);
    }
    
    private void loadTexture(Texture t)
    {
        for(int i = 0; i < size * size; i++)
        {
            pixels[i % size][i/size] = t.pixels[i];
        }
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.setColor(new Color(192, 192, 192));
        g.fillRect(0, 0, width, height);
        for(int x = 0; x < size; x++)
        {
            for(int y = 0; y < size; y++)
            {
                int pixelR = Math.max(0, Math.min(255, getR(pixels[x][y])));
                int pixelG = Math.max(0, Math.min(255, getG(pixels[x][y])));
                int pixelB = Math.max(0, Math.min(255, getB(pixels[x][y])));
                g.setColor(new Color(pixelR, pixelG, pixelB));
                g.fillRect(x * height/size, y * height/size, height/size, height/size);
                g.setColor(new Color(0, 0, 0));
                g.drawRect(x * height/size, y * height/size, height/size, height/size);
            }
        }
        for(int x = 0; x < 224; x++)
        {
            int lineR;
            int lineG;
            int lineB;
            for(int y = 0; y < 192; y++)
            {
                if(x < 32)
                {
                    lineR = 255;
                    lineG = x % 32 * 8;
                    lineB = 0;
                }
                else if(x < 64)
                {
                    lineR = 255 - x % 32 * 8;
                    lineG = 255;
                    lineB = 0;
                }
                else if(x < 96)
                {
                    lineR = 0;
                    lineG = 255;
                    lineB = x % 32 * 8;
                }
                else if(x < 128)
                {
                    lineR = 0;
                    lineG = 255 - x % 32 * 8;
                    lineB = 255;
                }
                else if(x < 160)
                {
                    lineR = x % 32 * 8;
                    lineG = 0;
                    lineB = 255;
                }
                else if(x < 192)
                {
                    lineR = 255;
                    lineG = 0;
                    lineB = 255 - x % 32 * 8;
                }
                else
                {
                    lineR = 128;
                    lineG = 128;
                    lineB = 128;
                }
                if(y < 96)
                {
                    lineR = (int) (lineR * y/96.0);
                    lineG = (int) (lineG * y/96.0);
                    lineB = (int) (lineB * y/96.0);
                }
                else
                {
                    lineR = 255 - (int) ((255 - lineR) * (192 - y)/96.0);
                    lineG = 255 - (int) ((255 - lineG) * (192 - y)/96.0);
                    lineB = 255 - (int) ((255 - lineB) * (192 - y)/96.0);
                }
                lineR = Math.max(lineR, 0);
                lineG = Math.max(lineG, 0);
                lineB = Math.max(lineB, 0);
                lineR = Math.min(lineR, 255);
                lineG = Math.min(lineG, 255);
                lineB = Math.min(lineB, 255);
                g.setColor(new Color(lineR, lineG, lineB));
                g.drawLine(x + colorPickerX, y + colorPickerY, x + colorPickerX, y + colorPickerY);
            }
        }
        g.setColor(new Color(0, 0, 0));
        g.fillRect(colorPickerX - 5, colorPickerY - 5, 233, 5);
        g.fillRect(colorPickerX - 5, colorPickerY + 192, 233, 5);
        g.fillRect(colorPickerX - 5, colorPickerY - 5, 5, 202);
        g.fillRect(colorPickerX + 192, colorPickerY - 5, 5, 202);
        g.fillRect(colorPickerX + 223, colorPickerY - 5, 5, 202);
        Font font = new Font("Verdana", Font.PLAIN, 15);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        if(colorR != -1 && colorG != -1 && colorB != -1)
        {
            g.drawString("R: " + colorR, 770, 33 + fm.getAscent()/2);
            g.drawString("G: " + colorG, 770, 53 + fm.getAscent()/2);
            g.drawString("B: " + colorB, 770, 73 + fm.getAscent()/2);
        }
        else
        {
            g.drawString("R: ", 770, 33 + fm.getAscent()/2);
            g.drawString("G: ", 770, 53 + fm.getAscent()/2);
            g.drawString("B: ", 770, 73 + fm.getAscent()/2);
        }
        g.drawString("Amount To Fill In", 825 - fm.stringWidth("Amount To Fill In")/2, 99 + fm.getAscent()/2);
        g.drawLine(760, 101 + fm.getAscent()/2, 764 + fm.stringWidth("Amount To Fill In"), 101 + fm.getAscent()/2);
        g.setColor(new Color(255, 255, 255));
        rectWidth = 10 + fm.stringWidth("1x1");
        rectHeight = fm.getAscent() + 4;
        if(drawingSize == 1)
        {
            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(765, 120 - fm.getAscent()/2, rectWidth, rectHeight, 10, 10);
            g.setColor(new Color(0, 0, 0));
            g.drawRoundRect(765, 120 - fm.getAscent()/2, rectWidth, rectHeight, 10, 10);
        }
        if(drawingSize == 3)
        {
            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(765, 140 - fm.getAscent()/2, rectWidth, rectHeight, 10, 10);
            g.setColor(new Color(0, 0, 0));
            g.drawRoundRect(765, 140 - fm.getAscent()/2, rectWidth, rectHeight, 10, 10);
        }
        if(drawingSize == 5)
        {
            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(765, 160 - fm.getAscent()/2, rectWidth, rectHeight, 10, 10);
            g.setColor(new Color(0, 0, 0));
            g.drawRoundRect(765, 160 - fm.getAscent()/2, rectWidth, rectHeight, 10, 10);
        }
        if(drawingSize == 7)
        {
            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(765, 180 - fm.getAscent()/2, rectWidth, rectHeight, 10, 10);
            g.setColor(new Color(0, 0, 0));
            g.drawRoundRect(765, 180 - fm.getAscent()/2, rectWidth, rectHeight, 10, 10);
        }
        if(drawingSize == 9)
        {
            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(765, 200 - fm.getAscent()/2, rectWidth, rectHeight, 10, 10);
            g.setColor(new Color(0, 0, 0));
            g.drawRoundRect(765, 200 - fm.getAscent()/2, rectWidth, rectHeight, 10, 10);
        }
        int fillAllWidth = 10 + fm.stringWidth("Fill All");
        if(drawingSize == -1)
        {
            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(765, 220 - fm.getAscent()/2, fillAllWidth, rectHeight, 10, 10);
            g.setColor(new Color(0, 0, 0));
            g.drawRoundRect(765, 220 - fm.getAscent()/2, fillAllWidth, rectHeight, 10, 10);
        }
        g.setColor(new Color(0, 0, 0));
        g.drawString("1x1", 770, 120 + fm.getAscent()/2);
        g.drawString("3x3", 770, 140 + fm.getAscent()/2);
        g.drawString("5x5", 770, 160 + fm.getAscent()/2);
        g.drawString("7x7", 770, 180 + fm.getAscent()/2);
        g.drawString("9x9", 770, 200 + fm.getAscent()/2);
        g.drawString("Fill All", 770, 220 + fm.getAscent()/2);
        g.drawString("Recently Used Colors", 522, 222);
        g.drawLine(522, 224, 522 + fm.stringWidth("Recently Used Colors"), 224);
        for(int i = 0; i < recentColors.length; i++)
        {
            if(recentColors[i] != -1)
            {
                g.setColor(new Color(recentColors[i]));
                g.fillRect(522 + 40 * i, 232, 30, 30);
                g.setColor(new Color(0, 0, 0));
                g.fillRect(522 + 40 * i, 232, 30, 2);
                g.fillRect(522 + 40 * i, 260, 30, 2);
                g.fillRect(522 + 40 * i, 232, 2, 30);
                g.fillRect(550 + 40 * i, 232, 2, 30);
            }
        }
        g.setColor(new Color(0, 0, 0));
        g.drawString("Drawing Mode", 522, 294);
        g.drawLine(522, 296, 522 + fm.stringWidth("Drawing Mode"), 296);
        rectHeight2 = fm.getAscent() + 4;
        if(drawingMode.equals("Draw"))
        {
            rectWidth2 = 10 + fm.stringWidth("Draw");
            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(527, 312 - fm.getAscent()/2, rectWidth2, rectHeight2, 10, 10);
            g.setColor(new Color(0, 0, 0));
            g.drawRoundRect(527, 312 - fm.getAscent()/2, rectWidth2, rectHeight2, 10, 10);
        }
        if(drawingMode.equals("Select Color"))
        {
            rectWidth2 = 10 + fm.stringWidth("Select Color");
            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(527, 332 - fm.getAscent()/2, rectWidth2, rectHeight2, 10, 10);
            g.setColor(new Color(0, 0, 0));
            g.drawRoundRect(527, 332 - fm.getAscent()/2, rectWidth2, rectHeight2, 10, 10);
        }
        if(drawingMode.equals("Copy"))
        {
            rectWidth2 = 10 + fm.stringWidth("Copy");
            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(527, 352 - fm.getAscent()/2, rectWidth2, rectHeight2, 10, 10);
            g.setColor(new Color(0, 0, 0));
            g.drawRoundRect(527, 352 - fm.getAscent()/2, rectWidth2, rectHeight2, 10, 10);
        }
        if(drawingMode.equals("Paste"))
        {
            rectWidth2 = 10 + fm.stringWidth("Paste");
            g.setColor(new Color(255, 255, 255));
            g.fillRoundRect(527, 372 - fm.getAscent()/2, rectWidth2, rectHeight2, 10, 10);
            g.setColor(new Color(0, 0, 0));
            g.drawRoundRect(527, 372 - fm.getAscent()/2, rectWidth2, rectHeight2, 10, 10);
        }
        g.drawString("Draw", 532, 312+ fm.getAscent()/2);
        g.drawString("Select Color", 532, 332 + fm.getAscent()/2);
        g.drawString("Copy", 532, 352 + fm.getAscent()/2);
        g.drawString("Paste", 532, 372 + fm.getAscent()/2);
        if(drawingMode.equals("Copy"))
        {
            g.setColor(new Color(128, 128, 128, 128));
            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            int maxX = Math.max(x1, x2);
            int maxY = Math.max(y1, y2);
            g.fillRect(minX, minY, maxX - minX, maxY - minY);
            if(drawCopyText)
            {
                int centerX = (minX + maxX) / 2;
                int centerY = (minY + maxY) / 2;
                g.setColor(new Color(255, 255, 255));
                g.fillRoundRect(centerX - (10 + fm.stringWidth("Copy"))/2, centerY - (fm.getAscent() + 4)/2, 10 + fm.stringWidth("Copy"), fm.getAscent() + 4, 10, 10);
                g.setColor(new Color(0, 0, 0));
                g.drawRoundRect(centerX - (10 + fm.stringWidth("Copy"))/2, centerY - (fm.getAscent() + 4)/2, 10 + fm.stringWidth("Copy"), fm.getAscent() + 4, 10, 10);
                g.setColor(new Color(0, 0, 0));
                g.drawString("Copy", centerX - fm.stringWidth("Copy")/2, centerY + fm.getAscent()/2 - 2);
                copyTextWidth = 10 + fm.stringWidth("Copy");
                copyTextHeight = fm.getAscent() + 4;
            }
        }
        if(drawingMode.equals("Paste") && mouseX < height)
        {
            g.setColor(new Color(128, 128, 128, 128));
            int adjustx = 0;
            if(pasteWidth % 2 == 0)
                adjustx = 4;
            int adjusty = 0;
            if(pasteHeight % 2 == 0)
                adjusty = 4;
            if(copy.length > 0)
            {
                pasteWidth = copy.length;
                pasteHeight = copy[0].length;
                pasteX = Math.max(0, Math.min((mouseX + adjustx)/cellSize - pasteWidth/2, size - pasteWidth));
                pasteY = Math.max(0, Math.min((mouseY + adjusty)/cellSize - pasteHeight/2, size - pasteHeight));
            }
            g.fillRect(cellSize * pasteX, cellSize * pasteY, cellSize * pasteWidth, cellSize * pasteHeight);
        }
        if(drawingMode.equals("Draw") && mouseX >= 0 && mouseX < height && mouseY >= 0 && mouseY < height && drawingSize > 0)
        {
            // Show preview of drawing area
            g.setColor(new Color(255, 255, 255, 128));
            int centerCellX = size * mouseX / height;
            int centerCellY = size * mouseY / height;
            for (int y = centerCellY - drawingSize / 2; y <= centerCellY + drawingSize / 2; y++) {
                for (int x = centerCellX - drawingSize / 2; x <= centerCellX + drawingSize / 2; x++) {
                    if (x >= 0 && x < size && y >= 0 && y < size) {
                        g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                }
            }
        }
        if(drawingMode.equals("Select Color") && mouseX >= 0 && mouseX < height && mouseY >= 0 && mouseY < height)
        {
            // Show which cell will be selected
            g.setColor(new Color(255, 255, 255, 128));
            int cellX = size * mouseX / height;
            int cellY = size * mouseY / height;
            if (cellX >= 0 && cellX < size && cellY >= 0 && cellY < size) {
                g.fillRect(cellX * cellSize, cellY * cellSize, cellSize, cellSize);
            }
        }
        g.setColor(new Color(255, 255, 255));
        g.fillRoundRect(width - 100, height - 100, 90, 90, 16, 16);
        g.setColor(new Color(0, 0, 0));
        g.drawRoundRect(width - 100, height - 100, 90, 90, 16, 16);
        g.drawString("SAVE", width - 55 - fm.stringWidth("SAVE")/2, height - 55 + fm.getAscent()/2);
        if(colorR != -1 && colorG != -1 && colorB != -1)
        {
            g.setColor(new Color(0, 0, 0));
            g.drawOval(colorX - 4, colorY - 4, 8, 8);
            g.drawOval(colorX - 5, colorY - 5, 10, 10);
        }
        tm.start();
    }

    public void actionPerformed(ActionEvent ae)
    {
        repaint();
    }

    public void mouseClicked(MouseEvent me)
    {
        if(mouseX >= colorPickerX + 191 && mouseX <= colorPickerX + 194)
            mouseX = colorPickerX + 191;
        if(mouseX >= colorPickerX + 195 && mouseX <= colorPickerX + 197)
            mouseX = colorPickerX + 197;
        if(mouseX >= colorPickerX && mouseX < colorPickerX + 223 && mouseY >= colorPickerY && mouseY < colorPickerY + 192)
        {
            if(mouseX == colorPickerX)
            {
                colorR = 255;
                colorG = 0;
                colorB = 0;
            }
            else if(mouseX < colorPickerX + 32)
            {
                colorR = 255;
                colorG = (mouseX - colorPickerX) % 32 * 8;
                colorB = 0;
            }
            else if(mouseX < colorPickerX + 64)
            {
                colorR = 255 - (mouseX - colorPickerX) % 32 * 8;
                colorG = 255;
                colorB = 0;
            }
            else if(mouseX < colorPickerX + 96)
            {
                colorR = 0;
                colorG = 255;
                colorB = (mouseX - colorPickerX) % 32 * 8;
            }
            else if(mouseX < colorPickerX + 128)
            {
                colorR = 0;
                colorG = 255 - (mouseX - colorPickerX) % 32 * 8;
                colorB = 255;
            }
            else if(mouseX < colorPickerX + 160)
            {
                colorR = (mouseX - colorPickerX) % 32 * 8;
                colorG = 0;
                colorB = 255;
            }
            else if(mouseX < colorPickerX + 192)
            {
                colorR = 255;
                colorG = 0;
                colorB = 255 - (mouseX - colorPickerX) % 32 * 8;
            }
            else
            {
                colorR = 128;
                colorG = 128;
                colorB = 128;
            }
            if(mouseY < colorPickerY + 96)
            {
                colorR = (int) (colorR * (mouseY - colorPickerY)/96.0);
                colorG = (int) (colorG * (mouseY - colorPickerY)/96.0);
                colorB = (int) (colorB * (mouseY - colorPickerY)/96.0);
            }
            else {
                colorR = 255 - (int) ((255 - colorR) * (colorPickerY + 191 - mouseY)/96.0);
                colorG = 255 - (int) ((255 - colorG) * (colorPickerY + 191 - mouseY)/96.0);
                colorB = 255 - (int) ((255 - colorB) * (colorPickerY + 191 - mouseY)/96.0);
            }
            colorR = (int) Math.round(colorR/8.0) * 8;
            colorG = (int) Math.round(colorG/8.0) * 8;
            colorB = (int) Math.round(colorB/8.0) * 8;
            colorR = Math.max(colorR, 0);
            colorG = Math.max(colorG, 0);
            colorB = Math.max(colorB, 0);
            colorR = Math.min(colorR, 255);
            colorG = Math.min(colorG, 255);
            colorB = Math.min(colorB, 255);
            colorX = mouseX;
            colorY = mouseY;
            if(colorX >= colorPickerX + 191 && colorX <= colorPickerX + 194)
                colorX = colorPickerX + 191;
            if(colorX >= colorPickerX + 195 && colorX <= colorPickerX + 197)
                colorX = colorPickerX + 197;
            clickedRecent = false;
            updateTextFields();
        }
        else if(mouseX >= 0 && mouseX < height && mouseY >= 0 && mouseY < height)
        {
            switch (drawingMode) {
                case "Draw" -> {
                    if (drawingSize == -1) {
                        // Fill All button
                        fillAll();
                    } else {
                        // Normal drawing with brush size
                        for (int y = size * mouseY / height - drawingSize / 2; y <= size * mouseY / height + drawingSize / 2; y++) {
                            for (int x = size * mouseX / height - drawingSize / 2; x <= size * mouseX / height + drawingSize / 2; x++) {
                                boolean stillRecent = false;
                                for (int i = recentColors.length - 1; i >= 0; i--) {
                                    if (recentColors[i] == rgbNum(colorR, colorG, colorB)) {
                                        stillRecent = true;
                                        break;
                                    }
                                }
                                if (stillRecent && (colorR != -1 && colorG != -1 && colorB != -1)) {
                                    int color = 0;
                                    int index = 0;
                                    for (int i = recentColors.length - 1; i >= 0; i--) {
                                        if (recentColors[i] == rgbNum(colorR, colorG, colorB)) {
                                            color = rgbNum(colorR, colorG, colorB);
                                            index = i;
                                            if (clickedRecent) {
                                                colorX = 537;
                                                colorY = 247;
                                            }
                                        }
                                    }
                                    for (int i = index; i >= 1; i--) {
                                        recentColors[i] = recentColors[i - 1];
                                    }
                                    recentColors[0] = color;
                                } else if (colorR != -1 && colorG != -1 && colorB != -1) {
                                    for (int i = recentColors.length - 1; i >= 0; i--) {
                                        if (i == 0)
                                            recentColors[0] = rgbNum(colorR, colorG, colorB);
                                        else
                                            recentColors[i] = recentColors[i - 1];
                                    }
                                }
                                if (colorR != -1 && colorG != -1 && colorB != -1)
                                    pixels[Math.min(Math.max(x, 0), 63)][Math.min(Math.max(y, 0), 63)] = rgbNum(colorR, colorG, colorB);
                            }
                        }
                    }
                }
                case "Select Color" -> {
                    // Pick color from the pixel at mouse position
                    int pixelX = size * mouseX / height;
                    int pixelY = size * mouseY / height;
                    selectColorFromPixel(pixelX, pixelY);
                }
                case "Copy" -> {
                    if (drawCopyText) {
                        int minX = Math.min(x1, x2);
                        int minY = Math.min(y1, y2);
                        int maxX = Math.max(x1, x2);
                        int maxY = Math.max(y1, y2);
                        int centerX = (minX + maxX) / 2;
                        int centerY = (minY + maxY) / 2;
                        if (mouseX > centerX - copyTextWidth / 2 && mouseX < centerX + copyTextWidth / 2 && 
                            mouseY > centerY - copyTextHeight / 2 && mouseY < centerY + copyTextHeight / 2)
                            copy();
                        else {
                            startCellX = -1;
                            startCellY = -1;
                            x1 = -1;
                            y1 = -1;
                            x2 = -1;
                            y2 = -1;
                        }
                    } else {
                        startCellX = -1;
                        startCellY = -1;
                        x1 = -1;
                        y1 = -1;
                        x2 = -1;
                        y2 = -1;
                    }
                }
                case "Paste" -> {
                    if (copy.length > 0 && pasteWidth > 0 && pasteHeight > 0)
                        paste();
                }
            }
        }
        if(mouseX > width - 100 && mouseX < width - 10 && mouseY > height - 100 && mouseY < height - 10)
        {
            try
            {
                saveTexture();
            }
            catch(IOException ignored)
            {
                
            }
        }
        if(mouseX > 765 && mouseX < 775 + rectWidth && mouseY > 122 - rectHeight/2 && mouseY < 122 + rectHeight)
            drawingSize = 1;
        if(mouseX > 765 && mouseX < 775 + rectWidth && mouseY > 142 - rectHeight/2 && mouseY < 142 + rectHeight)
            drawingSize = 3;
        if(mouseX > 765 && mouseX < 775 + rectWidth && mouseY > 162 - rectHeight/2 && mouseY < 162 + rectHeight)
            drawingSize = 5;
        if(mouseX > 765 && mouseX < 775 + rectWidth && mouseY > 182 - rectHeight/2 && mouseY < 182 + rectHeight)
            drawingSize = 7;
        if(mouseX > 765 && mouseX < 775 + rectWidth && mouseY > 202 - rectHeight/2 && mouseY < 202 + rectHeight)
            drawingSize = 9;
        FontMetrics fm = new JLabel().getFontMetrics(new Font("Verdana", Font.PLAIN, 15));
        int fillAllWidth = 10 + fm.stringWidth("Fill All");
        if(mouseX > 765 && mouseX < 775 + fillAllWidth && mouseY > 222 - rectHeight/2 && mouseY < 222 + rectHeight)
            drawingSize = -1;
        if(mouseX > 532 && mouseX < 542 + rectWidth2 && mouseY > 312 - rectHeight/2 && mouseY < 312 + rectWidth)
            drawingMode = "Draw";
        if(mouseX > 532 && mouseX < 542 + rectWidth2 && mouseY > 332 - rectHeight/2 && mouseY < 332 + rectWidth)
            drawingMode = "Select Color";
        if(mouseX > 532 && mouseX < 542 + rectWidth2 && mouseY > 352 - rectHeight/2 && mouseY < 352 + rectWidth)
            drawingMode = "Copy";
        if(mouseX > 532 && mouseX < 542 + rectWidth2 && mouseY > 372 - rectHeight/2 && mouseY < 372 + rectWidth)
            drawingMode = "Paste";
        for(int i = 0; i < recentColors.length; i++)
        {
            if(mouseX > 522 + 40 * i && mouseX < 552 + 40 * i && mouseY > 232 && mouseY < 262)
            {
                colorR = getR(recentColors[i]);
                colorG = getG(recentColors[i]);
                colorB = getB(recentColors[i]);
                colorX = 537 + 40 * i;
                colorY = 247;
                clickedRecent = true;
                updateTextFields();
            }
        }
        repaint();
    }

    public void mouseEntered(MouseEvent me)
    {

    }

    public void mouseExited(MouseEvent me)
    {

    }

    public void mousePressed(MouseEvent me)
    {
        mouseX = me.getX();
        mouseY = me.getY() + MOUSE_Y_OFFSET;
        
        if(drawingMode.equals("Copy"))
        {
            if(drawCopyText) {
                int minX = Math.min(x1, x2);
                int minY = Math.min(y1, y2);
                int maxX = Math.max(x1, x2);
                int maxY = Math.max(y1, y2);
                int centerX = (minX + maxX) / 2;
                int centerY = (minY + maxY) / 2;
                if (mouseX > centerX - copyTextWidth / 2 && mouseX < centerX + copyTextWidth / 2 && 
                    mouseY > centerY - copyTextHeight / 2 && mouseY < centerY + copyTextHeight / 2)
                    copy();
                else {
                    // Start new selection - get the cell the mouse is in
                    if(mouseX >= 0 && mouseX < height && mouseY >= 0 && mouseY < height) {
                        // Store the starting cell position
                        startCellX = (mouseX / cellSize) * cellSize;
                        startCellY = (mouseY / cellSize) * cellSize;
                        // Initially select just this one cell
                        x1 = startCellX;
                        y1 = startCellY;
                        x2 = startCellX + cellSize;
                        y2 = startCellY + cellSize;
                    } else {
                        startCellX = -1;
                        startCellY = -1;
                        x1 = -1;
                        y1 = -1;
                        x2 = -1;
                        y2 = -1;
                    }
                }
            } else {
                // Start new selection - get the cell the mouse is in
                if(mouseX >= 0 && mouseX < height && mouseY >= 0 && mouseY < height) {
                    // Store the starting cell position
                    startCellX = (mouseX / cellSize) * cellSize;
                    startCellY = (mouseY / cellSize) * cellSize;
                    // Initially select just this one cell
                    x1 = startCellX;
                    y1 = startCellY;
                    x2 = startCellX + cellSize;
                    y2 = startCellY + cellSize;
                } else {
                    startCellX = -1;
                    startCellY = -1;
                    x1 = -1;
                    y1 = -1;
                    x2 = -1;
                    y2 = -1;
                }
            }
        }
        drawCopyText = false;
    }

    public void mouseReleased(MouseEvent me)
    {
        if(drawingMode.equals("Copy") && (x2 != x1 || y2 != y1))
        {
            drawCopyText = true;
        }
    }

    public void mouseDragged(MouseEvent me)
    {
        mouseX = me.getX();
        mouseY = me.getY() + MOUSE_Y_OFFSET;
        if(mouseX >= colorPickerX + 191 && mouseX <= colorPickerX + 194)
            mouseX = colorPickerX + 191;
        if(mouseX >= colorPickerX + 195 && mouseX <= colorPickerX + 197)
            mouseX = colorPickerX + 197;
        if(mouseX >= colorPickerX && mouseX < colorPickerX + 223 && mouseY >= colorPickerY && mouseY < colorPickerY + 192) {
            if(mouseX == colorPickerX) {
                colorR = 255;
                colorG = 0;
                colorB = 0;
            } else if(mouseX < colorPickerX + 32) {
                colorR = 255;
                colorG = (mouseX - colorPickerX) % 32 * 8;
                colorB = 0;
            } else if(mouseX < colorPickerX + 64) {
                colorR = 255 - (mouseX - colorPickerX) % 32 * 8;
                colorG = 255;
                colorB = 0;
            } else if(mouseX < colorPickerX + 96) {
                colorR = 0;
                colorG = 255;
                colorB = (mouseX - colorPickerX) % 32 * 8;
            } else if(mouseX < colorPickerX + 128) {
                colorR = 0;
                colorG = 255 - (mouseX - colorPickerX) % 32 * 8;
                colorB = 255;
            } else if(mouseX < colorPickerX + 160) {
                colorR = (mouseX - colorPickerX) % 32 * 8;
                colorG = 0;
                colorB = 255;
            } else if(mouseX < colorPickerX + 192) {
                colorR = 255;
                colorG = 0;
                colorB = 255 - (mouseX - colorPickerX) % 32 * 8;
            } else {
                colorR = 128;
                colorG = 128;
                colorB = 128;
            }
            if(mouseY < colorPickerY + 96) {
                colorR = (int) (colorR * (mouseY - colorPickerY)/96.0);
                colorG = (int) (colorG * (mouseY - colorPickerY)/96.0);
                colorB = (int) (colorB * (mouseY - colorPickerY)/96.0);
            } else {
                colorR = 255 - (int) ((255 - colorR) * (colorPickerY + 191 - mouseY)/96.0);
                colorG = 255 - (int) ((255 - colorG) * (colorPickerY + 191 - mouseY)/96.0);
                colorB = 255 - (int) ((255 - colorB) * (colorPickerY + 191 - mouseY)/96.0);
            }
            colorR = (int) Math.round(colorR/8.0) * 8;
            colorG = (int) Math.round(colorG/8.0) * 8;
            colorB = (int) Math.round(colorB/8.0) * 8;
            colorR = Math.max(colorR, 0);
            colorG = Math.max(colorG, 0);
            colorB = Math.max(colorB, 0);
            colorR = Math.min(colorR, 255);
            colorG = Math.min(colorG, 255);
            colorB = Math.min(colorB, 255);
            colorX = mouseX;
            colorY = mouseY;
            if(colorX >= colorPickerX + 191 && colorX <= colorPickerX + 194)
                colorX = colorPickerX + 191;
            if(colorX >= colorPickerX + 195 && colorX <= colorPickerX + 197)
                colorX = colorPickerX + 197;
            clickedRecent = false;
            updateTextFields();
        }
        else if(mouseX >= 0 && mouseX < height && mouseY >= 0 && mouseY < height)
        {
            switch (drawingMode) {
                case "Draw" -> {
                    if (drawingSize == -1) {
                        // Fill All button - do nothing on drag
                    } else {
                        // Normal drawing with brush size
                        for (int y = size * mouseY / height - drawingSize / 2; y <= size * mouseY / height + drawingSize / 2; y++) {
                            for (int x = size * mouseX / height - drawingSize / 2; x <= size * mouseX / height + drawingSize / 2; x++) {
                                boolean stillRecent = false;
                                for (int i = recentColors.length - 1; i >= 0; i--) {
                                    if (recentColors[i] == rgbNum(colorR, colorG, colorB)) {
                                        stillRecent = true;
                                        break;
                                    }
                                }
                                if (stillRecent && (colorR != -1 && colorG != -1 && colorB != -1)) {
                                    int color = 0;
                                    int index = 0;
                                    for (int i = recentColors.length - 1; i >= 0; i--) {
                                        if (recentColors[i] == rgbNum(colorR, colorG, colorB)) {
                                            color = rgbNum(colorR, colorG, colorB);
                                            index = i;
                                            if (clickedRecent) {
                                                colorX = 537;
                                                colorY = 247;
                                            }
                                        }
                                    }
                                    for (int i = index; i >= 1; i--) {
                                        recentColors[i] = recentColors[i - 1];
                                    }
                                    recentColors[0] = color;
                                } else if (colorR != -1 && colorG != -1 && colorB != -1) {
                                    for (int i = recentColors.length - 1; i >= 0; i--) {
                                        if (i == 0)
                                            recentColors[0] = rgbNum(colorR, colorG, colorB);
                                        else
                                            recentColors[i] = recentColors[i - 1];
                                    }
                                }
                                if (colorR != -1 && colorG != -1 && colorB != -1)
                                    pixels[Math.min(Math.max(x, 0), 63)][Math.min(Math.max(y, 0), 63)] = rgbNum(colorR, colorG, colorB);
                            }
                        }
                    }
                }
                case "Select Color" -> {
                    // Pick color from the pixel at mouse position
                    int pixelX = size * mouseX / height;
                    int pixelY = size * mouseY / height;
                    selectColorFromPixel(pixelX, pixelY);
                }
                case "Copy" -> {
                    if (startCellX == -1) {
                        // Store the starting cell position
                        // Clamp mouseX/mouseY to valid drawing area
                        int clampedX = Math.max(0, Math.min(mouseX, height - 1));
                        int clampedY = Math.max(0, Math.min(mouseY, height - 1));
                        startCellX = (clampedX / cellSize) * cellSize;
                        startCellY = (clampedY / cellSize) * cellSize;
                        x1 = startCellX;
                        y1 = startCellY;
                        x2 = startCellX + cellSize;
                        y2 = startCellY + cellSize;
                    } else {
                        // Get the current cell
                        // Clamp mouseX/mouseY to valid drawing area
                        int clampedX = Math.max(0, Math.min(mouseX, height - 1));
                        int clampedY = Math.max(0, Math.min(mouseY, height - 1));
                        int cellX = (clampedX / cellSize) * cellSize;
                        int cellY = (clampedY / cellSize) * cellSize;
                        
                        // Calculate selection bounds based on starting cell and current cell
                        if (cellX < startCellX) {
                            // Dragging left
                            x1 = cellX;
                            x2 = startCellX + cellSize;
                        } else {
                            // Dragging right
                            x1 = startCellX;
                            x2 = cellX + cellSize;
                        }
                        
                        if (cellY < startCellY) {
                            // Dragging up
                            y1 = cellY;
                            y2 = startCellY + cellSize;
                        } else {
                            // Dragging down
                            y1 = startCellY;
                            y2 = cellY + cellSize;
                        }
                    }
                    
                    if (drawCopyText) {
                        int minX = Math.min(x1, x2);
                        int minY = Math.min(y1, y2);
                        int maxX = Math.max(x1, x2);
                        int maxY = Math.max(y1, y2);
                        int centerX = (minX + maxX) / 2;
                        int centerY = (minY + maxY) / 2;
                        if (mouseX > centerX - copyTextWidth / 2 && mouseX < centerX + copyTextWidth / 2 && 
                            mouseY > centerY - copyTextHeight / 2 && mouseY < centerY + copyTextHeight / 2)
                            copy();
                    }
                }
                case "Paste" -> {
                    if (copy.length > 0 && pasteWidth > 0 && pasteHeight > 0)
                        paste();
                }
            }
        }
        if(mouseX > width - 100 && mouseX < width - 10 && mouseY > height - 100 && mouseY < height - 10)
        {
            try
            {
                saveTexture();
            }
            catch(IOException ignored)
            {
                
            }
        }
        if(mouseX > 765 && mouseX < 775 + rectWidth && mouseY > 122 - rectHeight/2 && mouseY < 122 + rectHeight)
            drawingSize = 1;
        if(mouseX > 765 && mouseX < 775 + rectWidth && mouseY > 142 - rectHeight/2 && mouseY < 142 + rectHeight)
            drawingSize = 3;
        if(mouseX > 765 && mouseX < 775 + rectWidth && mouseY > 162 - rectHeight/2 && mouseY < 162 + rectHeight)
            drawingSize = 5;
        if(mouseX > 765 && mouseX < 775 + rectWidth && mouseY > 182 - rectHeight/2 && mouseY < 182 + rectHeight)
            drawingSize = 7;
        if(mouseX > 765 && mouseX < 775 + rectWidth && mouseY > 202 - rectHeight/2 && mouseY < 202 + rectHeight)
            drawingSize = 9;
        FontMetrics fm2 = new JLabel().getFontMetrics(new Font("Verdana", Font.PLAIN, 15));
        int fillAllWidth2 = 10 + fm2.stringWidth("Fill All");
        if(mouseX > 765 && mouseX < 775 + fillAllWidth2 && mouseY > 222 - rectHeight/2 && mouseY < 222 + rectHeight)
            drawingSize = -1;
        if(mouseX > 532 && mouseX < 542 + rectWidth2 && mouseY > 312 - rectHeight/2 && mouseY < 312 + rectWidth)
            drawingMode = "Draw";
        if(mouseX > 532 && mouseX < 542 + rectWidth2 && mouseY > 332 - rectHeight/2 && mouseY < 332 + rectWidth)
            drawingMode = "Select Color";
        if(mouseX > 532 && mouseX < 542 + rectWidth2 && mouseY > 352 - rectHeight/2 && mouseY < 352 + rectWidth)
            drawingMode = "Copy";
        if(mouseX > 532 && mouseX < 542 + rectWidth2 && mouseY > 372 - rectHeight/2 && mouseY < 372 + rectWidth)
            drawingMode = "Paste";
        for(int i = 0; i < recentColors.length; i++)
        {
            if(mouseX > 522 + 40 * i && mouseX < 552 + 40 * i && mouseY > 232 && mouseY < 262)
            {
                colorR = getR(recentColors[i]);
                colorG = getG(recentColors[i]);
                colorB = getB(recentColors[i]);
                colorX = 537 + 40 * i;
                colorY = 247;
                clickedRecent = true;
                updateTextFields();
            }
        }
        repaint();
    }

    public void mouseMoved(MouseEvent me)
    {
        mouseX = me.getX();
        mouseY = me.getY() + MOUSE_Y_OFFSET;
    }

    private void saveTexture() throws IOException
    {
        String location = JOptionPane.showInputDialog("Name of file to save to:");
        if(location != null && !location.isEmpty())
        {
            // Try relative path from source directory first
            File dir = new File("../../resources/SavedTextures");
            if(!dir.exists()) {
                // Use local resources directory when running from JAR
                dir = new File("resources/SavedTextures");
                if(!dir.exists())
                    dir.mkdirs();
            }
            PrintWriter outFile = new PrintWriter(new File(dir, location + ".txt"));
            for(int x = 0; x < size; x++)
            {
                for(int y = 0; y < size; y++)
                {
                    outFile.print(pixels[y][x] + " ");
                }
                outFile.println();
            }
            outFile.close();
            JOptionPane.showMessageDialog(null, "Saved to '" + location + ".txt'\nin resources/SavedTextures folder.");
        }
    }

    private void copy()
    {
        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        
        pasteWidth = (maxX - minX) / cellSize;
        pasteHeight = (maxY - minY) / cellSize;
        
        if(pasteWidth > 0 && pasteHeight > 0) {
            copy = new int[pasteWidth][pasteHeight];
            for(int x = 0; x < pasteWidth; x++)
            {
                System.arraycopy(pixels[x + minX / cellSize], minY / cellSize, copy[x], 0, pasteHeight);
            }
        }
    }

    private void paste()
    {
        if(pasteWidth > 0 && pasteHeight > 0)
        {
            for(int x = 0; x < copy.length; x++)
            {
                for(int y = 0; y < copy[0].length; y++)
                {
                    if(pasteX < 0)
                        pasteX = 0;
                    if(pasteX > size - copy.length)
                        pasteX = size - copy.length;
                    if(pasteY < 0)
                        pasteY = 0;
                    if(pasteY > size - copy[0].length)
                        pasteY = size - copy[0].length;
                    pixels[x + pasteX][y + pasteY] = copy[x][y];
                }
            }
            pasteWidth = 0;
            pasteHeight = 0;
            pasteX = 0;
            pasteY = 0;
        }
    }

    private void fillAll()
    {
        if (colorR != -1 && colorG != -1 && colorB != -1) {
            int fillColor = rgbNum(colorR, colorG, colorB);
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    pixels[x][y] = fillColor;
                }
            }
            addToRecentColors(fillColor);
        }
    }
    
    private void selectColorFromPixel(int pixelX, int pixelY)
    {
        if (pixelX >= 0 && pixelX < size && pixelY >= 0 && pixelY < size) {
            int selectedColor = pixels[pixelX][pixelY];
            colorR = getR(selectedColor);
            colorG = getG(selectedColor);
            colorB = getB(selectedColor);
            
            // Calculate accurate color picker position
            calculateColorPickerPosition();
            
            addToRecentColors(selectedColor);
            clickedRecent = false;
            updateTextFields();
        }
    }
    
    private void addToRecentColors(int color)
    {
        boolean stillRecent = false;
        int index = -1;
        for (int i = recentColors.length - 1; i >= 0; i--) {
            if (recentColors[i] == color) {
                stillRecent = true;
                index = i;
                break;
            }
        }
        if (stillRecent) {
            // Move to front
            for (int i = index; i >= 1; i--) {
                recentColors[i] = recentColors[i - 1];
            }
            recentColors[0] = color;
        } else {
            // Add to front, shift others
            for (int i = recentColors.length - 1; i >= 1; i--) {
                recentColors[i] = recentColors[i - 1];
            }
            recentColors[0] = color;
        }
    }
    
    private void updateColorFromFields()
    {
        try {
            int r = Integer.parseInt(rField.getText().trim());
            int g = Integer.parseInt(gField.getText().trim());
            int b = Integer.parseInt(bField.getText().trim());
            
            // Clamp values to 0-255
            r = Math.max(0, Math.min(255, r));
            g = Math.max(0, Math.min(255, g));
            b = Math.max(0, Math.min(255, b));
            
            colorR = r;
            colorG = g;
            colorB = b;
            
            // Update text fields with clamped values
            rField.setText(String.valueOf(colorR));
            gField.setText(String.valueOf(colorG));
            bField.setText(String.valueOf(colorB));
            
            // Calculate accurate color picker position
            calculateColorPickerPosition();
            
            clickedRecent = false;
            repaint();
        } catch (NumberFormatException ignored) {
            // Invalid input, restore current values
            if (colorR != -1) rField.setText(String.valueOf(colorR));
            if (colorG != -1) gField.setText(String.valueOf(colorG));
            if (colorB != -1) bField.setText(String.valueOf(colorB));
        }
    }
    
    private void calculateColorPickerPosition()
    {
        if (colorR == -1 || colorG == -1 || colorB == -1) return;
        
        int r = colorR;
        int g = colorG;
        int b = colorB;
        
        // Find max and min to determine the pure hue
        int maxComp = Math.max(r, Math.max(g, b));
        int minComp = Math.min(r, Math.min(g, b));
        
        // First, reverse the vertical transformation to find the pure hue color
        // and determine which y position we're at
        int pureR, pureG, pureB;
        int yPos;
        
        if (maxComp == minComp) {
            // Grayscale
            colorX = colorPickerX + 197;
            // Map grayscale value to y position
            if (maxComp <= 128) {
                yPos = colorPickerY + maxComp * 96 / 128;
            } else {
                yPos = colorPickerY + 96 + (maxComp - 128) * 96 / 127;
            }
            colorY = yPos;
            return;
        }
        
        // Determine if we're in the dark zone (y < 96) or light zone (y >= 96)
        // by checking the ratio of min to max
        double ratio = (double)minComp / maxComp;
        
        if (ratio < 0.5) {
            // Dark zone: color = pureHue * (y/96)
            // So: pureHue = color / (y/96) = color * 96 / y
            // And: y = maxComp * 96 / 255 (since pure hue has max=255)
            yPos = colorPickerY + maxComp * 96 / 255;
            
            // Calculate pure hue by scaling up
            if (maxComp > 0) {
                pureR = r * 255 / maxComp;
                pureG = g * 255 / maxComp;
                pureB = b * 255 / maxComp;
            } else {
                pureR = pureG = pureB = 0;
            }
        } else {
            // Light zone: color = 255 - (255 - pureHue) * (192-y)/96
            // Solving for y: (192-y)/96 = (255 - color) / (255 - pureHue)
            // We need to find pureHue first
            
            // In light zone, the pure hue is when minComp = 0
            // The amount of white added is minComp
            // So: pureHue = color - minComp (approximately)
            pureR = Math.max(0, r - minComp);
            pureG = Math.max(0, g - minComp);
            pureB = Math.max(0, b - minComp);
            
            // Normalize to 255
            int pureMax = Math.max(pureR, Math.max(pureG, pureB));
            if (pureMax > 0) {
                pureR = pureR * 255 / pureMax;
                pureG = pureG * 255 / pureMax;
                pureB = pureB * 255 / pureMax;
            }
            
            // Calculate y position
            // At y=96: minComp should be 0
            // At y=192: minComp should equal maxComp (white)
            yPos = colorPickerY + 96 + minComp * 96 / maxComp;
        }
        
        // Now find the x position based on the pure hue
        int xPos = colorPickerX;
        
        // Determine which segment the pure hue is in
        if (pureR == 255 && pureB == 0) {
            // Red to Yellow (0-32): R=255, G=0-255, B=0
            xPos = colorPickerX + pureG * 32 / 255;
        } else if (pureG == 255 && pureB == 0) {
            // Yellow to Green (32-64): R=255-0, G=255, B=0
            xPos = colorPickerX + 32 + (255 - pureR) * 32 / 255;
        } else if (pureG == 255 && pureR == 0) {
            // Green to Cyan (64-96): R=0, G=255, B=0-255
            xPos = colorPickerX + 64 + pureB * 32 / 255;
        } else if (pureB == 255 && pureR == 0) {
            // Cyan to Blue (96-128): R=0, G=255-0, B=255
            xPos = colorPickerX + 96 + (255 - pureG) * 32 / 255;
        } else if (pureB == 255 && pureG == 0) {
            // Blue to Magenta (128-160): R=0-255, G=0, B=255
            xPos = colorPickerX + 128 + pureR * 32 / 255;
        } else if (pureR == 255 && pureG == 0) {
            // Magenta to Red (160-192): R=255, G=0, B=255-0
            xPos = colorPickerX + 160 + (255 - pureB) * 32 / 255;
        }
        
        // Clamp and set
        colorX = Math.max(colorPickerX, Math.min(xPos, colorPickerX + 222));
        colorY = Math.max(colorPickerY, Math.min(yPos, colorPickerY + 191));
        
        // Handle special positions for gray column
        if(colorX >= colorPickerX + 191 && colorX <= colorPickerX + 194)
            colorX = colorPickerX + 191;
        if(colorX >= colorPickerX + 195 && colorX <= colorPickerX + 197)
            colorX = colorPickerX + 197;
    }
    
    private void updateTextFields()
    {
        if (colorR != -1) rField.setText(String.valueOf(colorR));
        if (colorG != -1) gField.setText(String.valueOf(colorG));
        if (colorB != -1) bField.setText(String.valueOf(colorB));
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

    public static void main(String[] args)
    {
        TextureEditor te = new TextureEditor();
        te.setPreferredSize(new Dimension(width, height));
        JFrame jf = new JFrame();
        jf.setTitle("Texture Editor");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setResizable(false);
        
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu templatesMenu = new JMenu("Load Template");
        
        // Add template options
        String[] templates = {"Bricks", "Black", "Cool Pattern", "Grass", "Gravel", "Gray",
                             "Flag", "XOR", "Circles", "Collisions", "Wave", "Dog",
                             "Fractal", "Map", "Tiles", "Waves"};
        
        for(String template : templates) {
            JMenuItem item = new JMenuItem(template);
            item.addActionListener(e -> {
                Texture t = switch(template) {
                    case "Bricks" -> Texture.bricks;
                    case "Black" -> Texture.black;
                    case "Cool Pattern" -> Texture.coolpattern;
                    case "Grass" -> Texture.grass;
                    case "Gravel" -> Texture.gravel;
                    case "Gray" -> Texture.gray;
                    case "Flag" -> Texture.flag;
                    case "XOR" -> Texture.xor;
                    case "Circles" -> Texture.circles;
                    case "Collisions" -> Texture.collisions;
                    case "Wave" -> Texture.wave;
                    case "Dog" -> Texture.dog;
                    case "Fractal" -> Texture.fractal;
                    case "Map" -> Texture.map;
                    case "Tiles" -> Texture.tiles;
                    case "Waves" -> Texture.waves;
                    default -> Texture.bricks;
                };
                te.loadTexture(t);
                te.repaint();
            });
            templatesMenu.add(item);
        }
        
        fileMenu.add(templatesMenu);
        menuBar.add(fileMenu);
        jf.setJMenuBar(menuBar);
        
        jf.add(te);
        jf.pack();
        jf.setLocationRelativeTo(null);
        jf.setVisible(true);
    }
}