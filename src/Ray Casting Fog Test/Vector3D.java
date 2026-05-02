public class Vector3D
{
    public double x;
    public double y;
    public double z;

    public Vector3D(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vector3D(Vector3D v)
    {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public String toString()
    {
        return x + " " + y + " " + z;
    }
}