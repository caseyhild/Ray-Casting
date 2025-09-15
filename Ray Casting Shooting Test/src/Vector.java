public class Vector
{
    private double x;
    private double y;
    public Vector(double x, double y)
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

    public static Vector sub(Vector v1, Vector v2)
    {
        Vector v = new Vector(0, 0);
        v.x = v1.x - v2.x;
        v.y = v1.y - v2.y;
        return v;
    }

    public void mult(double n)
    {
        this.x *= n;
        this.y *= n;
    }

    public void div(double n)
    {
        this.x /= n;
        this.y /= n;
    }

    public double mag()
    {
        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }

    public void normalize()
    {
        if(mag() > 0)
            this.div(this.mag());
    }

    public String toString()
    {
        return x + " , " + y;
    }
}