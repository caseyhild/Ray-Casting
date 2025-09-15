public class Sprite
{
    public double xPos;
    public double yPos;
    public Texture texture;
    public Sprite(double x, double y, Texture tex)
    {
        // creates a sprite with coordinates (x,y) and a texture tex
        xPos = x;
        yPos = y;
        texture = tex;
    }
}