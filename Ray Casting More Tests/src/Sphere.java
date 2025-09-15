import java.io.*;
public class Sphere
{
    public Sphere(PrintWriter outFile)
    {
        this(outFile, 0, 0, 0);
    }
    
    public Sphere(PrintWriter outFile, double xCenter, double yCenter, double zCenter)
    {
        // color sphere
        for(double x = -0.5; x <= 0.5; x += 0.05)
        {
            for(double y = -0.5; y <= 0.5; y += 0.05)
            {
                for(double z = -0.5; z <= 0.5; z += 0.05)
                {
                    double squaredMagnitude = x * x + y * y + z * z;
                    if(squaredMagnitude <= 0.275 && squaredMagnitude >= 0.21)
                    {
                        outFile.printf("%.4f", (x + xCenter));
                        outFile.print("  ");
                        outFile.printf("%.4f", (y + yCenter));
                        outFile.print("  ");
                        outFile.printf("%.4f", (1.1 * z + zCenter - 0.05));
                        outFile.print("  ");
                        outFile.println(rgbNum((int) (Math.abs(x) * 510), (int) (Math.abs(y) * 510), (int) (Math.abs(z) * 510)));
                    }
                }
            }
        }
        outFile.close();
    }

    private int rgbNum(int r, int g, int b)
    {
        //gets rgb decimal value from rgb input
        return r * 65536 + g * 256 + b;
    }
}