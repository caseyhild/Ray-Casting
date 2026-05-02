public class Vector3D
{
    public double x;
    public double y;
    public double z;

    public Vector3D()
    {
        x = 0; 
        y = 0;
        z = 0;
    }

    public Vector3D(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }
    
    public double getZ()
    {
        return z;
    }
    public void add(Vector3D v)
    {
        x += v.x;
        y += v.y;
        z += v.z;
    }

    public double dist(Vector3D v)
    {
        return Math.sqrt((x - v.x) * (x - v.x) + (y - v.y) * (y - v.y) + (z - v.z) * (z - v.z));
    }

    public void rotateZ3D(double theta, Vector3D origin) 
    {
        double sinTheta = Math.sin(Math.toRadians(theta));
        double cosTheta = Math.cos(Math.toRadians(theta));
        double xCopy = x - origin.x;
        double yCopy = y - origin.y;
        x = origin.x + xCopy * cosTheta - yCopy * sinTheta;
        y = origin.y + yCopy * cosTheta + xCopy * sinTheta;
    }

    public boolean equals(Vector3D v)
    {
        return x == v.x && y == v.y && z == v.z;
    }

    public String toString()
    {
        return x + " " + y + " " + z;
    }
}