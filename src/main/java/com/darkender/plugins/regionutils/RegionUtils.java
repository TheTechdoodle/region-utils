package com.darkender.plugins.regionutils;

import com.darkender.plugins.regionutils.commands.*;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RegionUtils extends JavaPlugin
{
    public static WorldEditPlugin worldEdit;
    
    @Override
    public void onEnable()
    {
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit") ||
                (worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit")) == null)
        {
            this.getLogger().severe("****************************************");
            this.getLogger().severe("");
            this.getLogger().severe("    This plugin depends on WorldEdit    ");
            this.getLogger().severe("         Please download it here        ");
            this.getLogger().severe("http://dev.bukkit.org/bukkit-plugins/worldedit/");
            this.getLogger().severe("");
            this.getLogger().severe("****************************************");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
    
        RegionPosCommand regionPosCommand = new RegionPosCommand();
        getCommand("rgpos").setExecutor(regionPosCommand);
        getCommand("rgpos").setTabCompleter(regionPosCommand);
    
        RegionInfoCommand regionInfoCommand = new RegionInfoCommand();
        getCommand("rginfo").setExecutor(regionInfoCommand);
        getCommand("rginfo").setTabCompleter(regionInfoCommand);
    
        RegionTPCommand regionTPCommand = new RegionTPCommand();
        getCommand("rgtp").setExecutor(regionTPCommand);
        getCommand("rgtp").setTabCompleter(regionTPCommand);
    
        SnapPosCommand snapPosCommand = new SnapPosCommand();
        getCommand("snappos").setExecutor(snapPosCommand);
        getCommand("snappos").setTabCompleter(snapPosCommand);
    
        PasteCenterCommand pasteCenterCommand = new PasteCenterCommand();
        getCommand("pastecenter").setExecutor(pasteCenterCommand);
        getCommand("pastecenter").setTabCompleter(pasteCenterCommand);
    }
    
    public static void snap(Location loc)
    {
        loc.setX(Math.round(loc.getX() * 2) / 2.0);
        loc.setY(Math.round(loc.getY() * 2) / 2.0);
        loc.setZ(Math.round(loc.getZ() * 2) / 2.0);
    
        loc.setPitch(Math.round(loc.getPitch() / 90.0) * 90);
        float yaw = Math.round(loc.getYaw() / 90.0) * 90;
        if(yaw == 360)
        {
            yaw = 0;
        }
        loc.setYaw(yaw);
    }
    
    public static File getRegionFile(World w, int regionX, int regionZ)
    {
        String regionFileName = "r." + regionX + "." + regionZ + ".mca";
        return new File(w.getWorldFolder().getPath() + "/region/" + regionFileName);
    }
    
    public static File getRegionFile(Player p)
    {
        Chunk c = p.getLocation().getChunk();
        int regionX = (int) Math.floor(c.getX() / 32.0);
        int regionZ = (int) Math.floor(c.getZ() / 32.0);
        return getRegionFile(p.getWorld(), regionX, regionZ);
    }
}
