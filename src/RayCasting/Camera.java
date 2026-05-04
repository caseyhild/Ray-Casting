import java.awt.*;
import java.awt.event.*;
public class Camera implements MouseListener, MouseMotionListener, KeyListener
{
    public double xPos, yPos, xDir, yDir, xPlane, yPlane;
    private boolean left;
    private boolean right;
    private boolean forward;
    private boolean back;
    private final double turnLeft;
    private final double turnRight;
    private int turns;
    public boolean shoot;
    public int timer;
    public int mouseX;
    public int mouseY;
    public int mouseDeltaY;
    private int prevMouseY;
    public int mouseDeltaX;
    private int prevMouseX;
    private final int winWidth;
    private final int winHeight;
    private int skipMoveEvents;
    private Robot robot;
    private int warpTargetX;
    private int warpTargetY;
    private Runnable cursorHider;

    public Camera(double x, double y, double xd, double yd, double xp, double yp, int width, int height)
    {   
        //gets starting location and direction
        xPos = x;
        yPos = y;
        xDir = xd;
        yDir = yd;
        xPlane = xp;
        yPlane = yp;
        left = false;
        right = false;
        forward = false;
        back = false;
        turnLeft = 0;
        turnRight = 0;
        shoot = false;
        timer = 0;
        winWidth = width;
        winHeight = height;
        mouseX = winWidth / 2;
        mouseY = winHeight / 2;
        mouseDeltaY = 0;
        prevMouseY = winHeight / 2;
        mouseDeltaX = 0;
        prevMouseX = winWidth / 2;
        skipMoveEvents = 0;
        try { robot = new Robot(); } catch (AWTException ignored) { robot = null; }
        warpTargetX = Toolkit.getDefaultToolkit().getScreenSize().width / 2;
        warpTargetY = Toolkit.getDefaultToolkit().getScreenSize().height / 2;
    }
    public void warpCenter()
    {
        mouseX = winWidth / 2;
        mouseY = winHeight / 2;
        prevMouseX = winWidth / 2;
        prevMouseY = winHeight / 2;
        skipMoveEvents = 2;
    }

    public void setWarpTarget(int screenX, int screenY)
    {
        warpTargetX = screenX;
        warpTargetY = screenY;
    }

    public void setCursorHider(Runnable r)
    {
        cursorHider = r;
    }

    //checks if key is pressed
    public void keyPressed(KeyEvent key)
    {
        //closes program with escape key
        if((key.getKeyCode() == KeyEvent.VK_ESCAPE))
            System.exit(0);
        //W to move forward
        if((key.getKeyCode() == KeyEvent.VK_W))
            forward = true;
        //A to move left
        if((key.getKeyCode() == KeyEvent.VK_A))
            left = true;
        //S to move back
        if((key.getKeyCode() == KeyEvent.VK_S))
            back = true;
        //D to move right
        if((key.getKeyCode() == KeyEvent.VK_D))
            right = true;
    }
    //checks if key is released
    public void keyReleased(KeyEvent key)
    {
        //stops moving forward
        if((key.getKeyCode() == KeyEvent.VK_W))
            forward = false;
        //stops moving left
        if((key.getKeyCode() == KeyEvent.VK_A))
            left = false;
        //stops moving back
        if((key.getKeyCode() == KeyEvent.VK_S))
            back = false;
        //stops moving right
        if((key.getKeyCode() == KeyEvent.VK_D))
            right = false;
    }
    //checks if key is typed
    public void keyTyped(KeyEvent key)
    {

    }

    public void mouseClicked(MouseEvent me)
    {
    }

    public void mouseEntered(MouseEvent me)
    {
        prevMouseX = me.getX();
        prevMouseY = me.getY();
        if (cursorHider != null) cursorHider.run();
    }

    public void mouseExited(MouseEvent me)
    {
        if (robot != null) {
            robot.mouseMove(warpTargetX, warpTargetY);
            mouseX = winWidth / 2;
            prevMouseX = winWidth / 2;
            prevMouseY = winHeight / 2;
            skipMoveEvents = 2;
        }
    }

