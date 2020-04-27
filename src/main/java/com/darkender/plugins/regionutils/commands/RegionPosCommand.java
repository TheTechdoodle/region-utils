package com.darkender.plugins.regionutils.commands;

import com.darkender.plugins.regionutils.RegionUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RegionPosCommand implements CommandExecutor, TabCompleter
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
        Chunk c = p.getLocation().getChunk();
    
        int regionX = (int) Math.floor(c.getX() / 32.0);
        int regionZ = (int) Math.floor(c.getZ() / 32.0);
    
        int relativeX = c.getX() - (regionX * 32);
        int relativeZ = c.getZ() - (regionZ * 32);
    
        p.sendMessage(ChatColor.BLUE + "======" + ChatColor.AQUA + "  Region-Based Position " + ChatColor.BLUE + "======");
    
        p.sendMessage(ChatColor.GOLD + "Relative chunk x/z: " + ChatColor.WHITE + relativeX + " " + relativeZ);
        p.sendMessage(ChatColor.GOLD + "Region x/z: " + ChatColor.WHITE + regionX + " " + regionZ);
    
        p.sendMessage(ChatColor.GOLD + "Relative X: " + ChatColor.WHITE + (p.getLocation().getX() - (regionX * 512)));
        p.sendMessage(ChatColor.GOLD + "Relative Y: " + ChatColor.WHITE + p.getLocation().getY());
        p.sendMessage(ChatColor.GOLD + "Relative Z: " + ChatColor.WHITE + (p.getLocation().getZ() - (regionZ * 512)));
    
        p.sendMessage(ChatColor.GOLD + "Pitch: " + ChatColor.WHITE + p.getLocation().getPitch());
        p.sendMessage(ChatColor.GOLD + "Yaw: " + ChatColor.WHITE + p.getLocation().getYaw());
    
        Location snapped = p.getLocation().clone();
        String locStr = (snapped.getX() - (regionX * 512)) + " " + snapped.getY() + " " + (snapped.getZ() - (regionZ * 512))
                + " " + snapped.getPitch() + " " + snapped.getYaw();
        RegionUtils.snap(snapped);
        String snappedLocStr = (snapped.getX() - (regionX * 512)) + " " + snapped.getY() + " " + (snapped.getZ() - (regionZ * 512))
                + " " + snapped.getPitch() + " " + snapped.getYaw();
    
        p.spigot().sendMessage(new ComponentBuilder(" ")
                .append(ChatColor.BLUE + "[" + ChatColor.GREEN + "Snap Pos" + ChatColor.BLUE + "]")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Runs ")
                                .append("/snappos").color(ChatColor.GOLD)
                                .append("\nRounds the location to the nearest half block and nearest 90-degree view").reset()
                                .create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/snappos"))
                .append("  ").reset()
                .append(ChatColor.BLUE + "[" + ChatColor.GREEN + "TP Center" + ChatColor.BLUE + "]")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Runs ")
                                .append("/rgtp " + regionX + " " + regionZ).color(ChatColor.GOLD)
                                .append("\nTeleports to the center of the region").reset()
                                .create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rgtp " + regionX + " " + regionZ))
                .append("  ").reset()
                .append(ChatColor.BLUE + "[" + ChatColor.GREEN + "Copy" + ChatColor.BLUE + "]")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Copies ")
                                .append(locStr).color(ChatColor.GOLD)
                                .append(" to the clipboard").reset()
                                .create()))
                .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, locStr))
                .append("  ").reset()
                .append(ChatColor.BLUE + "[" + ChatColor.GREEN + "Copy Snapped" + ChatColor.BLUE + "]")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Copies ")
                                .append(snappedLocStr).color(ChatColor.GOLD)
                                .append(" to the clipboard").reset()
                                .create()))
                .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, snappedLocStr))
                .create()
        );
    
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        return null;
    }
}
