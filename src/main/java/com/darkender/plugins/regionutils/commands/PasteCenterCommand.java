package com.darkender.plugins.regionutils.commands;

import com.darkender.plugins.regionutils.RegionUtils;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PasteCenterCommand implements CommandExecutor, TabCompleter
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
    
        if(args.length < 1)
        {
            sender.sendMessage(ChatColor.RED + "Must specify bottom Y-level for paste");
            return false;
        }
    
        int bottomY;
        try
        {
            bottomY = Integer.parseInt(args[0]);
        }
        catch(NumberFormatException e)
        {
            sender.sendMessage(ChatColor.RED + "Invalid bottom Y-level!");
            return false;
        }
    
        Player p = (Player) sender;
        LocalSession session = RegionUtils.worldEdit.getSession(p);
        ClipboardHolder clipHolder;
        Clipboard clip;
        try
        {
            clipHolder = session.getClipboard();
            clip = clipHolder.getClipboard();
        }
        catch(EmptyClipboardException e)
        {
            sender.sendMessage(ChatColor.RED + "Please copy to clipboard first");
            return false;
        }
    
        clip.setOrigin(clip.getMinimumPoint());
    
        Chunk c = p.getLocation().getChunk();
        int regionX = (int) Math.floor(c.getX() / 32.0);
        int regionZ = (int) Math.floor(c.getZ() / 32.0);
        int centerX = (regionX * 512) + 256;
        int centerZ = (regionZ * 512) + 256;
    
        int lengthX = clip.getMaximumPoint().getBlockX() - clip.getMinimumPoint().getBlockX();
        int lengthZ = clip.getMaximumPoint().getBlockZ() - clip.getMinimumPoint().getBlockZ();
    
        try (EditSession editSession = session.createEditSession(RegionUtils.worldEdit.wrapPlayer(p)))
        {
            Operation operation = new ClipboardHolder(clip)
                    .createPaste(editSession)
                    .to(BlockVector3.at(centerX - (lengthX / 2), bottomY, centerZ - (lengthZ / 2)))
                    .copyBiomes(true)
                    .copyEntities(true)
                    .build();
            Operations.complete(operation);
        }
        catch(WorldEditException e)
        {
            e.printStackTrace();
        }
    
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
    {
        if(args.length > 1 || !(sender instanceof Player))
        {
            return empty;
        }
        return Collections.singletonList(Integer.toString(((Player) sender).getLocation().getBlockY()));
    }
}