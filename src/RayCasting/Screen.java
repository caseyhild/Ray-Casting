import java.util.ArrayList;
import java.util.stream.IntStream;

public class Screen
{
    private int frame;
    private int[][] map;
    private int[][] map2;
    private int[][] floorMap;
    private int[][] ceilingMap;
    private final int mapWidth;
    private final int mapHeight;
    private final int width;
    private final int height;
    private final ArrayList<Texture> textures;
    private final double[] ZBuffer;
    private final ArrayList<PointsFile> files;
    private ArrayList<ArrayList<Vector3D>> points;
    private int shotTimer;
    private final ArrayList<Shot> shots;
    private Vector3D player;
    private int shift;
    private final double[] colored;
    private final Vector3D tempV = new Vector3D();
    private final Vector3D tempCenter = new Vector3D();
    private int fogColor = 0;
    private double[] fog;
    private double fogVisibility = 1.5;

    public Screen(int[][] m, int[][] fm, int[][] cm, int mapW, int mapH, ArrayList<Texture> tex, ArrayList<PointsFile> files, int w, int h)
    {
        this(m, null, fm, cm, mapW, mapH, tex, files, w, h);
    }

    public Screen(int[][] m, int[][] m2, int[][] fm, int[][] cm, int mapW, int mapH, ArrayList<Texture> tex, ArrayList<PointsFile> files, int w, int h)
    {
        frame = 0;
        map = m;
        map2 = m2;
        floorMap = fm;
        ceilingMap = cm;
        mapWidth = mapW;
        mapHeight = mapH;
        textures = tex;
        width = w;
        height = h;
        ZBuffer = new double[width];
        this.files = files;
        shotTimer = 0;
        shots = new ArrayList<>();
        player = new Vector3D();
        shift = 0;
        colored = new double[width * height];
        fog = new double[height * 2];
        for(int y = 0; y < fog.length; y++)
        {
            double currentDist = height / (2.0 * y - height);
            fog[y] = 1 / (1 + Math.exp(-currentDist / fogVisibility + 1));
            if(fog[y] > 0.999) fog[y] = 1;
        }
        fogColor = rgbNum(192, 192, 192);
    }

    public void setFog(int color, double visibility)
    {
        fogColor = color;
        fogVisibility = visibility;
        fog = new double[height * 2];
        for(int y = 0; y < fog.length; y++)
        {
            double currentDist = height / (2.0 * y - height);
            fog[y] = 1 / (1 + Math.exp(-currentDist / visibility + 1));
            if(fog[y] > 0.999) fog[y] = 1;
        }
    }

    // Overload kept for backward compatibility - uses stored map2
    public void updateGame(Camera camera, int[] pixels, int[][] m, int[][] fm, int[][] cm, int f)
    {
        updateGame(camera, pixels, m, map2, fm, cm, f);
    }

