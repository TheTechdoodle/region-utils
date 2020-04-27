package com.darkender.plugins.regionutils.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;

public class RegionTPCommand implements CommandExecutor, TabCompleter
{
    private final List<String> empty = new ArrayList<>();
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(!(sender instanceof Player))
        {
            sender.sendMessage("Must be a player!");
            return false;
        }
    
        Player p = (Player) sender;
        int regionX, regionZ;
    
        if(args.length < 2)
        {
            Chunk c = p.getLocation().getChunk();
            regionX = (int) Math.floor(c.getX() / 32.0);
            regionZ = (int) Math.floor(c.getZ() / 32.0);
        }
        else
        {
            try
            {
                regionX = Integer.parseInt(args[0]);
            }
            catch(NumberFormatException e)
            {
                sender.sendMessage(ChatColor.RED + "Invalid X coordinate!");
                return false;
            }
        
            try
            {
                regionZ = Integer.parseInt(args[1]);
            }
            catch(NumberFormatException e)
            {
                sender.sendMessage(ChatColor.RED + "Invalid Z coordinate!");
                return false;
            }
        }
    
        Location teleportLoc = p.getLocation().clone();
        teleportLoc.setX((regionX * 512) + 256);
        teleportLoc.setZ((regionZ * 512) + 256);
        teleportLoc.getChunk().load();
        teleportLoc.setY(teleportLoc.getWorld().getHighestBlockYAt(teleportLoc, HeightMap.MOTION_BLOCKING));
        
        p.teleport(teleportLoc, PlayerTeleportEvent.TeleportCause.COMMAND);
    
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        return empty;
    }
}