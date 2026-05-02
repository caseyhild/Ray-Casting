import java.awt.event.*;
public class Camera implements KeyListener
{
    public double xPos, yPos, xDir, yDir, xPlane, yPlane;
    public double playerHeight = 0.5;
    private final boolean allowJump;
    private boolean left, right, forward, back, jump;
    private boolean up = false;
    private final double jumpAcceleration = -0.005;
    private final double jumpHeight = 1.0;
    private double jumpSpeed = Math.sqrt(-2 * jumpAcceleration * jumpHeight);
    private boolean jumpHit = false;
    private final double turnLeft;
    private final double turnRight;
    private int turns = 0;
    public Vector ball;
    public Vector ballDir;
    public Vector startBall;
    public boolean shoot;
    private int[][] map2;
    public Camera(double x, double y, double xd, double yd, double xp, double yp, boolean jump) 
    {   
        //gets starting location and direction
        xPos = x;
        yPos = y;
        xDir = xd;
        yDir = yd;
        xPlane = xp;
        yPlane = yp;
        turnLeft = 0;
        turnRight = 0;
        allowJump = jump;
        ball = new Vector(x, y);
        ballDir = new Vector(xd, yd);
        startBall = new Vector(x, y);
        shoot = false;
    }

    //checks if key is pressed
    public void keyPressed(KeyEvent key)
    {
        if((key.getKeyCode() == KeyEvent.VK_LEFT))
            left = true;
        if((key.getKeyCode() == KeyEvent.VK_RIGHT))
            right = true;
        if((key.getKeyCode() == KeyEvent.VK_UP))
            forward = true;
        if((key.getKeyCode() == KeyEvent.VK_DOWN))
            back = true;
        if((key.getKeyCode() == KeyEvent.VK_SPACE) && allowJump && playerHeight == 0.5 && map2[(int) xPos][(int) yPos] == 0)
        {
            jump = true;
            up = true;
            jumpHit = false;
        }
        else if((key.getKeyCode() == KeyEvent.VK_SPACE) && allowJump && playerHeight == 0.5)
        {
            jump = true;
            up = true;
            jumpHit = true;
        }
        else if((key.getKeyCode() == KeyEvent.VK_SPACE))
        {
            ball.setX(xPos);
            ball.setY(yPos);
            ballDir.setX(xDir/3);
            ballDir.setY(yDir/3);
            startBall.setX(xPos);
            startBall.setY(yPos);
            shoot = true;
        }
    }
    //checks if key is released
    public void keyReleased(KeyEvent key)
    {
        if((key.getKeyCode() == KeyEvent.VK_LEFT))
            left = false;
        if((key.getKeyCode() == KeyEvent.VK_RIGHT))
            right = false;
        if((key.getKeyCode() == KeyEvent.VK_UP))
            forward = false;
        if((key.getKeyCode() == KeyEvent.VK_DOWN))
            back = false;
    }
    //checks if key is typed
    public void keyTyped(KeyEvent key)
    {

    }

