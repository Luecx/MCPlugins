import com.mysql.fabric.xmlrpc.base.Array;
import games.Game;
import games.Hunter;
import games.Swapper;
import net.minecraft.server.v1_15_R1.ItemCompass;
import net.minecraft.server.v1_15_R1.Position;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class Main extends JavaPlugin implements Listener {


    private ArrayList<Game> games = new ArrayList<>();

    @Override
    public void onDisable() {
        for(Game g:games){
            g.onDisable();
        }
    }

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(this, this);

        games.add(new Swapper(this));
        games.add(new Hunter(this));

        for(Game g:games){
            g.onEnable();
        }
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        String game = cmd.split("_")[0];
        for(Game g:games){
            if(g.getLeadingCommand().equals(game)){
                return g.onCommand(sender, command, label, args);
            }
        }

        return false;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for(Game g:games){
            g.playerLeftServer(event);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        for(Game g:games){
            g.playerDied(event);
        }
    }

}
