public class Vector2D
{
    public double x;
    public double y;

    public Vector2D(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public void add(Vector2D v)
    {
        x += v.x;
        y += v.y;
    }

    public void sub(Vector2D v)
    {
        x -= v.x;
        y -= v.y;
    }

    public void mult(double n)
    {
        x *= n;
        y *= n;
    }

    public void div(double n)
    {
        x /= n;
        y /= n;
    }

    public double mag()
    {
        return Math.sqrt(x * x + y * y);
    }

    public void normalize()
    {
        if(mag() > 0)
            div(mag());
    }

    public String toString()
    {
        return x + " " + y;
    }
}