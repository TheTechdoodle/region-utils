package com.darkender.plugins.regionutils;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import de.md5lukas.nbt.tags.CompoundTag;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.zip.Inflater;

public class RegionUtils extends JavaPlugin
{
    public static WorldEditPlugin worldEdit;
    
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
    
        getCommand("rgpos").setExecutor(new CommandExecutor()
        {
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
                snap(snapped);
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
        });
        
        getCommand("rginfo").setExecutor(new CommandExecutor()
        {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
            {
                if(!(sender instanceof Player))
                {
                    sender.sendMessage("Must be a player!");
                    return false;
                }
    
                Player p = (Player) sender;
                Chunk c = p.getLocation().getChunk();
                File regionFile = getRegionFile(p);
    
                WorldRegion region = new WorldRegion(regionFile);
                region.init();
    
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
    
                try
                {
                    CompoundTag root = region.getChunk(region.getOffset(relativeX, relativeZ), new Inflater());
                    CompoundTag level = root.getCompound("Level");
        
                    p.sendMessage(ChatColor.GOLD + "Inhabited time: " + ChatColor.WHITE + level.getLong("InhabitedTime"));
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
                
                region.close();
                
                return true;
            }
        });
    
        getCommand("rgtp").setExecutor(new CommandExecutor()
        {
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
                teleportLoc.setY(256);
                teleportLoc.setZ((regionZ * 512) + 256);
                p.teleport(teleportLoc, PlayerTeleportEvent.TeleportCause.COMMAND);
            
                return true;
            }
        });
    
        getCommand("snappos").setExecutor(new CommandExecutor()
        {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
            {
                if(!(sender instanceof Player))
                {
                    sender.sendMessage("Must be a player!");
                    return false;
                }
    
                Player p = (Player) sender;
                Location snapPos = p.getLocation().clone();
                snap(snapPos);
                p.teleport(snapPos, PlayerTeleportEvent.TeleportCause.COMMAND);
                
                return true;
            }
        });
    
        getCommand("pastecenter").setExecutor(new CommandExecutor()
        {
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
                LocalSession session = worldEdit.getSession(p);
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
    
                try (EditSession editSession = session.createEditSession(worldEdit.wrapPlayer(p)))
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
        });
    }
    
    private void snap(Location loc)
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
    
    private File getRegionFile(World w, int regionX, int regionZ)
    {
        String regionFileName = "r." + regionX + "." + regionZ + ".mca";
        return new File(w.getWorldFolder().getPath() + "/region/" + regionFileName);
    }
    
    private File getRegionFile(Player p)
    {
        Chunk c = p.getLocation().getChunk();
        int regionX = (int) Math.floor(c.getX() / 32.0);
        int regionZ = (int) Math.floor(c.getZ() / 32.0);
        return getRegionFile(p.getWorld(), regionX, regionZ);
    }
}