    public void update(int[][] map, int[][] map2)
    {
        this.map2 = map2;
        //moves and turns player based on key input
        //uses physics to allow the player to jump
        double MOVE_SPEED = .08;
        int nonSolid = 0;
        if(forward)
        {
            if(playerHeight <= 1)
            {
                if(map[(int)(xPos + xDir * MOVE_SPEED)][(int)yPos] == 0 || map[(int)(xPos + xDir * MOVE_SPEED)][(int)yPos] == nonSolid)
                    xPos+=xDir* MOVE_SPEED;
                if(map[(int)xPos][(int)(yPos + yDir * MOVE_SPEED)] == 0 || map[(int)xPos][(int)(yPos + yDir * MOVE_SPEED)] == nonSolid)
                    yPos+=yDir* MOVE_SPEED;
            }
            else
            {
                if((map[(int)(xPos + xDir * MOVE_SPEED)][(int)yPos] == 0 || map[(int)(xPos + xDir * MOVE_SPEED)][(int)yPos] == nonSolid) && (map2[(int)(xPos + xDir * MOVE_SPEED)][(int)yPos] == 0 || map2[(int)(xPos + xDir * MOVE_SPEED)][(int)yPos] == nonSolid))
                    xPos+=xDir* MOVE_SPEED;
                if((map[(int)xPos][(int)(yPos + yDir * MOVE_SPEED)] == 0 || map[(int)xPos][(int)(yPos + yDir * MOVE_SPEED)] == nonSolid) && (map2[(int)xPos][(int)(yPos + yDir * MOVE_SPEED)] == 0 || map2[(int)xPos][(int)(yPos + yDir * MOVE_SPEED)] == nonSolid))
                    yPos+=yDir* MOVE_SPEED;
            }
        }
        if(back)
        {
            if(playerHeight <= 1)
            {
                if(map[(int)(xPos - xDir * MOVE_SPEED)][(int)yPos] == 0 || map[(int)(xPos - xDir * MOVE_SPEED)][(int)yPos] == nonSolid)
                    xPos-=xDir* MOVE_SPEED;
                if(map[(int)xPos][(int)(yPos - yDir * MOVE_SPEED)] == 0 || map[(int)xPos][(int)(yPos - yDir * MOVE_SPEED)] == nonSolid)
                    yPos-=yDir* MOVE_SPEED;
            }
            else
            {
                if((map[(int)(xPos - xDir * MOVE_SPEED)][(int)yPos] == 0 || map[(int)(xPos - xDir * MOVE_SPEED)][(int)yPos] == nonSolid) && (map2[(int)(xPos - xDir * MOVE_SPEED)][(int)yPos] == 0 || map2[(int)(xPos - xDir * MOVE_SPEED)][(int)yPos] == nonSolid))
                    xPos-=xDir* MOVE_SPEED;
                if((map[(int)xPos][(int)(yPos - yDir * MOVE_SPEED)] == 0 || map[(int)xPos][(int)(yPos - yDir * MOVE_SPEED)] == nonSolid) && (map2[(int)xPos][(int)(yPos - yDir * MOVE_SPEED)] == 0 || map2[(int)xPos][(int)(yPos - yDir * MOVE_SPEED)] == nonSolid))
                    yPos-=yDir* MOVE_SPEED;
            }
        }
        double ROTATION_SPEED = .045;
        if(right)
        {
            double oldxDir = xDir;
            xDir = xDir * Math.cos(-ROTATION_SPEED) - yDir * Math.sin(-ROTATION_SPEED);
            yDir = oldxDir * Math.sin(-ROTATION_SPEED) + yDir * Math.cos(-ROTATION_SPEED);
            double oldxPlane = xPlane;
            xPlane = xPlane * Math.cos(-ROTATION_SPEED) - yPlane * Math.sin(-ROTATION_SPEED);
            yPlane = oldxPlane * Math.sin(-ROTATION_SPEED) + yPlane * Math.cos(-ROTATION_SPEED);
        }
        if(left)
        {
            double oldxDir = xDir;
            xDir = xDir * Math.cos(ROTATION_SPEED) - yDir * Math.sin(ROTATION_SPEED);
            yDir = oldxDir * Math.sin(ROTATION_SPEED) + yDir * Math.cos(ROTATION_SPEED);
            double oldxPlane = xPlane;
            xPlane = xPlane * Math.cos(ROTATION_SPEED) - yPlane * Math.sin(ROTATION_SPEED);
            yPlane =oldxPlane * Math.sin(ROTATION_SPEED) + yPlane * Math.cos(ROTATION_SPEED);
        }
        if(jump)
        {
            if(playerHeight > 0.5 || up)
            {
                playerHeight += jumpSpeed;
                jumpSpeed += jumpAcceleration;
                up = false;
                if(jumpHit && playerHeight >= 0.8)
                {
                    jumpSpeed *= -0.05;
                    jumpHit = false;
                }
            }
            else
            {
                playerHeight = 0.5;
                jumpSpeed = Math.sqrt(-2 * jumpAcceleration * jumpHeight);
                jump = false;
            }
        }
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