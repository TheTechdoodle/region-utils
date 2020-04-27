package com.darkender.plugins.regionutils;

import de.md5lukas.nbt.NbtIo;
import de.md5lukas.nbt.tags.CompoundTag;

import java.io.*;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class WorldRegion
{
    private byte[] locations = new byte[4096];
    private byte[] modified = new byte[4096];
    //private OffsetSegment[] offsetSegments = new OffsetSegment[1024];
    private RandomAccessFile raf;
    private File regionFile;
    
    public WorldRegion(File regionFile)
    {
        this.regionFile = regionFile;
    }
    
    public void init()
    {
        try
        {
            raf = new RandomAccessFile(regionFile, "r");
            raf.seek(0);
            raf.readFully(locations);
            raf.readFully(modified);
            
            /*int i = 0;
            for(int z = 0; z < 32; z++)
            {
                for(int x = 0; x < 32; x++)
                {
                    offsetSegments[i] = new OffsetSegment();
                    offsetSegments[i].offset = getOffset(x, z);
                    offsetSegments[i].length = getLength(x, z);
                    offsetSegments[i].x = (byte) x;
                    offsetSegments[i].z = (byte) z;
                    
                    if(offsetSegments[i].offset == 2)
                    {
                        Bukkit.getLogger().info("i for offset 2 is: " + (((x & 31) << 2) + ((z & 31) << 7)));
                    }
                    
                    i++;
                }
            }
            Arrays.sort(offsetSegments);*/
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void close()
    {
        try
        {
            raf.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public int getOffset(int chunkX, int chunkZ)
    {
        int i = ((chunkX & 31) << 2) + ((chunkZ & 31) << 7);
        //int offset = (((locations[i] & 0xFF) << 16) + ((locations[i + 1] & 0xFF) << 8) + ((locations[i + 2] & 0xFF)));
        
        // Multiply by 4096
        //return offset << 12;
        //return offset;
        
        return (locations[i + 2] & 0xFF) | ((locations[i + 1] & 0xFF) << 8) | ((locations[i] & 0x0F) << 16);
    }
    
    public int getLength(int chunkX, int chunkZ)
    {
        int i = ((chunkX & 31) << 2) + ((chunkZ & 31) << 7);
        return locations[i + 3];
    }
    
    public byte[] getChunkCompressedData(int sectorOffset) throws IOException
    {
        raf.seek(sectorOffset * 4096);
        int size = raf.readInt();
        int compression = raf.read();
    
        byte[] data = new byte[size];
        raf.readFully(data);
        
        return data;
    }
    
    public CompoundTag getChunk(int sectorOffset, Inflater inflater) throws IOException
    {
        byte[] data = getChunkCompressedData(sectorOffset);
        CompoundTag tag;
        try(DataInputStream dis = new DataInputStream(
                new InflaterInputStream(
                        new ByteArrayInputStream(data),
                        inflater)))
        {
            tag = NbtIo.read(dis);
        }
        return tag;
    }
    
    public int getChunkSize(int chunkX, int chunkZ) throws IOException
    {
        int offset = getOffset(chunkX, chunkZ) * 4096;
        if(offset == 0 && getLength(chunkX, chunkZ) == 0)
        {
            return 0;
        }
    
        raf.seek(offset);
        return raf.readInt();
    }
    
    public byte[] getLocations()
    {
        return locations;
    }
    
    public byte[] getModified()
    {
        return modified;
    }
}
