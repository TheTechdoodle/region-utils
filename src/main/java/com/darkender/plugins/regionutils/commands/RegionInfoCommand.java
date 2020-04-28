package com.darkender.plugins.regionutils.commands;

import com.darkender.plugins.regionapi.Region;
import com.darkender.plugins.regionutils.RegionUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

public class RegionInfoCommand implements CommandExecutor, TabCompleter
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
        File regionFile = RegionUtils.getRegionFile(p);
    
        Region region = null;
        try
        {
            region = new Region(regionFile);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return true;
        }
    
        int loaded = 0;
        for(int x = 0; x < 32; x++)
        {
            for(int y = 0; y < 32; y++)
            {
                if(region.getOffset(x, y) != 0)
                {
                    loaded++;
                }
            }
        }
    
        p.sendMessage(ChatColor.BLUE + "======" + ChatColor.AQUA + "  Region Information " + ChatColor.BLUE + "======");
    
        p.spigot().sendMessage(new ComponentBuilder("Region file: ")
                .color(ChatColor.GOLD)
                .append(regionFile.getName()).color(ChatColor.WHITE)
                .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, regionFile.getName()))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to copy").create()))
                .append(" (" + humanReadableByteCountBin(regionFile.length()) + ")").reset()
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(Long.toString(regionFile.length()))
                                .append(" bytes")
                                .color(ChatColor.GOLD)
                                .create()))
                .create()
        );
    
        p.sendMessage(ChatColor.GOLD + "Chunks generated: " + ChatColor.WHITE + "" + loaded + "/" + (32 * 32));
    
        int relativeX = c.getX() % 32;
        int relativeZ = c.getZ() % 32;
        p.sendMessage(ChatColor.GOLD + "Current chunk offset: " + ChatColor.WHITE + region.getOffset(relativeX, relativeZ));
        p.sendMessage(ChatColor.GOLD + "Inhabited time: " + ChatColor.WHITE + c.getInhabitedTime());
    
        try
        {
            region.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        return empty;
    }
    
    public static String humanReadableByteCountBin(long bytes)
    {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }
}