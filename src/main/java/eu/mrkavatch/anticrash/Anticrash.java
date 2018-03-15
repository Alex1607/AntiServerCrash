package eu.mrkavatch.anticrash;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class Anticrash extends JavaPlugin
{
    private ProtocolManager protocolManager;
    private String cmd = "&cDont try to crash our server please :c";
    private boolean usecmd = true;
    private int delay = 1;
    private ArrayList<Player> cooldown = new ArrayList<>();

    public void onEnable()
    {
        getConfig().addDefault("punish", "kick {player} &cDont try to crash our server please :c");
        getConfig().addDefault("usepunish", "true");
        getConfig().addDefault("delay", "1");
        getConfig().options().copyDefaults(true);
        saveConfig();
        cmd = getConfig().getString("punish");
        delay = getConfig().getInt("delay");
        usecmd = Boolean.parseBoolean(getConfig().getString("usepunish"));
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, new PacketType[]{PacketType.Play.Client.POSITION})
        {
            public void onPacketReceiving(PacketEvent event)
            {
                if (event.getPacketType().equals(PacketType.Play.Client.POSITION))
                {
                    try
                    {
                        int before = (int) event.getPlayer().getLocation().getY();
                        int next = event.getPacket().getDoubles().read(1).intValue();
                        if (before + 200 < next)
                        {
                            event.setCancelled(true);
                        }
                    } catch (Exception localException)
                    {
                    }
                }
            }
        });
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, new PacketType[]{PacketType.Play.Client.CUSTOM_PAYLOAD})
        {
            public void onPacketReceiving(PacketEvent event)
            {
                if (event.getPacketType().equals(PacketType.Play.Client.CUSTOM_PAYLOAD))
                {
                    String x = event.getPacket().getStrings().read(0);
                    if (x.equals("MC|BSign") || x.equals("MC|BEdit"))
                    {
                        if (event.getPlayer().getItemInHand().getType() == Material.BOOK_AND_QUILL)
                        {
                            return;
                        }
                        event.setCancelled(true);
                        if (usecmd == true)
                        {
                            punish(event.getPlayer());
                        }
                    }
                }
            }
        });
    }

    private void punish(Player player)
    {
        if (!cooldown.contains(player))
        {
            cooldown.add(player);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable()
            {
                @Override
                public void run()
                {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("&", "§").replace("{player}", player.getName()));
                    removeCooldown(player);
                }
            }, 0);
        }
    }

    private void removeCooldown(Player player)
    {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable()
        {
            @Override
            public void run()
            {
                cooldown.remove(player);
            }
        }, delay * 20);
    }
}
