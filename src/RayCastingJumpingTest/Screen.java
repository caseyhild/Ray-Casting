import java.util.ArrayList;
public class Screen 
{
    private int[][] map;
    private int[][] map2;
    private int[][] floorMap;
    private int[][] ceilingMap;
    private final int mapWidth;
    private final int mapHeight;
    private final int width;
    private final int height;
    private final ArrayList<Texture> textures;

    public Screen(int[][] m, int[][]m2, int[][] fm, int[][] cm, int mapW, int mapH, ArrayList<Texture> tex, int w, int h)
    {
        map = m;
        map2 = m2;
        floorMap = fm;
        ceilingMap = cm;
        mapWidth = mapW;
        mapHeight = mapH;
        textures = tex;
        width = w;
        height = h;
    }

    public void updateGame(Camera camera, int[] pixels, int[][] m, int[][] m2, int[][] fm, int[][] cm, double ph)
    {
        map = m;
        map2 = m2;
        floorMap = fm;
        ceilingMap = cm;

        // Cache frequently used values
        final int halfHeight = height / 2;
        final double phHeight = ph * height;
        final double ceilHeight = (2.0 - ph) * height;
        final Texture floorTex = textures.getFirst();
        final int texSize = floorTex.SIZE;
        final int texMask = texSize - 1; // works if SIZE is power of 2

        // Depth buffer
        double[] depthBuffer = new double[width * height];

        // --- PASS 1: Floor + ceiling + depth init in one loop ---
        for(int x = 0; x < width; x++)
        {
            double cameraX = 2.0 * x / width - 1.0;
            double rayDirX = camera.xDir + camera.xPlane * cameraX;
            double rayDirY = camera.yDir + camera.yPlane * cameraX;
            for(int y = halfHeight; y < height; y++)
            {
                double denom = y - halfHeight + 0.5;
                double rowDistF = phHeight / denom;
                double rowDistC = ceilHeight / denom;

                // floor
                double floorX = camera.xPos + rowDistF * rayDirX;
                double floorY = camera.yPos + rowDistF * rayDirY;
                floorX = Math.min(Math.max(floorX, 1), mapWidth - 1);
                floorY = Math.min(Math.max(floorY, 1), mapHeight - 1);
                int ftx = (int)(floorX * texSize) & texMask;
                int fty = (int)(floorY * texSize) & texMask;
                int floorTexture = floorMap[(int)floorX][(int)floorY];
                int floorIdx = width * y + x;
                pixels[floorIdx] = textures.get(floorTexture - 1).pixels[texSize * fty + ftx];
                depthBuffer[floorIdx] = rowDistF;

                // ceiling (mirrored)
                int ceilRow = height - y;
                if(ceilRow >= 0 && ceilRow < height && rowDistC > 0)
                {
                    double ceilX = camera.xPos + rowDistC * rayDirX;
                    double ceilY = camera.yPos + rowDistC * rayDirY;
                    ceilX = Math.min(Math.max(ceilX, 1), mapWidth - 1);
                    ceilY = Math.min(Math.max(ceilY, 1), mapHeight - 1);
                    int ctx = (int)(ceilX * texSize) & texMask;
                    int cty = (int)(ceilY * texSize) & texMask;
                    int ceilTexture = ceilingMap[(int)ceilX][(int)ceilY];
                    int ceilIdx = width * ceilRow + x;
                    pixels[ceilIdx] = textures.get(ceilTexture - 1).pixels[texSize * cty + ctx];
                    depthBuffer[ceilIdx] = rowDistC;
                }
            }
        }

        int texX, texY;
        int floorStart = height;

        // Per-column data for horizontal surfaces (computed during wall loop)
        double[] colBottomDist = new double[width];
        double[] colTopDist    = new double[width];
        double[] colFloorXWall = new double[width];
        double[] colFloorYWall = new double[width];
        int[]    colHighMapX   = new int[width];
        int[]    colHighMapY   = new int[width];
        int[]    colHighSide   = new int[width];
        double[] colHighWallX  = new double[width];

        // --- PASS 2: Wall sides ---
        for(int x = 0; x < width; x++)
        {
            double cameraX = 2.0 * x / width - 1.0;
            double rayDirX = camera.xDir + camera.xPlane * cameraX;
            double rayDirY = camera.yDir + camera.yPlane * cameraX;

            // DDA setup
            int mapX = (int) camera.xPos;
            int mapY = (int) camera.yPos;
            double dx = Math.sqrt(1 + (rayDirY * rayDirY) / (rayDirX * rayDirX));
            double dy = Math.sqrt(1 + (rayDirX * rayDirX) / (rayDirY * rayDirY));
            double deltaDistX = dx, deltaDistY = dy;
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
                if(!(mapX == 0 || mapY == 0 || mapX == mapWidth-1 || mapY == mapHeight-1))
                {
                    if(sideDistX < sideDistY) { sideDistX += deltaDistX; mapX += stepX; side = 0; }
                    else                       { sideDistY += deltaDistY; mapY += stepY; side = 1; }
                }
                if(mapX >= mapWidth)  mapX = mapWidth - 1;
                if(mapY >= mapHeight) mapY = mapHeight - 1;
                if(mapX == 0 || mapY == 0 || mapX == mapWidth-1 || mapY == mapHeight-1) hit = true;
                else if(map[mapX][mapY] > 0) hit = true;
            }

            double perpWallDist = (side == 0)
                ? Math.abs((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX)
                : Math.abs((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY);

            int lineHeight = (perpWallDist > 0) ? Math.abs((int)(height / perpWallDist)) : height;
            int phOffset = (int)(lineHeight * (ph - 0.5));
            int drawStart = Math.max(0, -lineHeight/2 + halfHeight + phOffset);
            int drawEnd   = Math.min(height - 1, lineHeight/2 + halfHeight + phOffset);
            int lowDrawEnd = lineHeight/2 + halfHeight + phOffset; // unclamped

            floorStart = Math.min(floorStart, drawEnd + 1);

            double wallX = (side == 1)
                ? camera.xPos + ((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY) * rayDirX
                : camera.yPos + ((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX) * rayDirY;
            wallX -= Math.floor(wallX);

            int texNum = (mapX >= 0 && mapY >= 0 && mapX < mapWidth && mapY < mapHeight) ? map[mapX][mapY] - 1 : -1;
            if(texNum >= 0)
            {
                Texture t = textures.get(texNum);
                int[] tpix = t.pixels;
                int tsize = t.SIZE;
                texX = (int)(wallX * tsize);
                if(side == 0 && rayDirX > 0) texX = tsize - texX - 1;
                if(side == 1 && rayDirY < 0) texX = tsize - texX - 1;
                boolean darken = (side != 0);
                for(int y = drawStart; y < drawEnd; y++)
                {
                    texY = ((((y - halfHeight - phOffset) * 2 + lineHeight) << 6) / lineHeight) / 2;
                    int tidx = texX + texY * tsize;
                    if(tidx >= 0 && tidx < tsize * tsize)
                    {
                        int color = darken ? (tpix[tidx] >> 1) & 8355711 : tpix[tidx];
                        if(color != 0)
                        {
                            int pidx = x + y * width;
                            if(perpWallDist < depthBuffer[pidx]) { pixels[pidx] = color; depthBuffer[pidx] = perpWallDist; }
                        }
                    }
                }
            }

            int oldStart = drawStart;
            double bottomDist = perpWallDist;
            int lowSide = side;
            int lowMapX = mapX, lowMapY = mapY;
            double lowSideDistX = sideDistX, lowSideDistY = sideDistY;
            double lowDeltaDistX = deltaDistX, lowDeltaDistY = deltaDistY;
            double lowWallX = wallX;

            // Ray cast - upper wall (map2)
            mapX = (int) camera.xPos; mapY = (int) camera.yPos;
            deltaDistX = dx; deltaDistY = dy;
            hit = false; side = 0;
            if(rayDirX < 0) { stepX = -1; sideDistX = (camera.xPos - mapX) * deltaDistX; }
            else             { stepX =  1; sideDistX = (mapX + 1.0 - camera.xPos) * deltaDistX; }
            if(rayDirY < 0) { stepY = -1; sideDistY = (camera.yPos - mapY) * deltaDistY; }
            else             { stepY =  1; sideDistY = (mapY + 1.0 - camera.yPos) * deltaDistY; }

            while(!hit)
            {
                if(!(mapX == 0 || mapY == 0 || mapX == mapWidth-1 || mapY == mapHeight-1))
                {
                    if(sideDistX < sideDistY) { sideDistX += deltaDistX; mapX += stepX; side = 0; }
                    else                       { sideDistY += deltaDistY; mapY += stepY; side = 1; }
                }
                if(mapX >= mapWidth)  mapX = mapWidth - 1;
                if(mapY >= mapHeight) mapY = mapHeight - 1;
                if(mapX == 0 || mapY == 0 || mapX == mapWidth-1 || mapY == mapHeight-1) hit = true;
                else if(map2[mapX][mapY] > 0) hit = true;
            }

            perpWallDist = (side == 0)
                ? Math.abs((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX)
                : Math.abs((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY);

            lineHeight = (perpWallDist > 0) ? Math.abs((int)(height / perpWallDist)) : height;
            phOffset = (int)(lineHeight * (ph - 0.5));

            int highDrawEndU = (ph > 1)
                ? -lineHeight/2 + halfHeight + phOffset
                : Math.min(oldStart, -lineHeight/2 + halfHeight + phOffset);
            int highDrawEnd = highDrawEndU; // unclamped
            drawStart = Math.max(0, highDrawEndU - lineHeight);
            drawEnd   = Math.max(0, Math.min(height - 1, highDrawEndU));

            wallX = (side == 1)
                ? camera.xPos + ((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY) * rayDirX
                : camera.yPos + ((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX) * rayDirY;
            wallX -= Math.floor(wallX);

            texNum = (mapX >= 0 && mapY >= 0 && mapX < mapWidth && mapY < mapHeight) ? map2[mapX][mapY] - 1 : -1;
            if(texNum >= 0)
            {
                Texture t = textures.get(texNum);
                int[] tpix = t.pixels;
                int tsize = t.SIZE;
                texX = (int)(wallX * tsize);
                if(side == 0 && rayDirX > 0) texX = tsize - texX - 1;
                if(side == 1 && rayDirY < 0) texX = tsize - texX - 1;
                boolean darken = (side != 0);
                for(int y = drawStart; y < drawEnd; y++)
                {
                    texY = ((((y - halfHeight - phOffset) * 2 + lineHeight * 3) << 6) / lineHeight) / 2;
                    int tidx = texX + texY * tsize;
                    if(tidx >= 0 && tidx < tsize * tsize)
                    {
                        int color = darken ? (tpix[tidx] >> 1) & 8355711 : tpix[tidx];
                        if(color != 0)
                        {
                            int pidx = x + y * width;
                            if(perpWallDist < depthBuffer[pidx]) { pixels[pidx] = color; depthBuffer[pidx] = perpWallDist; }
                        }
                    }
                }
            }

            int oldEnd = drawEnd;
            double topDist = perpWallDist;
            int highSide = side, highMapX = mapX, highMapY = mapY;
            double highWallX = wallX;
            boolean stop = false;

            // Additional upper walls (ph <= 1)
            if(ph <= 1)
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
                    perpWallDist = (side == 0)
                        ? Math.abs((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX)
                        : Math.abs((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY);
                    lineHeight = (perpWallDist > 0) ? Math.abs((int)(height / perpWallDist)) : height;
                    phOffset = (int)(lineHeight * (ph - 0.5));
                    drawStart = Math.max(0, Math.max(oldEnd, drawEnd));
                    drawEnd   = Math.min(height - 1, Math.min(oldStart, -lineHeight/2 + halfHeight + phOffset));
                    texNum = map2[mapX][mapY] - 1;
                    wallX = (side == 1)
                        ? camera.xPos + ((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY) * rayDirX
                        : camera.yPos + ((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX) * rayDirY;
                    wallX -= Math.floor(wallX);
                    if(texNum >= 0)
                    {
                        Texture t = textures.get(texNum);
                        int[] tpix = t.pixels;
                        int tsize = t.SIZE;
                        texX = (int)(wallX * tsize);
                        if(side == 0 && rayDirX > 0) texX = tsize - texX - 1;
                        if(side == 1 && rayDirY < 0) texX = tsize - texX - 1;
                        boolean darken = (side != 0);
                        for(int y = drawStart; y < drawEnd; y++)
                        {
                            texY = ((((y - halfHeight - phOffset) * 2 + lineHeight * 3) << 6) / lineHeight) / 2;
                            int tidx = texX + texY * tsize;
                            if(tidx >= 0 && tidx < tsize * tsize)
                            {
                                int color = darken ? (tpix[tidx] >> 1) & 8355711 : tpix[tidx];
                                if(color != 0)
                                {
                                    int pidx = x + y * width;
                                    if(perpWallDist < depthBuffer[pidx]) { pixels[pidx] = color; depthBuffer[pidx] = perpWallDist; }
                                }
                            }
                        }
                    }
                }while(drawStart < oldStart && !stop);
            }
            else
            {
                // Additional lower walls (ph > 1)
                mapX = lowMapX; mapY = lowMapY;
                sideDistX = lowSideDistX; sideDistY = lowSideDistY;
                deltaDistX = lowDeltaDistX; deltaDistY = lowDeltaDistY;
                drawStart = height;
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
                    perpWallDist = (side == 0)
                        ? Math.abs((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX)
                        : Math.abs((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY);
                    lineHeight = (perpWallDist > 0) ? Math.abs((int)(height / perpWallDist)) : height;
                    phOffset = (int)(lineHeight * (ph - 0.5));
                    drawEnd   = Math.min(height - 1, Math.min(oldStart, drawStart));
                    drawStart = Math.max(0, Math.max(oldEnd, -lineHeight/2 + halfHeight + phOffset));
                    texNum = map[mapX][mapY] - 1;
                    wallX = (side == 1)
                        ? camera.xPos + ((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY) * rayDirX
                        : camera.yPos + ((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX) * rayDirY;
                    wallX -= Math.floor(wallX);
                    if(texNum >= 0)
                    {
                        Texture t = textures.get(texNum);
                        int[] tpix = t.pixels;
                        int tsize = t.SIZE;
                        texX = (int)(wallX * tsize);
                        if(side == 0 && rayDirX > 0) texX = tsize - texX - 1;
                        if(side == 1 && rayDirY < 0) texX = tsize - texX - 1;
                        boolean darken = (side != 0);
                        for(int y = drawStart; y < drawEnd; y++)
                        {
                            texY = ((((y - halfHeight - phOffset) * 2 + lineHeight) << 6) / lineHeight) / 2;
                            int tidx = texX + texY * tsize;
                            if(tidx >= 0 && tidx < tsize * tsize)
                            {
                                int color = darken ? (tpix[tidx] >> 1) & 8355711 : tpix[tidx];
                                if(color != 0)
                                {
                                    int pidx = x + y * width;
                                    if(perpWallDist < depthBuffer[pidx]) { pixels[pidx] = color; depthBuffer[pidx] = perpWallDist; }
                                }
                            }
                        }
                    }
                }while(drawEnd > oldEnd && !stop);
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
        }

        // --- PASS 3: Horizontal surfaces (top of lower walls, bottom of upper walls) ---
        for(int x = 0; x < width; x++)
        {
            double cameraX = 2.0 * x / width - 1.0;
            double rayDirX = camera.xDir + camera.xPlane * cameraX;
            double rayDirY = camera.yDir + camera.yPlane * cameraX;
            double distWall   = colBottomDist[x];
            double floorXWall = colFloorXWall[x];
            double floorYWall = colFloorYWall[x];

            // Bottom of upper walls (ph < 1)
            if(ph < 1)
            {
                double factor = (1.0 - ph) * height;
                for(int y = halfHeight + 1; y < height; y++)
                {
                    double d = y - halfHeight;
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
                        // 75% brightness: multiply each channel by 3/4
                        bc = rgbNum((getR(bc) * 3) >> 2, (getG(bc) * 3) >> 2, (getB(bc) * 3) >> 2);
                        int botIdx = width * (height - y) + x;
                        if(currentDist2 < depthBuffer[botIdx]) { pixels[botIdx] = bc; depthBuffer[botIdx] = currentDist2; }
                    }
                }
            }

            // Top of lower walls (ph > 1)
            if(ph > 1)
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
                double factor = (ph - 1.0) * height;
                for(int y = halfHeight + 1; y < height; y++)
                {
                    double d = y - halfHeight;
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
                        int topIdx = width * y + x;
                        if(currentDist2 < depthBuffer[topIdx]) { pixels[topIdx] = tc; depthBuffer[topIdx] = currentDist2; }
                    }
                }
            }
        }
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
