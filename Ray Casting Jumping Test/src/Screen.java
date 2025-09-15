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
    private final boolean lag;
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
        lag = false;
    }

    public void updateGame(Camera camera, int[] pixels, int[][] m, int[][] m2, int[][] fm, int[][] cm, double ph)
    {
        map = m;
        map2 = m2;
        floorMap = fm;
        ceilingMap = cm;
        int floorStart = height;
        //coordinates of floor at bottom of wall
        int texX = 0;
        int texY;
        //loops through all x-coordinates of the screen
        for(int x = 0; x < width; x++)
        {
            double cameraX = 2 * x / (double) (width) - 1;
            double rayDirX = camera.xDir + camera.xPlane * cameraX;
            double rayDirY = camera.yDir + camera.yPlane * cameraX;
            //Map position
            int mapX = (int) camera.xPos;
            int mapY = (int) camera.yPos;
            //length of ray from current position to next x or y-side
            double sideDistX;
            double sideDistY;
            //Length of ray from one side to next in map
            double dx = Math.sqrt(1 + (rayDirY * rayDirY) / (rayDirX * rayDirX));
            double deltaDistX = dx;
            double dy = Math.sqrt(1 + (rayDirX * rayDirX) / (rayDirY * rayDirY));
            double deltaDistY = dy;
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
                if(!(mapX == 0 || mapY == 0 || mapX == mapWidth - 1 || mapY == mapHeight - 1)) {
                    if (sideDistX < sideDistY) {
                        sideDistX += deltaDistX;
                        mapX += stepX;
                        side = 0;
                    } else {
                        sideDistY += deltaDistY;
                        mapY += stepY;
                        side = 1;
                    }
                }
                if(mapX >= mapWidth)
                    mapX = mapWidth - 1;
                if(mapY >= mapHeight)
                    mapY = mapHeight - 1;
                //Check if ray has hit a wall
                if(mapX == 0 || mapY == 0 || mapX == mapWidth - 1 || mapY == mapHeight - 1) 
                    hit = true;
                else if(map[mapX][mapY] > 0)
                    hit = true;
            }
            //Calculate distance to the point of impact
            if(side == 0)
                perpWallDist = Math.abs((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX);
            else
                perpWallDist = Math.abs((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY);
            //Now calculate the height of the wall based on the distance from the camera
            int lineHeight;
            if(perpWallDist > 0) 
                lineHeight = Math.abs((int) (height / perpWallDist));
            else
                lineHeight = height;
            //calculate lowest and highest pixel to fill in current stripe
            int drawStart = -lineHeight/2 + height/2 + (int) (lineHeight * (ph - 0.5));
            if(drawStart < 0)
                drawStart = 0;
            int drawEnd = lineHeight/2 + height/2 + (int) (lineHeight * (ph - 0.5));
            if(drawEnd >= height) 
                drawEnd = height - 1;
            floorStart = Math.min(floorStart, drawEnd + 1);
            //add a texture
            int texNum;
            if(mapX >= 0 && mapY >= 0 && mapX < mapWidth && mapY < mapHeight)
                texNum = map[mapX][mapY] - 1;
            else
                texNum = -1;
            double wallX;//Exact position of where wall was hit
            if(side == 1)
            {//If it is a y-axis wall
                wallX = (camera.xPos + ((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY) * rayDirX);
            }
            else
            {//X-axis wall
                wallX = (camera.yPos + ((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX) * rayDirY);
            }
            wallX -= Math.floor(wallX);
            //x coordinate on the texture
            if(texNum >= 0)
            {
                texX = (int) (wallX * (textures.get(texNum).SIZE));
                if(side == 0 && rayDirX > 0) 
                    texX = textures.get(texNum).SIZE - texX - 1;
                if(side == 1 && rayDirY < 0) 
                    texX = textures.get(texNum).SIZE - texX - 1;
            }
            //calculate y coordinate on texture
            for(int y = drawStart; y < drawEnd; y++) 
            {
                if(texNum >= 0)
                {
                    texY = ((((y - height/2 - (int) (lineHeight * (ph - 0.5))) * 2 + lineHeight) << 6) / lineHeight) / 2;
                    int color = 0;
                    if(side == 0 && texX + (texY * textures.get(texNum).SIZE) >= 0 && texX + (texY * textures.get(texNum).SIZE) < textures.get(texNum).SIZE * textures.get(texNum).SIZE)
                        color = textures.get(texNum).pixels[texX + (texY * textures.get(texNum).SIZE)];
                    else if(texX + (texY * textures.get(texNum).SIZE) >= 0 && texX + (texY * textures.get(texNum).SIZE) < textures.get(texNum).SIZE * textures.get(texNum).SIZE)
                        color = (textures.get(texNum).pixels[texX + (texY * textures.get(texNum).SIZE)] >> 1) & 8355711;//Make y sides darker
                    pixels[x + y * (width)] = color;
                }
            }
            int oldStart = drawStart;
            int lowLineHeight = lineHeight;
            double bottomDist = perpWallDist;
            int lowSide = side;
            int lowMapX = mapX;
            int lowMapY = mapY;
            double lowSideDistX = sideDistX;
            double lowSideDistY = sideDistY;
            double lowDeltaDistX = deltaDistX;
            double lowDeltaDistY = deltaDistY;
            double lowWallX = wallX;
            //repeat ray casting for second level of walls
            //Map position
            mapX = (int) camera.xPos;
            mapY = (int) camera.yPos;
            //Length of ray from one side to next in map
            deltaDistX = dx;
            deltaDistY = dy;
            hit = false;//was a wall hit
            side = 0;//was the wall vertical or horizontal
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
                if(!(mapX == 0 || mapY == 0 || mapX == mapWidth - 1 || mapY == mapHeight - 1)) {
                    if (sideDistX < sideDistY) {
                        sideDistX += deltaDistX;
                        mapX += stepX;
                        side = 0;
                    } else {
                        sideDistY += deltaDistY;
                        mapY += stepY;
                        side = 1;
                    }
                }
                if(mapX >= mapWidth)
                    mapX = mapWidth - 1;
                if(mapY >= mapHeight)
                    mapY = mapHeight - 1;
                //Check if ray has hit a wall
                if(mapX == 0 || mapY == 0 || mapX == mapWidth - 1 || mapY == mapHeight - 1) 
                    hit = true;
                else if(map2[mapX][mapY] > 0)
                    hit = true;
            }
            //Calculate distance to the point of impact
            if(side==0)
                perpWallDist = Math.abs((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX);
            else
                perpWallDist = Math.abs((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY);
            //Now calculate the height of the wall based on the distance from the camera
            if(perpWallDist > 0) 
                lineHeight = Math.abs((int)(height / perpWallDist));
            else 
                lineHeight = height;
            //calculate lowest and highest pixel to fill in current stripe
            if(ph > 1)
                drawEnd = -lineHeight/2 + height/2 + (int) (lineHeight * (ph - 0.5));
            else
                drawEnd = Math.min(oldStart, -lineHeight/2 + height/2 + (int) (lineHeight * (ph - 0.5)));
            drawStart = drawEnd - 2 * lineHeight;
            if(drawStart < 0)
                drawStart = 0;
            if(drawEnd >= height) 
                drawEnd = height - 1;
            //ceiling starting point
            //add a texture
            if(mapX >= 0 && mapY >= 0 && mapX < mapWidth && mapY < mapHeight)
                texNum = map2[mapX][mapY] - 1;
            else
                texNum = -1;
            if(side == 1)
            {//If it is a y-axis wall
                wallX = (camera.xPos + ((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY) * rayDirX);
            }
            else
            {//X-axis wall
                wallX = (camera.yPos + ((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX) * rayDirY);
            }
            wallX -= Math.floor(wallX);
            //x coordinate on the texture
            if(texNum >= 0)
            {
                texX = (int) (wallX * (textures.get(texNum).SIZE));
                if(side == 0 && rayDirX > 0) 
                    texX = textures.get(texNum).SIZE - texX - 1;
                if(side == 1 && rayDirY < 0) 
                    texX = textures.get(texNum).SIZE - texX - 1;
            }
            //calculate y coordinate on texture
            for(int y = drawStart; y < drawEnd; y++) 
            {
                if(texNum >= 0)
                {
                    texY = ((((y - height/2 - (int) (lineHeight * (ph - 0.5))) * 2 + lineHeight * 3) << 6) / lineHeight) / 2;
                    int color = 0;
                    if(side == 0 && texX + (texY * textures.get(texNum).SIZE) >= 0 && texX + (texY * textures.get(texNum).SIZE) < textures.get(texNum).SIZE * textures.get(texNum).SIZE)
                        color = textures.get(texNum).pixels[texX + (texY * textures.get(texNum).SIZE)];
                    else if(texX + (texY * textures.get(texNum).SIZE) >= 0 && texX + (texY * textures.get(texNum).SIZE) < textures.get(texNum).SIZE * textures.get(texNum).SIZE)
                        color = (textures.get(texNum).pixels[texX + (texY * textures.get(texNum).SIZE)] >> 1) & 8355711;//Make y sides darker
                    pixels[x + y * (width)] = color;
                }
            }
            int oldEnd = drawEnd;
            int highLineHeight = lineHeight;
            double topDist = perpWallDist;
            int highSide = side;
            int highMapX = mapX;
            int highMapY = mapY;
            double highWallX = wallX;
            boolean stop = false;
            if(ph <= 1)
            {
                do
                {
                    hit = false;
                    while(!hit)
                    {
                        //Jump to next square
                        if(mapX == 0 || mapY == 0 || mapX == mapWidth - 1 || mapY == mapHeight - 1) 
                            stop = true;
                        else if(sideDistX < sideDistY)
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
                        if(mapX >= mapWidth)
                            mapX = mapWidth - 1;
                        if(mapY >= mapHeight)
                            mapY = mapHeight - 1;
                        //Check if ray has hit a wall
                        if(mapX == 0 || mapY == 0 || mapX == mapWidth - 1 || mapY == mapHeight - 1) 
                            hit = true;
                        else if(map2[mapX][mapY] > 0)
                            hit = true;
                    }
                    //Calculate distance to the point of impact
                    if(side == 0)
                        perpWallDist = Math.abs((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX);
                    else
                        perpWallDist = Math.abs((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY);
                    //Now calculate the height of the wall based on the distance from the camera
                    if(perpWallDist > 0) 
                        lineHeight = Math.abs((int) (height / perpWallDist));
                    else 
                        lineHeight = height;
                    //calculate lowest and highest pixel to fill in current stripe
                    drawStart = Math.max(oldEnd, drawEnd);
                    drawEnd = Math.min(oldStart, -lineHeight/2 + height/2 + (int) (lineHeight * (ph - 0.5)));
                    if(drawStart < 0)
                        drawStart = 0;
                    if(drawEnd >= height) 
                        drawEnd = height - 1;
                    texNum = map2[mapX][mapY] - 1;
                    if(side == 1)
                    {//If it is a y-axis wall
                        wallX = (camera.xPos + ((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY) * rayDirX);
                    }
                    else
                    {//X-axis wall
                        wallX = (camera.yPos + ((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX) * rayDirY);
                    }
                    wallX -= Math.floor(wallX);
                    //x coordinate on the texture
                    texX = (int) (wallX * (textures.get(texNum).SIZE));
                    if(side == 0 && rayDirX > 0) 
                        texX = textures.get(texNum).SIZE - texX - 1;
                    if(side == 1 && rayDirY < 0) 
                        texX = textures.get(texNum).SIZE - texX - 1;
                    //calculate y coordinate on texture
                    for(int y = drawStart; y < drawEnd; y++) 
                    {
                        texY = ((((y - height/2 - (int) (lineHeight * (ph - 0.5))) * 2 + lineHeight * 3) << 6) / lineHeight) / 2;
                        int color = 0;
                        if(side == 0 && texX + (texY * textures.get(texNum).SIZE) >= 0 && texX + (texY * textures.get(texNum).SIZE) < textures.get(texNum).SIZE * textures.get(texNum).SIZE)
                            color = textures.get(texNum).pixels[texX + (texY * textures.get(texNum).SIZE)];
                        else if(texX + (texY * textures.get(texNum).SIZE) >= 0 && texX + (texY * textures.get(texNum).SIZE) < textures.get(texNum).SIZE * textures.get(texNum).SIZE)
                            color = (textures.get(texNum).pixels[texX + (texY * textures.get(texNum).SIZE)] >> 1) & 8355711;//Make y sides darker
                        pixels[x + y * (width)] = color;
                    }
                }while(drawStart < oldStart && !stop);
            }
            else
            {
                mapX = lowMapX;
                mapY = lowMapY;
                sideDistX = lowSideDistX;
                sideDistY = lowSideDistY;
                deltaDistX = lowDeltaDistX;
                deltaDistY = lowDeltaDistY;
                drawStart = height;
                do
                {
                    hit = false;
                    while(!hit)
                    {
                        //Jump to next square
                        if(mapX == 0 || mapY == 0 || mapX == mapWidth - 1 || mapY == mapHeight - 1) 
                            stop = true;
                        else if(sideDistX < sideDistY)
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
                        if(mapX >= mapWidth)
                            mapX = mapWidth - 1;
                        if(mapY >= mapHeight)
                            mapY = mapHeight - 1;
                        //Check if ray has hit a wall
                        if(mapX == 0 || mapY == 0 || mapX == mapWidth - 1 || mapY == mapHeight - 1) 
                            hit = true;
                        else if(map[mapX][mapY] > 0)
                            hit = true;
                    }
                    //Calculate distance to the point of impact
                    if(side == 0)
                        perpWallDist = Math.abs((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX);
                    else
                        perpWallDist = Math.abs((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY);
                    //Now calculate the height of the wall based on the distance from the camera
                    if(perpWallDist > 0) 
                        lineHeight = Math.abs((int) (height / perpWallDist));
                    else 
                        lineHeight = height;
                    //calculate lowest and highest pixel to fill in current stripe
                    drawEnd = Math.min(oldStart, drawStart);
                    drawStart = Math.max(oldEnd, -lineHeight/2 + height/2 + (int) (lineHeight * (ph - 0.5)));
                    if(drawStart < 0)
                        drawStart = 0;
                    if(drawEnd >= height) 
                        drawEnd = height - 1;
                    texNum = map[mapX][mapY] - 1;
                    if(side == 1)
                    {//If it is a y-axis wall
                        wallX = (camera.xPos + ((mapY - camera.yPos + (1 - stepY) / 2.0) / rayDirY) * rayDirX);
                    }
                    else
                    {//X-axis wall
                        wallX = (camera.yPos + ((mapX - camera.xPos + (1 - stepX) / 2.0) / rayDirX) * rayDirY);
                    }
                    wallX -= Math.floor(wallX);
                    //x coordinate on the texture
                    texX = (int) (wallX * (textures.get(texNum).SIZE));
                    if(side == 0 && rayDirX > 0) 
                        texX = textures.get(texNum).SIZE - texX - 1;
                    if(side == 1 && rayDirY < 0) 
                        texX = textures.get(texNum).SIZE - texX - 1;
                    //calculate y coordinate on texture
                    for(int y = drawStart; y < drawEnd; y++) 
                    {
                        texY = ((((y - height/2 - (int) (lineHeight * (ph - 0.5))) * 2 + lineHeight) << 6) / lineHeight) / 2;
                        int color = 0;
                        if(side == 0 && texX + (texY * textures.get(texNum).SIZE) >= 0 && texX + (texY * textures.get(texNum).SIZE) < textures.get(texNum).SIZE * textures.get(texNum).SIZE)
                            color = textures.get(texNum).pixels[texX + (texY * textures.get(texNum).SIZE)];
                        else if(texX + (texY * textures.get(texNum).SIZE) >= 0 && texX + (texY * textures.get(texNum).SIZE) < textures.get(texNum).SIZE * textures.get(texNum).SIZE)
                            color = (textures.get(texNum).pixels[texX + (texY * textures.get(texNum).SIZE)] >> 1) & 8355711;//Make y sides darker
                        pixels[x + y * (width)] = color;
                    }
                }while(drawEnd > oldEnd && !stop);
            }
            //floorcasting:
            //draws floors
            //draws ceiling and tops/bottoms of walls if lag 
            //variable is set to true
            if(!lag)
            {
                double floorXWall;
                double floorYWall;
                mapX = lowMapX;
                mapY = lowMapY;
                side = lowSide;
                wallX = lowWallX;
                drawEnd = oldStart + lowLineHeight - 1;
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
                double currentDist;
                double distPlayer = 0.0;
                if(drawEnd < 0) 
                    drawEnd = height;
                int lineColor = 0;
                //loops through y-coordinates from bottom of wall to
                //bottom of screen
                for(int y = drawEnd + 1; y < height; y++)
                {
                    //calculates color on texture for each pixel of the floor
                    currentDist = ph * height / (y - height/2.0);
                    double weight = (currentDist - distPlayer) / (bottomDist - distPlayer);
                    double currentFloorX = weight * floorXWall + (1.0 - weight) * camera.xPos;
                    double currentFloorY = weight * floorYWall + (1.0 - weight) * camera.yPos;
                    currentFloorX = Math.min(Math.max(currentFloorX, 1), mapWidth - 1);
                    currentFloorY = Math.min(Math.max(currentFloorY, 1), mapHeight - 1);
                    int floorTexX = (int) (currentFloorX * textures.getFirst().SIZE) % textures.getFirst().SIZE;
                    int floorTexY = (int) (currentFloorY * textures.getFirst().SIZE) % textures.getFirst().SIZE;
                    int floorTexture = floorMap[(int) currentFloorX][(int) currentFloorY];
                    int floorColor = textures.get(floorTexture - 1).pixels[textures.get(floorTexture - 1).SIZE * floorTexY + floorTexX];
                    pixels[width * y + x] = floorColor;
                    if(y == drawEnd + 1)
                        lineColor = floorColor;
                }
                if(drawEnd < height - 1 && pixels[width * drawEnd + x] == 0)
                    pixels[width * drawEnd + x] = lineColor;

                drawEnd = height - oldEnd + highLineHeight - 1;
                for(int y = drawEnd + 1; y < height; y++)
                {
                    if(height - y < height)
                        pixels[width * (height - y) + x] = 0;
                }
            }
            else
            {
                double floorXWall;
                double floorYWall;
                mapX = lowMapX;
                mapY = lowMapY;
                side = lowSide;
                wallX = lowWallX;
                drawEnd = oldStart + lowLineHeight - 1;
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
                double currentDist;
                double distWall = bottomDist;
                double distPlayer = 0.0;
                if(drawEnd < 0) 
                    drawEnd = height;
                int lineColor = 0;
                //loops through y-coordinates from bottom of wall to
                //bottom of screen
                for(int y = drawEnd + 1; lag && y < height; y++)
                {
                    //calculates color on texture for each pixel of the floor
                    //floors
                    currentDist = ph * height / (y - height/2.0);
                    double weight = (currentDist - distPlayer) / (distWall - distPlayer);
                    double currentFloorX = weight * floorXWall + (1.0 - weight) * camera.xPos;
                    double currentFloorY = weight * floorYWall + (1.0 - weight) * camera.yPos;
                    currentFloorX = Math.min(Math.max(currentFloorX, 1), mapWidth - 1);
                    currentFloorY = Math.min(Math.max(currentFloorY, 1), mapHeight - 1);
                    int floorTexX = (int) (currentFloorX * textures.getFirst().SIZE) % textures.getFirst().SIZE;
                    int floorTexY = (int) (currentFloorY * textures.getFirst().SIZE) % textures.getFirst().SIZE;
                    int floorTexture = floorMap[(int) currentFloorX][(int) currentFloorY];
                    int floorColor = textures.get(floorTexture - 1).pixels[textures.get(floorTexture - 1).SIZE * floorTexY + floorTexX];
                    pixels[width * y + x] = floorColor;
                    if(y == drawEnd + 1)
                        lineColor = floorColor;
                    //bottom of walls
                    int bottomTexture;
                    int bottomColor = -1;
                    if(ph < 1)
                    {
                        currentDist = (1 - ph) * height / (y - height/2.0);
                        weight = (currentDist - distPlayer) / (distWall - distPlayer);
                        currentFloorX = weight * floorXWall + (1.0 - weight) * camera.xPos;
                        currentFloorY = weight * floorYWall + (1.0 - weight) * camera.yPos;
                        currentFloorX = Math.min(Math.max(currentFloorX, 1), mapWidth - 1);
                        currentFloorY = Math.min(Math.max(currentFloorY, 1), mapHeight - 1);
                        floorTexX = (int) (currentFloorX * textures.getFirst().SIZE) % textures.getFirst().SIZE;
                        floorTexY = (int) (currentFloorY * textures.getFirst().SIZE) % textures.getFirst().SIZE;
                        bottomTexture = map2[(int) currentFloorX][(int) currentFloorY];
                        if(bottomTexture != 0 && map[(int) currentFloorX][(int) currentFloorY] == 0)
                            bottomColor = textures.get(bottomTexture - 1).pixels[textures.get(bottomTexture - 1).SIZE * floorTexY + floorTexX];
                        if(bottomColor >= 0)
                            bottomColor = rgbNum((int) (0.75 * getR(bottomColor)), (int) (0.75 * getG(bottomColor)), (int) (0.75 * getB(bottomColor)));                //fog/color amount same as for walls
                    }
                    if(bottomColor >= 0)
                        pixels[width * (height - y) + x] = bottomColor;
                }
                if(drawEnd < height - 1 && pixels[width * drawEnd + x] == 0)
                    pixels[width * drawEnd + x] = lineColor;
                double ceilingXWall;
                double ceilingYWall;
                mapX = highMapX;
                mapY = highMapY;
                side = highSide;
                wallX = highWallX;
                drawEnd = height - oldEnd + highLineHeight - 1;
                if(side == 0 && rayDirX > 0)
                {
                    ceilingXWall = mapX;
                    ceilingYWall = mapY + wallX;
                }
                else if(side == 0 && rayDirX < 0)
                {
                    ceilingXWall = mapX + 1.0;
                    ceilingYWall = mapY + wallX;
                }
                else if(side == 1 && rayDirY > 0)
                {
                    ceilingXWall = mapX + wallX;
                    ceilingYWall = mapY;
                }
                else
                {
                    ceilingXWall = mapX + wallX;
                    ceilingYWall = mapY + 1.0;
                }
                distWall = topDist;
                for(int y = drawEnd + 1; y < height; y++)
                {
                    //calculates color on texture for each pixel of the floor
                    //ceilings
                    currentDist = (2 - ph) * height / (y - height/2.0);
                    double weight = (currentDist - distPlayer) / (distWall - distPlayer);
                    double currentCeilingX = weight * ceilingXWall + (1.0 - weight) * camera.xPos;
                    double currentCeilingY = weight * ceilingYWall + (1.0 - weight) * camera.yPos;
                    currentCeilingX = Math.min(Math.max(currentCeilingX, 1), mapWidth - 1);
                    currentCeilingY = Math.min(Math.max(currentCeilingY, 1), mapHeight - 1);
                    int ceilingTexX = (int) (currentCeilingX * textures.getFirst().SIZE) % textures.getFirst().SIZE;
                    int ceilingTexY = (int) (currentCeilingY * textures.getFirst().SIZE) % textures.getFirst().SIZE;
                    int ceilingTexture = ceilingMap[(int) currentCeilingX][(int) currentCeilingY];
                    int ceilingColor = textures.get(ceilingTexture - 1).pixels[textures.get(ceilingTexture - 1).SIZE * ceilingTexY + ceilingTexX];
                    if(height - y < height)
                        pixels[width * (height - y) + x] = ceilingColor;
                    //top of walls
                    int topColor = -1;
                    if(ph > 1)
                    {
                        currentDist = (ph - 1) * height / (y - height/2.0);
                        weight = (currentDist - distPlayer) / (distWall - distPlayer);
                        currentCeilingX = weight * ceilingXWall + (1.0 - weight) * camera.xPos;
                        currentCeilingY = weight * ceilingYWall + (1.0 - weight) * camera.yPos;
                        currentCeilingX = Math.min(Math.max(currentCeilingX, 1), mapWidth - 1);
                        currentCeilingY = Math.min(Math.max(currentCeilingY, 1), mapHeight - 1);
                        ceilingTexX = (int) (currentCeilingX * textures.getFirst().SIZE) % textures.getFirst().SIZE;
                        ceilingTexY = (int) (currentCeilingY * textures.getFirst().SIZE) % textures.getFirst().SIZE;
                        int topTexture = map[(int) currentCeilingX][(int) currentCeilingY];
                        if(topTexture != 0 && map2[(int) currentCeilingX][(int) currentCeilingY] == 0)
                            topColor = textures.get(topTexture - 1).pixels[textures.get(topTexture - 1).SIZE * ceilingTexY + ceilingTexX];
                        if(topColor >= 0)
                            topColor = rgbNum((int) (0.75 * getR(topColor)), (int) (0.75 * getG(topColor)), (int) (0.75 * getB(topColor)));                //fog/color amount same as for walls
                    }
                    if(topColor >= 0)
                        pixels[width * y + x] = topColor;
                }
            }
        }
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
        color -= color/65536 * 65536;
        return color/256;
    }

    private int getB(int color)
    {
        //gets b value from rgb decimal input
        color -= color/65536 * 65536;
        color -= color/256 * 256;
        return color;
    }
}