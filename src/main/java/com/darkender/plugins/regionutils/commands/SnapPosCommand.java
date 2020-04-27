package com.darkender.plugins.regionutils.commands;

import com.darkender.plugins.regionutils.RegionUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;

public class SnapPosCommand implements CommandExecutor, TabCompleter
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
        Location snapPos = p.getLocation().clone();
        RegionUtils.snap(snapPos);
        p.teleport(snapPos, PlayerTeleportEvent.TeleportCause.COMMAND);
    
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        return empty;
    }
}