    public void updateGame(Camera camera, int[] pixels, int[][] m, int[][] m2, int[][] fm, int[][] cm, int f)
    {
        frame = f;
        map = m;
        map2 = m2;
        floorMap = fm;
        ceilingMap = cm;
        double playerHeight = camera.playerHeight;
        player.x = camera.xPos;
        player.y = camera.yPos;
        player.z = playerHeight;
        final int halfHeight = height / 2;
        final int texSize = textures.getFirst().SIZE;
        final int texMask = texSize - 1;
        double[] depthBuffer = new double[width * height];
        java.util.Arrays.fill(depthBuffer, Double.MAX_VALUE);
        java.util.Arrays.fill(pixels, fogColor);
        java.util.Arrays.fill(colored, 0);

        // shift from mouse look (up/down)
        shift += camera.mouseDeltaY * -1;
        camera.mouseDeltaY = 0;
        shift = Math.max(-height/2, Math.min(shift, height/2));

        // --- PASS 1: Floor + ceiling ---
        IntStream.range(0, width).parallel().forEach(x ->
        {
            double cameraX = 2.0 * x / width - 1.0;
            double rayDirX = camera.xDir + camera.xPlane * cameraX;
            double rayDirY = camera.yDir + camera.yPlane * cameraX;
            // shift the horizon: treat the effective half-height as offset by shift
            int shiftedHalf = halfHeight + shift;
            double phHeight = playerHeight * height;
            double ceilHeight = (2.0 - playerHeight) * height;
            for(int y = 0; y < height; y++)
            {
                // floor: rows below the shifted horizon
                double denomF = y - shiftedHalf + 0.5;
                if(denomF > 0)
                {
                    double rowDistF = phHeight / denomF;
                    double floorX = camera.xPos + rowDistF * rayDirX;
                    double floorY = camera.yPos + rowDistF * rayDirY;
                    floorX = Math.min(Math.max(floorX, 1), mapWidth - 1);
                    floorY = Math.min(Math.max(floorY, 1), mapHeight - 1);
                    int ftx = (int)(floorX * texSize) & texMask;
                    int fty = (int)(floorY * texSize) & texMask;
                    int floorTexture = floorMap[(int)floorX][(int)floorY];
                    int floorIdx = width * y + x;
                    int floorColor = textures.get(floorTexture - 1).pixels[texSize * fty + ftx];
                    if(fog != null && fogColor != 0)
                    {
                        // Use distance-based fog like walls, not screen-space
                        int fogIdx = (int)(height / 2.0 + height / (2.0 * rowDistF));
                        fogIdx = Math.max(0, Math.min(fogIdx, fog.length - 1));
                        double fogAmt = fog[fogIdx];
                        double colAmt = 1.0 - fogAmt;
                        int fogR = fogColor/65536, fogG = fogColor%65536/256, fogB = fogColor%65536%256;
                        int fr = floorColor/65536, fg2 = floorColor%65536/256, fb = floorColor%65536%256;
                        floorColor = Math.min(255,(int)(fogAmt*fogR+colAmt*fr))*65536 + Math.min(255,(int)(fogAmt*fogG+colAmt*fg2))*256 + Math.min(255,(int)(fogAmt*fogB+colAmt*fb));
                    }
                    pixels[floorIdx] = floorColor;
                    depthBuffer[floorIdx] = rowDistF;
                }
                // ceiling: rows above the shifted horizon
                double denomC = shiftedHalf - y + 0.5;
                if(denomC > 0)
                {
                    double rowDistC = ceilHeight / denomC;
                    double ceilX = camera.xPos + rowDistC * rayDirX;
                    double ceilY = camera.yPos + rowDistC * rayDirY;
                    ceilX = Math.min(Math.max(ceilX, 1), mapWidth - 1);
                    ceilY = Math.min(Math.max(ceilY, 1), mapHeight - 1);
                    int ctx = (int)(ceilX * texSize) & texMask;
                    int cty = (int)(ceilY * texSize) & texMask;
                    int ceilTexture = ceilingMap[(int)ceilX][(int)ceilY];
                    int ceilIdx = width * y + x;
                    int ceilingColor = textures.get(ceilTexture - 1).pixels[texSize * cty + ctx];
                    if(fog != null && fogColor != 0)
                    {
                        // Use distance-based fog like walls, not screen-space
                        int fogIdx = (int)(height / 2.0 + height / (2.0 * rowDistC));
                        fogIdx = Math.max(0, Math.min(fogIdx, fog.length - 1));
                        double fogAmt = fog[fogIdx];
                        double colAmt = 1.0 - fogAmt;
                        int fogR = fogColor/65536, fogG = fogColor%65536/256, fogB = fogColor%65536%256;
                        int cr = ceilingColor/65536, cg = ceilingColor%65536/256, cb = ceilingColor%65536%256;
                        ceilingColor = Math.min(255,(int)(fogAmt*fogR+colAmt*cr))*65536 + Math.min(255,(int)(fogAmt*fogG+colAmt*cg))*256 + Math.min(255,(int)(fogAmt*fogB+colAmt*cb));
                    }
                    pixels[ceilIdx] = ceilingColor;
                    depthBuffer[ceilIdx] = rowDistC;
                }
            }
        });

        // Per-column data for horizontal surfaces (top/bottom of 2-story walls)
        double[] colBottomDist = new double[width];
        double[] colTopDist    = new double[width];
        double[] colFloorXWall = new double[width];
        double[] colFloorYWall = new double[width];
        int[]    colHighMapX   = new int[width];
        int[]    colHighMapY   = new int[width];
        int[]    colHighSide   = new int[width];
        double[] colHighWallX  = new double[width];

        // --- PASS 2: Wall sides ---
        IntStream.range(0, width).parallel().forEach(x ->
        {
            double cameraX = 2.0 * x / width - 1.0;
            double rayDirX = camera.xDir + camera.xPlane * cameraX;
            double rayDirY = camera.yDir + camera.yPlane * cameraX;

            int mapX = (int) camera.xPos;
            int mapY = (int) camera.yPos;
            double deltaDistX = Math.abs(1 / rayDirX);
            double deltaDistY = Math.abs(1 / rayDirY);
            double sideDistX, sideDistY;
            int stepX, stepY;
            boolean hit = false;
            int side = 0;

            if(rayDirX < 0) { stepX = -1; sideDistX = (camera.xPos - mapX) * deltaDistX; }
            else             { stepX =  1; sideDistX = (mapX + 1.0 - camera.xPos) * deltaDistX; }
            if(rayDirY < 0) { stepY = -1; sideDistY = (camera.yPos - mapY) * deltaDistY; }
            else             { stepY =  1; sideDistY = (mapY + 1.0 - camera.yPos) * deltaDistY; }

            // Ray cast - lower wall (map)
            while(!hit)
            {
                if(sideDistX < sideDistY) { sideDistX += deltaDistX; mapX += stepX; side = 0; }
                else                       { sideDistY += deltaDistY; mapY += stepY; side = 1; }
                if(mapX < 0 || mapX >= mapWidth || mapY < 0 || mapY >= mapHeight) hit = true;
                else if(map[mapX][mapY] > 0) hit = true;
            }

            double perpWallDist;
            if(side == 0) perpWallDist = (mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX;
            else          perpWallDist = (mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY;
            if(perpWallDist < 0) perpWallDist = -perpWallDist;

            int lineHeight = (perpWallDist > 0) ? (int)(height / perpWallDist) : height;
            int phOffset = (int)(lineHeight * (playerHeight - 0.5));
            // Geometry uses unshifted halfHeight (matches JumpingTest exactly).
            // shift is applied only when writing to the pixel buffer.
            int geoDrawStart = -lineHeight/2 + halfHeight + phOffset;
            int geoDrawEnd   =  lineHeight/2 + halfHeight + phOffset;
            int drawStart = Math.max(0,        geoDrawStart + shift);
            int drawEnd   = Math.min(height-1, geoDrawEnd   + shift);

            double wallX;
            if(side == 0) wallX = camera.yPos + perpWallDist * rayDirY;
            else          wallX = camera.xPos + perpWallDist * rayDirX;
            wallX -= Math.floor(wallX);

            int texNum = (mapX >= 0 && mapY >= 0 && mapX < mapWidth && mapY < mapHeight) ? map[mapX][mapY] - 1 : -1;
            if(texNum >= 0)
            {
                Texture tex = textures.get(texNum);
                int texX = (int)(wallX * tex.SIZE);
                if(side == 0 && rayDirX > 0) texX = tex.SIZE - texX - 1;
                if(side == 1 && rayDirY < 0) texX = tex.SIZE - texX - 1;
                // texY formula uses y-shift to undo the pixel offset, matching JumpingTest's y - halfHeight - phOffset
                double a = 1.0 * tex.SIZE / lineHeight;
                double b = (-height/2.0 + lineHeight/2.0 - phOffset) * a;
                boolean notBlack = tex != Texture.black;
                double wallColorVal = (mapX + wallX - camera.xPos) * (mapX + wallX - camera.xPos) + (mapY - camera.yPos) * (mapY - camera.yPos) - mapWidth * mapHeight;
                for(int y = drawStart; y < drawEnd; y++)
                {
                    int texY = (int)((y - shift) * a + b);
                    if(texY < 0) texY = 0;
                    if(texY >= tex.SIZE) texY = tex.SIZE - 1;
                    int color = tex.pixels[texX + texY * tex.SIZE];
                    if(side == 1) color = (color >> 1) & 8355711;
                    if(fog != null && fogColor != 0)
                    {
                        int fogIdx = (int)(height / 2.0 + height / (2.0 * perpWallDist));
                        fogIdx = Math.max(0, Math.min(fogIdx, fog.length - 1));
                        double fogAmt = fog[fogIdx];
                        double colAmt = 1.0 - fogAmt;
                        int fogR = fogColor/65536, fogG = fogColor%65536/256, fogB = fogColor%65536%256;
                        int cr = color/65536, cg = color%65536/256, cb = color%65536%256;
                        color = Math.min(255,(int)(fogAmt*fogR+colAmt*cr))*65536 + Math.min(255,(int)(fogAmt*fogG+colAmt*cg))*256 + Math.min(255,(int)(fogAmt*fogB+colAmt*cb));
                    }
                    int pidx = x + y * width;
                    if(perpWallDist < depthBuffer[pidx])
                    {
                        pixels[pidx] = color;
                        depthBuffer[pidx] = perpWallDist;
                        if(notBlack) colored[pidx] = wallColorVal;
                    }
                }
            }
            ZBuffer[x] = perpWallDist;

            // Save lower-wall data — use unshifted geo values for clipping
            int lowMapX = mapX, lowMapY = mapY, lowSide = side;
            double lowWallX = wallX, bottomDist = perpWallDist;
            double lowSideDistX = sideDistX, lowSideDistY = sideDistY;
            // oldStart is the unshifted geometry draw start, matching JumpingTest
            int oldStart = geoDrawStart;

            // Ray cast - upper wall (map2) if present
            int highMapX = mapX, highMapY = mapY, highSide = side;
            double highWallX = wallX, topDist = perpWallDist;
            if(map2 != null)
            {
                mapX = (int) camera.xPos; mapY = (int) camera.yPos;
                hit = false; side = 0;
                if(rayDirX < 0) { stepX = -1; sideDistX = (camera.xPos - mapX) * deltaDistX; }
                else             { stepX =  1; sideDistX = (mapX + 1.0 - camera.xPos) * deltaDistX; }
                if(rayDirY < 0) { stepY = -1; sideDistY = (camera.yPos - mapY) * deltaDistY; }
                else             { stepY =  1; sideDistY = (mapY + 1.0 - camera.yPos) * deltaDistY; }

                while(!hit)
                {
                    if(sideDistX < sideDistY) { sideDistX += deltaDistX; mapX += stepX; side = 0; }
                    else                       { sideDistY += deltaDistY; mapY += stepY; side = 1; }
                    if(mapX < 0 || mapX >= mapWidth || mapY < 0 || mapY >= mapHeight) hit = true;
                    else if(map2[mapX][mapY] > 0) hit = true;
                }

                if(side == 0) perpWallDist = (mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX;
                else          perpWallDist = (mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY;
                if(perpWallDist < 0) perpWallDist = -perpWallDist;

                lineHeight = (perpWallDist > 0) ? (int)(height / perpWallDist) : height;
                phOffset = (int)(lineHeight * (playerHeight - 0.5));

                // Geometry without shift — exactly as JumpingTest
                int highGeoDrawEndU = (playerHeight > 1)
                    ? -lineHeight/2 + halfHeight + phOffset
                    : Math.min(oldStart, -lineHeight/2 + halfHeight + phOffset);
                int highGeoDrawStart = highGeoDrawEndU - lineHeight;
                drawStart = Math.max(0,        highGeoDrawStart + shift);
                drawEnd   = Math.max(0, Math.min(height - 1, highGeoDrawEndU + shift));

                if(side == 0) wallX = camera.yPos + perpWallDist * rayDirY;
                else          wallX = camera.xPos + perpWallDist * rayDirX;
                wallX -= Math.floor(wallX);

                texNum = (mapX >= 0 && mapY >= 0 && mapX < mapWidth && mapY < mapHeight) ? map2[mapX][mapY] - 1 : -1;
                if(texNum >= 0)
                {
                    Texture tex = textures.get(texNum);
                    int texX = (int)(wallX * tex.SIZE);
                    if(side == 0 && rayDirX > 0) texX = tex.SIZE - texX - 1;
                    if(side == 1 && rayDirY < 0) texX = tex.SIZE - texX - 1;
                    double a = 1.0 * tex.SIZE / lineHeight;
                    double b = (lineHeight * 3 / 2.0 - height/2.0 - phOffset) * a;
                    boolean notBlack = tex != Texture.black;
                    double wallColorVal = (mapX + wallX - camera.xPos) * (mapX + wallX - camera.xPos) + (mapY - camera.yPos) * (mapY - camera.yPos) - mapWidth * mapHeight;
                    for(int y = drawStart; y < drawEnd; y++)
                    {
                        int texY = (int)((y - shift) * a + b);
                        if(texY < 0) texY = 0;
                        if(texY >= tex.SIZE) texY = tex.SIZE - 1;
                        int color = tex.pixels[texX + texY * tex.SIZE];
                        if(side == 1) color = (color >> 1) & 8355711;
                        if(fog != null && fogColor != 0)
                        {
                            int fogIdx = (int)(height / 2.0 + height / (2.0 * perpWallDist));
                            fogIdx = Math.max(0, Math.min(fogIdx, fog.length - 1));
                            double fogAmt = fog[fogIdx];
                            double colAmt = 1.0 - fogAmt;
                            int fogR = fogColor/65536, fogG = fogColor%65536/256, fogB = fogColor%65536%256;
                            int cr = color/65536, cg = color%65536/256, cb = color%65536%256;
                            color = Math.min(255,(int)(fogAmt*fogR+colAmt*cr))*65536 + Math.min(255,(int)(fogAmt*fogG+colAmt*cg))*256 + Math.min(255,(int)(fogAmt*fogB+colAmt*cb));
                        }
                        int pidx = x + y * width;
                        if(perpWallDist < depthBuffer[pidx])
                        {
                            pixels[pidx] = color;
                            depthBuffer[pidx] = perpWallDist;
                            if(notBlack) colored[pidx] = wallColorVal;
                        }
                    }
                }
                highMapX = mapX; highMapY = mapY; highSide = side;
                highWallX = wallX; topDist = perpWallDist;

                // Additional walls logic (matching JumpingTest)
                int oldEnd = drawEnd;
                boolean stop = false;

                // Additional upper walls (playerHeight <= 1)
                if(playerHeight <= 1)
                {
                    do
                    {
                        hit = false;
                        while(!hit)
                        {
                            if(mapX == 0 || mapY == 0 || mapX == mapWidth-1 || mapY == mapHeight-1) stop = true;
                            else if(sideDistX < sideDistY) { sideDistX += deltaDistX; mapX += stepX; side = 0; }
                            else                            { sideDistY += deltaDistY; mapY += stepY; side = 1; }
                            if(mapX >= mapWidth)  mapX = mapWidth - 1;
                            if(mapY >= mapHeight) mapY = mapHeight - 1;
                            if(mapX == 0 || mapY == 0 || mapX == mapWidth-1 || mapY == mapHeight-1) hit = true;
                            else if(map2[mapX][mapY] > 0) hit = true;
                        }
                        if(side == 0) perpWallDist = (mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX;
                        else          perpWallDist = (mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY;
                        if(perpWallDist < 0) perpWallDist = -perpWallDist;

                        lineHeight = (perpWallDist > 0) ? (int)(height / perpWallDist) : height;
                        phOffset = (int)(lineHeight * (playerHeight - 0.5));

                        int additionalHighGeoDrawEndU = (playerHeight > 1)
                            ? -lineHeight/2 + halfHeight + phOffset
                            : Math.min(oldStart, -lineHeight/2 + halfHeight + phOffset);
                        drawStart = Math.max(0, Math.max(oldEnd, additionalHighGeoDrawEndU - lineHeight + shift));
                        drawEnd   = Math.min(height - 1, Math.min(oldStart + shift, additionalHighGeoDrawEndU + shift));

                        texNum = map2[mapX][mapY] - 1;
                        if(side == 0) wallX = camera.yPos + perpWallDist * rayDirY;
                        else          wallX = camera.xPos + perpWallDist * rayDirX;
                        wallX -= Math.floor(wallX);

                        if(texNum >= 0)
                        {
                            Texture tex = textures.get(texNum);
                            int texX = (int)(wallX * tex.SIZE);
                            if(side == 0 && rayDirX > 0) texX = tex.SIZE - texX - 1;
                            if(side == 1 && rayDirY < 0) texX = tex.SIZE - texX - 1;
                            double a = 1.0 * tex.SIZE / lineHeight;
                            double b = (lineHeight * 3 / 2.0 - height/2.0 - phOffset) * a;
                            boolean notBlack = tex != Texture.black;
                            double wallColorVal = (mapX + wallX - camera.xPos) * (mapX + wallX - camera.xPos) + (mapY - camera.yPos) * (mapY - camera.yPos) - mapWidth * mapHeight;
                            for(int y = drawStart; y < drawEnd; y++)
                            {
                                int texY = (int)((y - shift) * a + b);
                                if(texY < 0) texY = 0;
                                if(texY >= tex.SIZE) texY = tex.SIZE - 1;
                                int color = tex.pixels[texX + texY * tex.SIZE];
                                if(side == 1) color = (color >> 1) & 8355711;
                                if(fog != null && fogColor != 0)
                                {
                                    int fogIdx = (int)(height / 2.0 + height / (2.0 * perpWallDist));
                                    fogIdx = Math.max(0, Math.min(fogIdx, fog.length - 1));
                                    double fogAmt = fog[fogIdx];
                                    double colAmt = 1.0 - fogAmt;
                                    int fogR = fogColor/65536, fogG = fogColor%65536/256, fogB = fogColor%65536%256;
                                    int cr = color/65536, cg = color%65536/256, cb = color%65536%256;
                                    color = Math.min(255,(int)(fogAmt*fogR+colAmt*cr))*65536 + Math.min(255,(int)(fogAmt*fogG+colAmt*cg))*256 + Math.min(255,(int)(fogAmt*fogB+colAmt*cb));
                                }
                                int pidx = x + y * width;
                                if(perpWallDist < depthBuffer[pidx])
                                {
                                    pixels[pidx] = color;
                                    depthBuffer[pidx] = perpWallDist;
                                    if(notBlack) colored[pidx] = wallColorVal;
                                }
                            }
                        }
                    }while(drawStart < oldStart + shift && !stop);
                }
                else
                {
                    // Additional lower walls (playerHeight > 1)
                    mapX = lowMapX; mapY = lowMapY;
                    sideDistX = lowSideDistX; sideDistY = lowSideDistY;
                    int drawStartInit = height;
                    do
                    {
                        hit = false;
                        while(!hit)
                        {
                            if(mapX == 0 || mapY == 0 || mapX == mapWidth-1 || mapY == mapHeight-1) stop = true;
                            else if(sideDistX < sideDistY) { sideDistX += deltaDistX; mapX += stepX; side = 0; }
                            else                            { sideDistY += deltaDistY; mapY += stepY; side = 1; }
                            if(mapX >= mapWidth)  mapX = mapWidth - 1;
                            if(mapY >= mapHeight) mapY = mapHeight - 1;
                            if(mapX == 0 || mapY == 0 || mapX == mapWidth-1 || mapY == mapHeight-1) hit = true;
                            else if(map[mapX][mapY] > 0) hit = true;
                        }
                        if(side == 0) perpWallDist = (mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX;
                        else          perpWallDist = (mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY;
                        if(perpWallDist < 0) perpWallDist = -perpWallDist;

                        lineHeight = (perpWallDist > 0) ? (int)(height / perpWallDist) : height;
                        phOffset = (int)(lineHeight * (playerHeight - 0.5));

                        int additionalGeoDrawStart = -lineHeight/2 + halfHeight + phOffset;
                        drawEnd   = Math.min(height - 1, Math.min(oldStart + shift, drawStartInit));
                        drawStart = Math.max(0, Math.max(oldEnd + shift, additionalGeoDrawStart + shift));

                        texNum = map[mapX][mapY] - 1;
                        if(side == 0) wallX = camera.yPos + perpWallDist * rayDirY;
                        else          wallX = camera.xPos + perpWallDist * rayDirX;
                        wallX -= Math.floor(wallX);

                        if(texNum >= 0)
                        {
                            Texture tex = textures.get(texNum);
                            int texX = (int)(wallX * tex.SIZE);
                            if(side == 0 && rayDirX > 0) texX = tex.SIZE - texX - 1;
                            if(side == 1 && rayDirY < 0) texX = tex.SIZE - texX - 1;
                            double a = 1.0 * tex.SIZE / lineHeight;
                            double b = (-height/2.0 + lineHeight/2.0 - phOffset) * a;
                            boolean notBlack = tex != Texture.black;
                            double wallColorVal = (mapX + wallX - camera.xPos) * (mapX + wallX - camera.xPos) + (mapY - camera.yPos) * (mapY - camera.yPos) - mapWidth * mapHeight;
                            for(int y = drawStart; y < drawEnd; y++)
                            {
                                int texY = (int)((y - shift) * a + b);
                                if(texY < 0) texY = 0;
                                if(texY >= tex.SIZE) texY = tex.SIZE - 1;
                                int color = tex.pixels[texX + texY * tex.SIZE];
                                if(side == 1) color = (color >> 1) & 8355711;
                                if(fog != null && fogColor != 0)
                                {
                                    int fogIdx = (int)(height / 2.0 + height / (2.0 * perpWallDist));
                                    fogIdx = Math.max(0, Math.min(fogIdx, fog.length - 1));
                                    double fogAmt = fog[fogIdx];
                                    double colAmt = 1.0 - fogAmt;
                                    int fogR = fogColor/65536, fogG = fogColor%65536/256, fogB = fogColor%65536%256;
                                    int cr = color/65536, cg = color%65536/256, cb = color%65536%256;
                                    color = Math.min(255,(int)(fogAmt*fogR+colAmt*cr))*65536 + Math.min(255,(int)(fogAmt*fogG+colAmt*cg))*256 + Math.min(255,(int)(fogAmt*fogB+colAmt*cb));
                                }
                                int pidx = x + y * width;
                                if(perpWallDist < depthBuffer[pidx])
                                {
                                    pixels[pidx] = color;
                                    depthBuffer[pidx] = perpWallDist;
                                    if(notBlack) colored[pidx] = wallColorVal;
                                }
                            }
                        }
                    }while(drawEnd > oldEnd + shift && !stop);
                }
            }

            // Store per-column data for horizontal surface pass
            {
                int s = lowSide; double lw = lowWallX;
                double fxw, fyw;
                if(s == 0 && rayDirX > 0)      { fxw = lowMapX;       fyw = lowMapY + lw; }
                else if(s == 0 && rayDirX < 0) { fxw = lowMapX + 1.0; fyw = lowMapY + lw; }
                else if(s == 1 && rayDirY > 0) { fxw = lowMapX + lw;  fyw = lowMapY;      }
                else                            { fxw = lowMapX + lw;  fyw = lowMapY + 1.0; }
                colBottomDist[x] = bottomDist;
                colTopDist[x]    = topDist;
                colFloorXWall[x] = fxw;
                colFloorYWall[x] = fyw;
                colHighMapX[x]   = highMapX; colHighMapY[x] = highMapY;
                colHighSide[x]   = highSide; colHighWallX[x] = highWallX;
            }
        });

        // --- PASS 3: Horizontal surfaces (top of lower walls / bottom of upper walls) ---
        // y is the unshifted distance-sampling variable (world-space projection).
        // Pixel writes are offset by shift so surfaces stay fixed in the world.
        if(map2 != null)
        {
            IntStream.range(0, width).parallel().forEach(x ->
            {
                double cameraX = 2.0 * x / width - 1.0;
                double rayDirX = camera.xDir + camera.xPlane * cameraX;
                double rayDirY = camera.yDir + camera.yPlane * cameraX;
                double distWall   = colBottomDist[x];
                double floorXWall = colFloorXWall[x];
                double floorYWall = colFloorYWall[x];

                // Bottom of upper walls (playerHeight < 1)
                // Loop covers full screen height so close-up surfaces aren't clipped.
                // weight2 > 1 means the sample point is beyond the wall; clamp to wall edge instead of skipping.
                // Bottom of upper walls (playerHeight < 1)
                // Iterate over screen rows directly, back-calculate the unshifted y for projection.
                if(playerHeight < 1)
                {
                    double factor = (1.0 - playerHeight) * height;
                    for(int botRow = 0; botRow < height; botRow++)
                    {
                        // botRow = height - y + shift  =>  y = height - botRow + shift
                        int y = height - botRow + shift;
                        double d = y - halfHeight;
                        if(d <= 0.5) d = 0.5;
                        double currentDist2 = factor / d;
                        double weight2 = currentDist2 / distWall;
                        if(weight2 > 1.0) continue;
                        double bx = weight2 * floorXWall + (1.0 - weight2) * camera.xPos;
                        double by = weight2 * floorYWall + (1.0 - weight2) * camera.yPos;
                        bx = Math.min(Math.max(bx, 1), mapWidth - 1);
                        by = Math.min(Math.max(by, 1), mapHeight - 1);
                        int bt = map2[(int)bx][(int)by];
                        if(bt != 0 && map[(int)bx][(int)by] == 0)
                        {
                            int btx = (int)(bx * texSize) & texMask;
                            int bty = (int)(by * texSize) & texMask;
                            int bc = textures.get(bt - 1).pixels[texSize * bty + btx];
                            bc = rgbNum((getR(bc) * 3) >> 2, (getG(bc) * 3) >> 2, (getB(bc) * 3) >> 2);
                            if(fog != null && fogColor != 0)
                            {
                                int fogIdx = (int)(height / 2.0 + height / (2.0 * currentDist2));
                                fogIdx = Math.max(0, Math.min(fogIdx, fog.length - 1));
                                double fogAmt = fog[fogIdx];
                                double colAmt = 1.0 - fogAmt;
                                int fogR = fogColor/65536, fogG = fogColor%65536/256, fogB = fogColor%65536%256;
                                int br = bc/65536, bg = bc%65536/256, bb = bc%65536%256;
                                bc = Math.min(255,(int)(fogAmt*fogR+colAmt*br))*65536 + Math.min(255,(int)(fogAmt*fogG+colAmt*bg))*256 + Math.min(255,(int)(fogAmt*fogB+colAmt*bb));
                            }
                            int botIdx = width * botRow + x;
                            pixels[botIdx] = bc;
                            depthBuffer[botIdx] = currentDist2;
                        }
                    }
                }

                // Top of lower walls (playerHeight > 1)
                // Iterate over screen rows directly, back-calculate the unshifted y for projection.
                if(playerHeight > 1)
                {
                    int hs = colHighSide[x];
                    double hw = colHighWallX[x];
                    int hmx = colHighMapX[x], hmy = colHighMapY[x];
                    double cxw, cyw;
                    if(hs == 0 && rayDirX > 0)      { cxw = hmx;       cyw = hmy + hw; }
                    else if(hs == 0 && rayDirX < 0) { cxw = hmx + 1.0; cyw = hmy + hw; }
                    else if(hs == 1 && rayDirY > 0) { cxw = hmx + hw;  cyw = hmy;      }
                    else                             { cxw = hmx + hw;  cyw = hmy + 1.0; }
                    double distWallTop = colTopDist[x];
                    double factor = (playerHeight - 1.0) * height;
                    for(int topRow = 0; topRow < height; topRow++)
                    {
                        // topRow = y + shift  =>  y = topRow - shift
                        int y = topRow - shift;
                        double d = y - halfHeight;
                        if(d <= 0.5) d = 0.5;
                        double currentDist2 = factor / d;
                        double weight2 = currentDist2 / distWallTop;
                        if(weight2 > 1.0) continue;
                        double tx = weight2 * cxw + (1.0 - weight2) * camera.xPos;
                        double ty = weight2 * cyw + (1.0 - weight2) * camera.yPos;
                        tx = Math.min(Math.max(tx, 1), mapWidth - 1);
                        ty = Math.min(Math.max(ty, 1), mapHeight - 1);
                        int tt = map[(int)tx][(int)ty];
                        if(tt != 0 && map2[(int)tx][(int)ty] == 0)
                        {
                            int ttx = (int)(tx * texSize) & texMask;
                            int tty = (int)(ty * texSize) & texMask;
                            int tc = textures.get(tt - 1).pixels[texSize * tty + ttx];
                            tc = rgbNum((getR(tc) * 3) >> 2, (getG(tc) * 3) >> 2, (getB(tc) * 3) >> 2);
                            if(fog != null && fogColor != 0)
                            {
                                int fogIdx = (int)(height / 2.0 + height / (2.0 * currentDist2));
                                fogIdx = Math.max(0, Math.min(fogIdx, fog.length - 1));
                                double fogAmt = fog[fogIdx];
                                double colAmt = 1.0 - fogAmt;
                                int fogR = fogColor/65536, fogG = fogColor%65536/256, fogB = fogColor%65536%256;
                                int tr = tc/65536, tg = tc%65536/256, tb = tc%65536%256;
                                tc = Math.min(255,(int)(fogAmt*fogR+colAmt*tr))*65536 + Math.min(255,(int)(fogAmt*fogG+colAmt*tg))*256 + Math.min(255,(int)(fogAmt*fogB+colAmt*tb));
                            }
                            int topIdx = width * topRow + x;
                            pixels[topIdx] = tc;
                            depthBuffer[topIdx] = currentDist2;
                        }
                    }
                }
            });
        }

        // --- 3D points ---
        double px;
        double py;
        double pz;
        int pcolor;
        for(int i = 0; i < points.size(); i++)
        {
            PointsFile file = files.get(i);
            for(int ctr = 0; ctr < file.length; ctr++)
            {
                px = file.x[ctr];
                py = file.y[ctr];
                pz = file.z[ctr];
                pcolor = file.color[ctr];
                for(int j = 0; j < points.get(i).size(); j++)
                {
                    px += points.get(i).get(j).getX();
                    py += points.get(i).get(j).getY();
                    pz += points.get(i).get(j).getZ();
                    if(i == 1)
                    {
                        tempV.x = px; tempV.y = py; tempV.z = pz;
                        tempCenter.x = points.get(i).get(j).getX();
                        tempCenter.y = points.get(i).get(j).getY();
                        tempCenter.z = points.get(i).get(j).getZ();
                        tempV.rotateZ3D(frame/5.0, tempCenter);
                        px = tempV.x;
                        py = tempV.y;
                        pz = tempV.z;
                    }
                    double pointX = px - camera.xPos;
                    double pointY = py - camera.yPos;
                    double pointZ = pz - playerHeight;
                    double invDet = camera.xPlane * camera.yDir - camera.xDir * camera.yPlane;
                    double transformX = invDet * (camera.yDir * pointX - camera.xDir * pointY);
                    double transformY = invDet * (-camera.yPlane * pointX + camera.xPlane * pointY);
                    int pointScreenX = (int)(width/2 * (1 + transformX/transformY));
                    int pointHeight = (int)(height/transformY);
                    int drawStartY = -pointHeight/64 + height/2 + shift - (int)(3 * pointHeight/8 * pointZ);
                    if(drawStartY < 0) drawStartY = 0;
                    int drawEndY = pointHeight/64 + height/2 + shift - (int)(3 * pointHeight/8 * pointZ);
                    if(drawEndY >= height) drawEndY = height - 1;
                    int drawStartX = -pointHeight/64 + pointScreenX;
                    if(drawStartX < 0) drawStartX = 0;
                    int drawEndX = pointHeight/64 + pointScreenX;
                    if(drawEndX >= width) drawEndX = width - 1;
                    for(int drawX = drawStartX; drawX <= drawEndX; drawX++)
                    {
                        if(transformY > 0)
                        {
                            double spriteDist = Math.sqrt(pointX * pointX + pointY * pointY);
                            int fogIdx = (int)(height / 2.0 + height / (2.0 * spriteDist));
                            fogIdx = Math.max(0, Math.min(fogIdx, fog.length - 1));
                            double fogAmt = (fog != null && fogColor != 0) ? fog[fogIdx] : 0;
                            double colAmt = 1.0 - fogAmt;
                            int fogR = fogColor/65536, fogG = fogColor%65536/256, fogB = fogColor%65536%256;
                            for(int drawY = drawStartY; drawY <= drawEndY; drawY++)
                            {
                                int pidx = Math.max(0, Math.min(drawY, height - 1)) * width + drawX;
                                // Only draw if sprite is closer than everything at this pixel
                                if(spriteDist < depthBuffer[pidx])
                                {
                                    int c = pcolor;
                                    if(fogAmt > 0)
                                    {
                                        int cr = c/65536, cg = c%65536/256, cb = c%65536%256;
                                        c = Math.min(255,(int)(fogAmt*fogR+colAmt*cr))*65536 + Math.min(255,(int)(fogAmt*fogG+colAmt*cg))*256 + Math.min(255,(int)(fogAmt*fogB+colAmt*cb));
                                    }
                                    pixels[pidx] = c;
                                    depthBuffer[pidx] = spriteDist;
                                }
                            }
                        }
                    }
                    if(i == 1)
                    {
                        tempV.x = px; tempV.y = py; tempV.z = pz;
                        tempCenter.x = points.get(i).get(j).getX();
                        tempCenter.y = points.get(i).get(j).getY();
                        tempCenter.z = points.get(i).get(j).getZ();
                        tempV.rotateZ3D(-frame/5.0, tempCenter);
                        px = tempV.x;
                        py = tempV.y;
                        pz = tempV.z;
                    }
                    px -= points.get(i).get(j).getX();
                    py -= points.get(i).get(j).getY();
                    pz -= points.get(i).get(j).getZ();
                }
            }
        }

        // --- Shots ---
        if(camera.shoot && shotTimer >= 30)
        {
            double spawnZ = playerHeight + shift / (height * 0.44);
            double zVel = (double) shift / (height / 2.0) / 30.0 * (3.0 * height / (2.0 * width));
            shots.add(new Shot(player.getX() + camera.xDir, player.getY() + camera.yDir, spawnZ, camera.xDir/30, camera.yDir/30, zVel));
            shotTimer = 0;
        }
        for(int i = 0; i < shots.size(); i++)
        {
            Shot shot = shots.get(i);
            boolean inBounds = shot.pos.getX() >= 0 && shot.pos.getX() < mapWidth && shot.pos.getY() >= 0 && shot.pos.getY() < mapHeight;
            boolean hitWall = inBounds && map[(int) shot.pos.getX()][(int) shot.pos.getY()] != 0;
            boolean hitFloor = shot.pos.getZ() < -0.1;
            boolean hitCeiling = false;
            if(shot.prevPos.getZ() < 1.0 && shot.pos.getZ() >= 1.0)
            {
                double t = (1.0 - shot.prevPos.getZ()) / (shot.pos.getZ() - shot.prevPos.getZ());
                double crossX = shot.prevPos.getX() + t * (shot.pos.getX() - shot.prevPos.getX());
                double crossY = shot.prevPos.getY() + t * (shot.pos.getY() - shot.prevPos.getY());
                int tx = (int) crossX;
                int ty = (int) crossY;
                if(tx >= 0 && tx < mapWidth && ty >= 0 && ty < mapHeight && textures.get(ceilingMap[tx][ty] - 1) != Texture.black)
                    hitCeiling = true;
            }
            if(!shot.pos.equals(shot.initialPos) && inBounds && !hitWall && !hitFloor && !hitCeiling)
            {
                shot.prevPos.x = shot.pos.getX();
                shot.prevPos.y = shot.pos.getY();
                shot.prevPos.z = shot.pos.getZ();
                shot.pos.add(shot.dir);
            }
            else if(!shot.pos.equals(shot.initialPos))
            {
                shots.remove(i);
                i--;
            }
            else
            {
                shot.prevPos.x = shot.pos.getX();
                shot.prevPos.y = shot.pos.getY();
                shot.prevPos.z = shot.pos.getZ();
                shot.pos.add(shot.dir);
            }
            if(!shot.pos.equals(shot.initialPos))
            {
                px = shot.pos.getX();
                py = shot.pos.getY();
                pz = shot.pos.getZ();
                pcolor = rgbNum(255, 0, 0);
                double pointX = px - camera.xPos;
                double pointY = py - camera.yPos;
                double pointZ = pz - playerHeight;
                double invDet = (camera.xPlane * camera.yDir - camera.xDir * camera.yPlane);
                double transformX = invDet * (camera.yDir * pointX - camera.xDir * pointY);
                double transformY = invDet * (-camera.yPlane * pointX + camera.xPlane * pointY);
                int pointScreenX = (int)((width/2) * (1 + transformX/transformY));
                int pointHeight = Math.abs((int)(height/transformY));
                pointHeight /= 4;
                pointHeight += 8;
                pointHeight = Math.max(32, Math.min(pointHeight, 512));
                int drawStartY = -pointHeight/32 + height/2 + shift - (int) Math.round(3.0 * pointHeight/4 * pointZ);
                if(drawStartY < 0) drawStartY = 0;
                int drawEndY = pointHeight/32 + height/2 + shift - (int) Math.round(3.0 * pointHeight/4 * pointZ);
                if(drawEndY >= height) drawEndY = height - 1;
                int pointWidth = Math.abs((int)(height/transformY));
                pointWidth /= 4;
                pointWidth += 8;
                pointWidth = Math.max(32, Math.min(pointWidth, 512));
                int drawStartX = -pointWidth/32 + pointScreenX;
                if(drawStartX <= 0) drawStartX = 0;
                int drawEndX = pointWidth/32 + pointScreenX;
                if(drawEndX >= width) drawEndX = width - 1;
                for(int drawX = drawStartX; drawX <= drawEndX; drawX++)
                {
                    if(transformY > 0 && drawX > 0 && drawX < width && transformY < ZBuffer[drawX])
                    {
                        double shotDist = Math.sqrt(pointX * pointX + pointY * pointY);
                        int fogIdx = (int)(height / 2.0 + height / (2.0 * shotDist));
                        fogIdx = Math.max(0, Math.min(fogIdx, fog.length - 1));
                        int shotColor = pcolor;
                        if(fog != null && fogColor != 0)
                        {
                            double fogAmt = fog[fogIdx];
                            double colAmt = 1.0 - fogAmt;
                            int fogR = fogColor/65536, fogG = fogColor%65536/256, fogB = fogColor%65536%256;
                            int cr = shotColor/65536, cg = shotColor%65536/256, cb = shotColor%65536%256;
                            shotColor = Math.min(255,(int)(fogAmt*fogR+colAmt*cr))*65536 + Math.min(255,(int)(fogAmt*fogG+colAmt*cg))*256 + Math.min(255,(int)(fogAmt*fogB+colAmt*cb));
                        }
                        for(int drawY = drawStartY; drawY <= drawEndY; drawY++)
                        {
                            int pidx = Math.max(0, Math.min(drawY, height - 1)) * width + drawX;
                            // Check against depthBuffer to respect horizontal surfaces
                            if(shotDist < depthBuffer[pidx])
                            {
                                pixels[pidx] = shotColor;
                            }
                        }
                    }
                }
            }
        }
        camera.shoot = false;
        shotTimer++;
        // --- Crosshair ---
        int cx = width / 2;
        int cy = height / 2;
        int crossColor = 0x000000;
        for(int y = cy - 11; y < cy - 3; y++) if(y >= 0 && y < height)
            for(int dx = -2; dx < 2; dx++) if(cx+dx >= 0 && cx+dx < width) pixels[(cx+dx) + y * width] = crossColor;
        for(int y = cy + 3; y < cy + 11; y++) if(y >= 0 && y < height)
            for(int dx = -2; dx < 2; dx++) if(cx+dx >= 0 && cx+dx < width) pixels[(cx+dx) + y * width] = crossColor;
        for(int x = cx - 11; x < cx - 3; x++) if(x >= 0 && x < width)
            for(int dy = -2; dy < 2; dy++) if(cy+dy >= 0 && cy+dy < height) pixels[x + (cy+dy) * width] = crossColor;
        for(int x = cx + 3; x < cx + 11; x++) if(x >= 0 && x < width)
            for(int dy = -2; dy < 2; dy++) if(cy+dy >= 0 && cy+dy < height) pixels[x + (cy+dy) * width] = crossColor;
    }

    public void setPoints(ArrayList<ArrayList<Vector3D>> p)
    {
        points = p;
    }

    private int rgbNum(int r, int g, int b)
    {
        return r * 65536 + g * 256 + b;
    }

    private int getR(int color)
    {
        return color >> 16;
    }

    private int getG(int color)
    {
        return (color >> 8) & 0xFF;
    }

    private int getB(int color)
    {
        return color & 0xFF;
    }
}