    public void mousePressed(MouseEvent me)
    {
        shoot = true;
    }

    public void mouseReleased(MouseEvent me)
    {
    }

    public void mouseDragged(MouseEvent me)
    {
        mouseX = me.getX();
        mouseY = me.getY();
        if (skipMoveEvents > 0)
            skipMoveEvents--;
        else
        {
            mouseDeltaX += mouseX - prevMouseX;
            mouseDeltaY += mouseY - prevMouseY;
        }
        prevMouseX = mouseX;
        prevMouseY = mouseY;
    }

    public void mouseMoved(MouseEvent me)
    {
        mouseX = me.getX();
        mouseY = me.getY();
        if (skipMoveEvents > 0)
            skipMoveEvents--;
        else
        {
            mouseDeltaX += mouseX - prevMouseX;
            mouseDeltaY += mouseY - prevMouseY;
        }
        prevMouseX = mouseX;
        prevMouseY = mouseY;
    }

    public void update(int[][] map)
    {
        //moves and turns player based on key input
        double MOVE_SPEED = .08;
        if(forward)
        {
            if(!((int) (xPos + xDir * MOVE_SPEED) < 0 || (int) (xPos + xDir * MOVE_SPEED) > map.length - 1 || (int) (yPos + yDir * MOVE_SPEED) < 0 || (int) (yPos + yDir * MOVE_SPEED) > map[0].length - 1))
            {
                xPos += xDir * MOVE_SPEED;
                if(xDir > 0 && map[(int) (xPos + 0.01)][(int) yPos] != 0)
                    xPos = Math.round(xPos) - 0.01;
                else if(xDir < 0 && map[(int) (xPos - 0.01)][(int) yPos] != 0)
                    xPos = Math.round(xPos) + 0.01;
                yPos += yDir * MOVE_SPEED;
                if(yDir > 0 && map[(int) xPos][(int) (yPos + 0.01)] != 0)
                    yPos = Math.round(yPos) - 0.01;
                else if(yDir < 0 && map[(int) xPos][(int) (yPos - 0.01)] != 0)
                    yPos = Math.round(yPos) + 0.01;
            }
        }
        
        if(back)
        {
            if(!((int) (xPos - xDir * MOVE_SPEED) < 0 || (int) (xPos - xDir * MOVE_SPEED) > map.length - 1 || (int) (yPos - yDir * MOVE_SPEED) < 0 || (int) (yPos - yDir * MOVE_SPEED) > map[0].length - 1))
            {
                xPos -= xDir * MOVE_SPEED;
                if(xDir > 0 && map[(int) (xPos - 0.01)][(int) yPos] != 0)
                    xPos = Math.round(xPos) + 0.01;
                else if(xDir < 0 && map[(int) (xPos + 0.01)][(int) yPos] != 0)
                    xPos = Math.round(xPos) - 0.01;
                yPos -= yDir * MOVE_SPEED;
                if(yDir > 0 && map[(int) xPos][(int) (yPos - 0.01)] != 0)
                    yPos = Math.round(yPos) + 0.01;
                else if(yDir < 0 && map[(int) xPos][(int) (yPos + 0.01)] != 0)
                    yPos = Math.round(yPos) - 0.01;
            }
        }

        if(left)
        {
            if(!((int) (xPos - yDir * MOVE_SPEED) < 0 || (int) (xPos - yDir * MOVE_SPEED) > map.length - 1 || (int) (yPos + xDir * MOVE_SPEED) < 0 || (int) (yPos + xDir * MOVE_SPEED) > map[0].length - 1))
            {
                xPos -= yDir * MOVE_SPEED;
                if(yDir > 0 && map[(int) (xPos - 0.01)][(int) yPos] != 0)
                    xPos = Math.round(xPos) + 0.01;
                else if(yDir < 0 && map[(int) (xPos + 0.01)][(int) yPos] != 0)
                    xPos = Math.round(xPos) - 0.01;
                yPos += xDir * MOVE_SPEED;
                if(xDir > 0 && map[(int) xPos][(int) (yPos + 0.01)] != 0)
                    yPos = Math.round(yPos) - 0.01;
                else if(xDir < 0 && map[(int) xPos][(int) (yPos - 0.01)] != 0)
                    yPos = Math.round(yPos) + 0.01;
            }
        }

        if(right)
        {
            if(!((int) (xPos + yDir * MOVE_SPEED) < 0 || (int) (xPos + yDir * MOVE_SPEED) > map.length - 1 || (int) (yPos - xDir * MOVE_SPEED) < 0 || (int) (yPos - xDir * MOVE_SPEED) > map[0].length - 1))
            {
                xPos += yDir * MOVE_SPEED;
                if(yDir > 0 && map[(int) (xPos + 0.01)][(int) yPos] != 0)
                    xPos = Math.round(xPos) - 0.01;
                else if(yDir < 0 && map[(int) (xPos - 0.01)][(int) yPos] != 0)
                    xPos = Math.round(xPos) + 0.01;
                yPos -= xDir * MOVE_SPEED;
                if(xDir > 0 && map[(int) xPos][(int) (yPos - 0.01)] != 0)
                    yPos = Math.round(yPos) + 0.01;
                else if(xDir < 0 && map[(int) xPos][(int) (yPos + 0.01)] != 0)
                    yPos = Math.round(yPos) - 0.01;
            }
        }

        //warp cursor back to center when near the edge
        if (mouseX < winWidth / 8 || mouseX > winWidth * 7 / 8 || mouseY < winHeight / 8 || mouseY > winHeight * 7 / 8) {
            if (robot != null) {
                robot.mouseMove(warpTargetX, warpTargetY);
                mouseX = winWidth / 2;
                prevMouseX = winWidth / 2;
                prevMouseY = winHeight / 2;
                skipMoveEvents = 2;
            }
        }

        //turning using the mouse
        {
            double rotationAmount = -0.002 * mouseDeltaX;
            mouseDeltaX = 0;
            double oldxDir = xDir;
            xDir = xDir * Math.cos(rotationAmount) - yDir * Math.sin(rotationAmount);
            yDir = oldxDir * Math.sin(rotationAmount) + yDir * Math.cos(rotationAmount);
            double oldxPlane = xPlane;
            xPlane = xPlane * Math.cos(rotationAmount) - yPlane * Math.sin(rotationAmount);
            yPlane = oldxPlane * Math.sin(rotationAmount) + yPlane * Math.cos(rotationAmount);
        }

        //rotates player certain amount left
        if(turnLeft > 0 && turns == 0)
        {
            double oldxDir = xDir;
            xDir = xDir * Math.cos(turnLeft) - yDir * Math.sin(turnLeft);
            yDir = oldxDir * Math.sin(turnLeft) + yDir * Math.cos(turnLeft);
            double oldxPlane = xPlane;
            xPlane = xPlane * Math.cos(turnLeft) - yPlane * Math.sin(turnLeft);
            yPlane = oldxPlane * Math.sin(turnLeft) + yPlane * Math.cos(turnLeft);
            turns = 1;
        }

        //rotates player certain amount right
        if(turnRight > 0 && turns == 0)
        {
            double oldxDir = xDir;
            xDir = xDir * Math.cos(-turnRight) - yDir * Math.sin(-turnRight);
            yDir = oldxDir * Math.sin(-turnRight) + yDir * Math.cos(-turnRight);
            double oldxPlane = xPlane;
            xPlane = xPlane * Math.cos(-turnRight) - yPlane * Math.sin(-turnRight);
            yPlane = oldxPlane * Math.sin(-turnRight) + yPlane * Math.cos(-turnRight);
            turns = 1;
        }
    }
}
