package games;

import com.mysql.fabric.xmlrpc.base.Array;
import net.minecraft.server.v1_15_R1.ItemCompass;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.Hash;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public class Hunter extends Game {

    public Hunter(JavaPlugin plugin) {
        super(plugin);
    }


    @Override
    public String getLeadingCommand() {
        return "hunt";
    }


    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void playerLeftServer(PlayerQuitEvent event) {
        if(playerHashMap.containsKey(event.getPlayer())){
            playerHashMap.remove(event.getPlayer());
            chickens.remove(event.getPlayer());
            hunters.remove(event.getPlayer());
            if(hunters.size() == 0 || chickens.size() == 0){
                isRunning = false;
                broadcast(ChatColor.RED, "the game has ended!");
            }
        }
    }

    @Override
    public void playerDied(PlayerDeathEvent event) {
        if(playerHashMap.containsKey(event.getEntity().getName())){

            if(chickens.contains(event.getEntity())){
                chickens.remove(event.getEntity());
                hunters.add(event.getEntity());
            }


            recalibrate();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            String cmdName = command.getName().toLowerCase();
            String senderName = sender.getName();
            Player player = (Player) sender;


            switch (cmdName){
                case "hunt_join":
                    if(isRunning) {
                        sendToPlayer(player, ChatColor.RED, "The game is already running!");
                        return true;
                    }
                    if(!playerHashMap.containsKey(senderName)){
                        playerHashMap.put(senderName, player);
                        broadcast(ChatColor.GREEN,senderName + " joined the game!");
                    }else{
                        sendToPlayer(player, ChatColor.RED, "You already joined this game!");
                    }
                    return true;
                case "hunt_leave":
                    if(playerHashMap.containsKey(senderName)){
                        broadcast(ChatColor.RED,senderName + " left the game!");
                        hunters.remove(player);
                        chickens.remove(player);
                        playerHashMap.remove(senderName);
                        recalibrate();
                    }else{
                        sendToPlayer(player, ChatColor.RED, "You are not part of this game!");
                    }
                    return true;
                case "hunt_list":
                    printInfo();
                    return true;
                case "hunt_start":
                    if(isRunning){
                        sendToPlayer(player, ChatColor.RED, "Stop the previous game first!");
                        return true;
                    }
                    if(playerHashMap.size() == 0){
                        sendToPlayer(player, ChatColor.RED, "Cannot start without players!");
                        return true;
                    }
                    if(playerHashMap.size() < initHunters){
                        sendToPlayer(player, ChatColor.RED, "Need more players in order to have " + initHunters + " hunters!");
                        return true;
                    }


                    isRunning = true;
                    Location start = getRandomSpawn(buildPlayerList().get(0).getWorld());
                    ArrayList<Player> shuffled = shuffle(buildPlayerList());
                    buildPlayerList().get(0).getWorld().setSpawnLocation(start);

                    for(Player p:shuffled){
                        p.teleport(start);
                        p.setHealth(20);
                        p.setGameMode(GameMode.SURVIVAL);
                        p.getInventory().clear();
                    }

                    hunters.clear();
                    chickens.clear();
                    hunters.addAll(shuffled.subList(0,initHunters));
                    if(initHunters < shuffled.size())
                        chickens.addAll(shuffled.subList(initHunters, shuffled.size()));



                    for(Player p:hunters){
                        p.getInventory().addItem(new ItemStack(Material.COMPASS,1));
                    }

                    recalibrate();
                    updateCompass();

                    broadcast(ChatColor.GREEN, "the game has started");
                    return true;
                case "hunt_stop":
                    if(!isRunning){
                        sendToPlayer(player, ChatColor.RED, "No game running atm!");
                    }
                    isRunning = false;
                    broadcast(ChatColor.RED, "the game has ended!");
                    return true;
                case "hunt_hunters":
                    if(!isRunning){
                        initHunters = Integer.parseInt(args[0]);
                        sendToPlayer(player, ChatColor.GREEN, "changed the amount of initial hunters to: " + initHunters);
                        return true;
                    }
            }
        }


        return false;
    }

    public void printInfo() {
        broadcast(ChatColor.YELLOW, "hunters:");
        for(Player p:hunters){
            broadcast(ChatColor.YELLOW, p.getName());
        }
        broadcast(ChatColor.YELLOW, "chickens:");
        for(Player p:chickens){
            broadcast(ChatColor.YELLOW, p.getName());
        }
    }

    public void recalibrate(){

        if(!isRunning) return;

        if(chickens.size() == 0){
            return;
        }

        targetMap.clear();

        broadcast(ChatColor.AQUA, "!!!recalibrating!!!");
        for(Player p:hunters){
            int target = (int)(Math.random() * chickens.size());

            targetMap.put(p, chickens.get(target));
            broadcast(ChatColor.YELLOW, p.getName() + " is now hunting " + chickens.get(target)+"!");
        }

    }

    public void updateCompass() {
        if(!isRunning) return;


        for(Player p:hunters){
            p.getWorld().setMonsterSpawnLimit(70);
            if(!hasCompass(p)){
                p.getInventory().addItem(new ItemStack(Material.COMPASS,1));
            }
            if(targetMap.get(p) != null)
                p.setCompassTarget(targetMap.get(p).getLocation());
        }
        timer(() -> updateCompass(), 1);
    }


    public boolean hasCompass(Player p){

        if(p.getInventory().contains(Material.COMPASS)) return true;
        return false;
    }

    private void broadcast(ChatColor color, String message){
        for(Player p:buildPlayerList()){
            sendToPlayer(p, color, message);
        }
    }

    private boolean isRunning = false;
    private int     initHunters = 1;
    private HashMap<String, Player> playerHashMap = new HashMap<>();
    private HashMap<Player, Player> targetMap = new HashMap<>();
    private ArrayList<Player> hunters = new ArrayList<>();
    private ArrayList<Player> chickens = new ArrayList<>();

    private ArrayList<Player> shuffle(ArrayList<Player> players) {
        ArrayList<Player> res = new ArrayList<>(players);
        for(int i = 0; i < players.size() * 5; i++){
            int id1 = (int)(Math.random() * players.size());
            int id2 = (int)(Math.random() * players.size());
            Player p = res.get(id1);
            res.set(id1, res.get(id2));
            res.set(id2, p);
        }
        return res;
    }


    private ArrayList<Player> buildPlayerList() {
        ArrayList<Player> players = new ArrayList<>();
        for(String s:playerHashMap.keySet()){
            players.add(playerHashMap.get(s));
        }
        return players;
    }
}
