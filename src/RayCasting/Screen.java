import java.util.ArrayList;
public class Screen
{
    private int frame;
    private int[][] map;
    private int[][] floorMap;
    private int[][] ceilingMap;
    private final int mapWidth;
    private final int mapHeight;
    private final int width;
    private final int height;
    private final double playerHeight;
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
        frame = 0;
        map = m;
        floorMap = fm;
        ceilingMap = cm;
        mapWidth = mapW;
        mapHeight = mapH;
        playerHeight = 0.5;
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

    public void updateGame(Camera camera, int[] pixels, int[][] m, int[][] fm, int[][] cm, int f)
    {
        frame = f;
        map = m;
        floorMap = fm;
        ceilingMap = cm;
        player.x = camera.xPos;
        player.y = camera.yPos;
        player.z = playerHeight;
        int texX;
        int texY;
        java.util.Arrays.fill(pixels, fogColor);
        java.util.Arrays.fill(colored, 0);
        //loops through all x-coordinates of the screen
        for(int x = 0; x < width; x++)
        {
            double cameraX = 2.0 * x / width - 1;
            double rayDirX = camera.xDir + camera.xPlane * cameraX;
            double rayDirY = camera.yDir + camera.yPlane * cameraX;
            //Map position
            int mapX = (int) camera.xPos;
            int mapY = (int) camera.yPos;
            //length of ray from current position to next x or y-side
            double sideDistX, sideDistY;
            //Length of ray from one side to next in map
            double deltaDistX = Math.abs(1 / rayDirX);
            double deltaDistY = Math.abs(1 / rayDirY);
            //perpendicular distance to the wall
            double perpWallDist;
            //Direction to go in x and y
            int stepX, stepY;
            boolean hit = false;//was a wall hit
            int side = 0;//was the wall vertical or horizontal
            //Figure out the step direction and initial distance to a side
            if(rayDirX < 0)
            {
                stepX = -1;
                sideDistX = (camera.xPos - mapX) * deltaDistX;
            }
            else
            {
                stepX = 1;
                sideDistX = (mapX + 1.0 - camera.xPos) * deltaDistX;
            }
            if(rayDirY < 0)
            {
                stepY = -1;
                sideDistY = (camera.yPos - mapY) * deltaDistY;
            }
            else
            {
                stepY = 1;
                sideDistY = (mapY + 1.0 - camera.yPos) * deltaDistY;
            }
            //Loop to find where the ray hits a wall
            while(!hit)
            {
                //Jump to next square
                if(sideDistX < sideDistY)
                {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                }
                else
                {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }
                //Check if ray has hit a wall
                if(mapX < 0 || mapX >= mapWidth || mapY < 0 || mapY >= mapHeight)
                    hit = true;
                else if(map[mapX][mapY] > 0)
                    hit = true;
            }
            double wallX;//Exact position of where wall was hit
            //Calculate distance to the point of impact
            if(side == 0)
            {
                perpWallDist = (mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX;
                wallX = camera.yPos + perpWallDist * rayDirY;
            }
            else
            {
                perpWallDist = (mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY;
                wallX = camera.xPos + perpWallDist * rayDirX;
            }
            //Get fractional part of wallX
            wallX -= (int) wallX;
            //Calculate the height of the wall based on the distance from the camera
            int lineHeight;
            if(perpWallDist > 0)
                lineHeight = (int) (height / perpWallDist);
            else
                lineHeight = height;
            //calculate lowest and highest pixel to fill in current stripe
            int drawStart = -lineHeight/2 + height/2;
            if(drawStart < -shift)
                drawStart = -shift;
            int drawEnd = lineHeight/2 + height/2;
            if(drawEnd >= height - shift) 
                drawEnd = height - 1 - shift;
            //add a texture
            int texNum;
            if(mapX < 0 || mapX >= mapWidth || mapY < 0 || mapY >= mapHeight)
                texNum = -1;
            else
                texNum = map[mapX][mapY] - 1;
            //x coordinate on the texture
            if(texNum >= 0)
            {
                Texture tex = textures.get(texNum);
                texX = (int) (wallX * tex.SIZE);
                if(side == 0 && rayDirX > 0)
                    texX = tex.SIZE - texX - 1;
                if(side == 1 && rayDirY < 0)
                    texX = tex.SIZE - texX - 1;
                //calculate y coordinate on texture
                double a = 1.0 * tex.SIZE / lineHeight;
                double b = (-height/2.0 + lineHeight/2.0) * a;
                boolean notBlack = tex != Texture.black;
                double wallColorVal = (mapX + wallX - camera.xPos) * (mapX + wallX - camera.xPos) + (mapY - camera.yPos) * (mapY - camera.yPos) - mapWidth * mapHeight;
                for(int y = drawStart; y < drawEnd; y++)
                {
                    texY = (int) (y * a + b);
                    int color = tex.pixels[texX + (texY * tex.SIZE)];
                    if(side == 1)
                        color = (color >> 1) & 8355711;//Make y sides darker
                    int py = Math.max(0, Math.min(y + shift, height - 1));
                    if(fog != null && fogColor != 0)
                    {
                        // fog based purely on wall distance, not view angle
                        int fogIdx = (int)(height / 2.0 + height / (2.0 * perpWallDist));
                        fogIdx = Math.max(0, Math.min(fogIdx, fog.length - 1));
                        double fogAmt = fog[fogIdx];
                        double colAmt = 1.0 - fogAmt;
                        int fogR = fogColor/65536, fogG = fogColor%65536/256, fogB = fogColor%65536%256;
                        int cr = color/65536, cg = color%65536/256, cb = color%65536%256;
                        int nr = Math.min(255, (int)(fogAmt*fogR+colAmt*cr));
                        int ng = Math.min(255, (int)(fogAmt*fogG+colAmt*cg));
                        int nb = Math.min(255, (int)(fogAmt*fogB+colAmt*cb));
                        color = nr*65536 + ng*256 + nb;
                    }
                    pixels[x + py * width] = color;
                    if(notBlack)
                        colored[x + py * width] = wallColorVal;
                }
            }
            ZBuffer[x] = perpWallDist;
            //coordinates of floor at bottom of wall
            double floorXWall;
            double floorYWall;
            if(side == 0 && rayDirX > 0)
            {
                floorXWall = mapX;
                floorYWall = mapY + wallX;
            }
            else if(side == 0 && rayDirX < 0)
            {
                floorXWall = mapX + 1.0;
                floorYWall = mapY + wallX;
            }
            else if(side == 1 && rayDirY > 0)
            {
                floorXWall = mapX + wallX;
                floorYWall = mapY;
            }
            else
            {
                floorXWall = mapX + wallX;
                floorYWall = mapY + 1.0;
            }
            floorXWall = Math.max(0, Math.min(floorXWall, mapWidth - 1));
            floorYWall = Math.max(0, Math.min(floorYWall, mapHeight - 1));
            double distWall = perpWallDist;
            if(drawEnd < 0) 
                drawEnd = height - shift;
            //loops through y-coordinates from bottom of wall to bottom of screen
            Texture firstTex = textures.getFirst();
            for(int y = drawEnd + 1; y < height + Math.abs(shift); y++)
            {
                //calculates color on texture for each pixel of the floor
                double weight = Math.max(0, Math.min(height / (distWall * (2 * y - height)), 1));
                double currentFloorX = weight * floorXWall + (1.0 - weight) * camera.xPos;
                double currentFloorY = weight * floorYWall + (1.0 - weight) * camera.yPos;
                int floorTexX = (int) (currentFloorX * firstTex.SIZE) % firstTex.SIZE;
                int floorTexY = (int) (currentFloorY * firstTex.SIZE) % firstTex.SIZE;
                int floorTexture = floorMap[(int) currentFloorX][(int) currentFloorY];
                int ceilingTexture = ceilingMap[(int) currentFloorX][(int) currentFloorY];
                Texture floorTex = textures.get(floorTexture - 1);
                Texture ceilTex = textures.get(ceilingTexture - 1);
                int floorColor = floorTex.pixels[floorTex.SIZE * floorTexY + floorTexX];
                int ceilingColor = ceilTex.pixels[ceilTex.SIZE * floorTexY + floorTexX];
                if(fog != null && fogColor != 0)
                {
                    int fogIdx = Math.min(Math.max(y, 0), fog.length-1);
                    double fogAmt = fog[fogIdx];
                    double colAmt = 1.0 - fogAmt;
                    int fogR = fogColor/65536, fogG = fogColor%65536/256, fogB = fogColor%65536%256;
                    int fr = floorColor/65536, fg2 = floorColor%65536/256, fb = floorColor%65536%256;
                    int cr = ceilingColor/65536, cg = ceilingColor%65536/256, cb = ceilingColor%65536%256;
                    floorColor = Math.min(255,(int)(fogAmt*fogR+colAmt*fr))*65536 + Math.min(255,(int)(fogAmt*fogG+colAmt*fg2))*256 + Math.min(255,(int)(fogAmt*fogB+colAmt*fb));
                    ceilingColor = Math.min(255,(int)(fogAmt*fogR+colAmt*cr))*65536 + Math.min(255,(int)(fogAmt*fogG+colAmt*cg))*256 + Math.min(255,(int)(fogAmt*fogB+colAmt*cb));
                }                int floorPy = Math.max(0, Math.min(y + shift, height - 1));
                int ceilPy = Math.max(0, Math.min(height - y + shift, drawStart + shift - 1));
                pixels[width * floorPy + x] = floorColor;
                pixels[width * ceilPy + x] = ceilingColor;
            }
            pixels[width * Math.max(0, Math.min(drawEnd + shift, height - 1)) + x] = pixels[width * Math.max(0, Math.min(drawEnd - 1 + shift, height - 1)) + x];
        }
        //drawing 3D points
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
                    int pointScreenX = (int) (width/2 * (1 + transformX/transformY));
                    int pointHeight = (int) (height/transformY);
                    int drawStartY = -pointHeight/64 + height/2 - (int) (3 * pointHeight/8 * pointZ);
                    if(drawStartY < -shift) 
                        drawStartY = -shift;
                    int drawEndY = pointHeight/64 + height/2 - (int) (3 * pointHeight/8 * pointZ);
                    if(drawEndY >= height - shift) 
                        drawEndY = height - 1 - shift;
                    int drawStartX = -pointHeight /64 + pointScreenX;
                    if(drawStartX < 0)
                        drawStartX = 0;
                    int drawEndX = pointHeight /64 + pointScreenX;
                    if(drawEndX >= width)
                        drawEndX = width - 1;
                    for(int drawX = drawStartX; drawX <= drawEndX; drawX++)
                    {
                        if(transformY > 0 && transformY < ZBuffer[drawX])
                        {
                            // fog based on actual distance to sprite
                            double spriteDist = Math.sqrt(pointX * pointX + pointY * pointY);
                            int fogIdx = (int)(height / 2.0 + height / (2.0 * spriteDist));
                            fogIdx = Math.max(0, Math.min(fogIdx, fog.length - 1));
                            double fogAmt = (fog != null && fogColor != 0) ? fog[fogIdx] : 0;
                            double colAmt = 1.0 - fogAmt;
                            int fogR = fogColor/65536, fogG = fogColor%65536/256, fogB = fogColor%65536%256;
                            for(int drawY = drawStartY; drawY <= drawEndY; drawY++)
                            {
                                double a = pointX * pointX + pointY * pointY - mapWidth * mapHeight;
                                if(a < colored[Math.max(0, Math.min(drawY + shift, height - 1)) * width + drawX])
                                {
                                    int c = pcolor;
                                    if(fogAmt > 0)
                                    {
                                        int cr = c/65536, cg = c%65536/256, cb = c%65536%256;
                                        int nr = Math.min(255,(int)(fogAmt*fogR+colAmt*cr));
                                        int ng = Math.min(255,(int)(fogAmt*fogG+colAmt*cg));
                                        int nb = Math.min(255,(int)(fogAmt*fogB+colAmt*cb));
                                        c = nr*65536 + ng*256 + nb;
                                    }
                                    pixels[Math.max(0, Math.min(drawY + shift, height - 1)) * width + drawX] = c;
                                    colored[Math.max(0, Math.min(drawY + shift, height - 1)) * width + drawX] = a;
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
        //shots
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
            // Check ceiling collision: interpolate to exact XY where shot crossed z=1.0
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
                // First frame - just advance without collision
                shot.prevPos.x = shot.pos.getX();
                shot.prevPos.y = shot.pos.getY();
                shot.prevPos.z = shot.pos.getZ();
                shot.pos.add(shot.dir);
            }
            //drawing shot
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
                int pointScreenX = (int) ((width/2) * (1 + transformX/transformY));
                int pointHeight = Math.abs((int) (height/transformY));
                pointHeight /= 4;
                pointHeight += 8;
                pointHeight = Math.max(32, Math.min(pointHeight, 512));
                int drawStartY = -pointHeight/32 + height/2 - (int) Math.round(3.0 * pointHeight/4 * pointZ);
                if(drawStartY < -shift) 
                    drawStartY = -shift;
                int drawEndY = pointHeight/32 + height/2 - (int) Math.round(3.0 * pointHeight/4 * pointZ);
                if(drawEndY >= height - shift) 
                    drawEndY = height - 1 - shift;
                int pointWidth = Math.abs((int) (height/transformY));
                pointWidth /= 4;
                pointWidth += 8;
                pointWidth = Math.max(32, Math.min(pointWidth, 512));
                int drawStartX = -pointWidth/32 + pointScreenX;
                if(drawStartX <= 0)
                    drawStartX = 0;
                int drawEndX = pointWidth/32 + pointScreenX;
                if(drawEndX >= width)
                    drawEndX = width - 1;
                for(int drawX = drawStartX; drawX <= drawEndX; drawX++)
                {
                    if(transformY > 0 && drawX > 0 && drawX < width && transformY < ZBuffer[drawX])
                    {
                        // fog based on actual distance to shot
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
                            int nr = Math.min(255,(int)(fogAmt*fogR+colAmt*cr));
                            int ng = Math.min(255,(int)(fogAmt*fogG+colAmt*cg));
                            int nb = Math.min(255,(int)(fogAmt*fogB+colAmt*cb));
                            shotColor = nr*65536 + ng*256 + nb;
                        }
                        for(int drawY = drawStartY; drawY <= drawEndY; drawY++)
                        {
                            pixels[Math.max(0, Math.min(drawY + shift, height - 1)) * width + drawX] = shotColor;
                        }
                    }
                }
            }
        }
        camera.shoot = false;
        shotTimer++;
        shift += camera.mouseDeltaY * -1;
        camera.mouseDeltaY = 0;
        shift = Math.max(-height/2, Math.min(shift, height/2));
        //draw crosshair into pixel buffer
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
        //gets rgb decimal value from rgb input
        return r * 65536 + g * 256 + b;
    }

    private int getR(int color)
    {
        //gets r value from rgb decimal input
        return color/65536;
    }

    private int getG(int color)
    {
        //gets g value from rgb decimal input
        return color % 65536/256;
    }

    private int getB(int color)
    {
        //gets b value from rgb decimal input
        return color % 65536 % 256;
    }
}
