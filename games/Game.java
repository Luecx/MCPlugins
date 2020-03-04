package games;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Game {

    protected JavaPlugin plugin;

    public Game(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void sendToPlayer(Player p, ChatColor chatColor, String message){
        p.sendMessage(chatColor + "["+getLeadingCommand().toUpperCase()+"] " + message);
    }

    public Location getRandomSpawn(World world){
        int x = (int)((Math.random() * 2 - 1) * 1E4);
        int y = (int)((Math.random() * 2 - 1) * 1E4);
        while(world.getHighestBlockAt(x,y).getLocation().getY() <= 65){
            x = (int)((Math.random() * 2 - 1) * 1E4);
            y = (int)((Math.random() * 2 - 1) * 1E4);
        }
        Location targetLocation = world.getHighestBlockAt(x,y).getLocation();
        targetLocation.setY(targetLocation.getY()+4);
        return targetLocation;
    }

    public abstract void onDisable();

    public abstract void onEnable();

    public abstract void playerLeftServer(PlayerQuitEvent event);

    public abstract void playerDied(PlayerDeathEvent event);

    public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);

    public abstract String getLeadingCommand();

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void timer(Runnable runnable, int seconds) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runnable, 20 * seconds);
    }

}
