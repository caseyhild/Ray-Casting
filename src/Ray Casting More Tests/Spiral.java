import java.io.*;
public class Spiral
{
    public Spiral(PrintWriter outFile)
    {
        int numVertical = 10;
        int numAround = 6;
        double radius = 0.2;
        double height = 0.6;
        double twistAngle = 120;
        double x, y, z;
        for(double deg = 0; deg < 360; deg += 360.0/numAround)
        {
            for(int i = 0; i < numVertical; i++)
            {
                double angle = Math.toRadians(deg + i * twistAngle / numVertical);
                x = radius * Math.cos(angle);
                y = radius * Math.sin(angle);
                z = -height/2 + height * i/(numVertical - 1);
                outFile.printf("%.4f", x);
                outFile.print("  ");
                outFile.printf("%.4f", y);
                outFile.print("  ");
                outFile.printf("%.4f", z);
                outFile.print("  ");
                outFile.println(rgbNum(128 + (int) (128 * z/height), 0, 0));
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