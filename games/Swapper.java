package games;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public class Swapper extends Game{



    private long _resetTime = 60 * 5;
    private long _secondsLeft = 0;
    private boolean _isRunning = false;

    public Swapper(JavaPlugin plugin) {
        super(plugin);
    }


    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {
        plugin.getServer().setDefaultGameMode(GameMode.SURVIVAL);
        plugin.getLogger().info("swapping game plugin activated!");
    }

    @Override
    public void playerLeftServer(PlayerQuitEvent event) {
        if(playerHashMap.containsKey(event.getPlayer().getName())){
            playerHashMap.remove(event.getPlayer().getName());
            if(_isRunning){
                broadcast(ChatColor.RED, event.getPlayer().getName()+ " left the server. Stopping the game!");
                _isRunning = false;
            }
        }
    }

    @Override
    public void playerDied(PlayerDeathEvent event) {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            String cmdName = command.getName().toLowerCase();
            String senderName = sender.getName();
            Player player = (Player)sender;

            switch (cmdName){
                case "swp_join":
                    if(playerHashMap.containsKey(senderName)){
                        sendToPlayer(player, ChatColor.RED, "you have already joined!");
                        return true;
                    }
                    playerHashMap.put(senderName, (Player) sender);
                    broadcast(ChatColor.GREEN, senderName+ " joined the game!");
                    return true;
                case "swp_leave":
                    if(!playerHashMap.containsKey(senderName)){
                        sendToPlayer(player, ChatColor.RED,"you are not part of the game!");
                        return true;
                    }
                    broadcast(ChatColor.GREEN, senderName+ " left the game!");
                    playerHashMap.remove(senderName);
                    if(playerHashMap.size() == 0 && _isRunning){
                        broadcast(ChatColor.RED, "stopping the game");
                        _isRunning = false;
                    }
                    return true;
                case "swp_list":
                    sendToPlayer(player, ChatColor.YELLOW,"player list: ");
                    for(String s:playerHashMap.keySet()){
                        sendToPlayer(player, ChatColor.YELLOW, "  " + s);
                    }
                    return true;
                case "swp_start":
                    if(_isRunning) {
                        sendToPlayer(player, ChatColor.RED, "end the previous game first!");
                        return true;
                    }
                    if(playerHashMap.size() == 0){
                        sendToPlayer(player, ChatColor.RED, "Cannot start the game without players");
                    }
                    World world = buildPlayerList().get(0).getWorld();
                    int x = (int)((Math.random() * 2 - 1) * 1E4);
                    int y = (int)((Math.random() * 2 - 1) * 1E4);
                    while(world.getHighestBlockAt(x,y).getLocation().getY() <= 65){
                        x = (int)((Math.random() * 2 - 1) * 1E4);
                        y = (int)((Math.random() * 2 - 1) * 1E4);
                    }
                    Location targetLocation = buildPlayerList().get(0).getWorld().getHighestBlockAt(x,y).getLocation();
                    targetLocation.setY(targetLocation.getY()+4);
                    for(Player p:buildPlayerList()){
                        p.setHealth(20);
                        p.setGameMode(GameMode.SURVIVAL);
                        p.getInventory().clear();
                        p.teleport(targetLocation);
                    }
                    _isRunning = true;
                    _secondsLeft = _resetTime;
                    broadcast(ChatColor.GREEN,"The game has started! Good Luck!");
                    timer();
                    return true;
                case "swp_stop":
                    if(!_isRunning){
                        sendToPlayer(player, ChatColor.RED, "start a game first!");
                        return true;
                    }
                    broadcast(ChatColor.GREEN,"The game has been stopped!");
                    _isRunning = false;
                    return true;
                case "swp_time":
                    Long time = Long.parseLong(args[0]);
                    for(Player p:buildPlayerList()){
                        sendToPlayer(player, ChatColor.YELLOW,"set the time to: " + time + " ms");
                    }
                    _resetTime = time;
                    return true;
                case "swp_left":
                    sendToPlayer(player, ChatColor.GREEN, "time left: " + _secondsLeft);
                    return true;
            }
        }
        return false;
    }


    private void broadcast(ChatColor color, String message){
        for(Player p:buildPlayerList()){
            sendToPlayer(p, color, message);
        }
    }

    public void timer() {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                                                    secondPassed();
                                                                }
                , 20);
    }

    public void secondPassed(){

        _secondsLeft --;
        for(Player p:buildPlayerList()){
            p.getWorld().setTime(1000);
            p.getWorld().setStorm(false);
        }
        if(_secondsLeft == 30 || _secondsLeft == 20 || _secondsLeft <= 10){
            plugin.getServer().broadcastMessage(ChatColor.GREEN + "" + _secondsLeft + " seconds left!");
        }
        if(_secondsLeft == 0){
            plugin.getServer().broadcastMessage(ChatColor.GREEN+"Swapping the player!");
            ArrayList<Player> players = buildPlayerList();
            ArrayList<Player> targets = shuffle(players);
            ArrayList<Location> positions = new ArrayList<>();
            for(Player p:targets){
                positions.add(p.getLocation());
            }
            for(int i = 0; i < players.size(); i++){
                players.get(i).teleport(positions.get((i+1) % players.size()));
                _secondsLeft = _resetTime;
            }
        }
        if(_isRunning)
            timer();
    }


    private HashMap<String, Player> playerHashMap = new HashMap<>();

    private ArrayList<Player> buildPlayerList() {
        ArrayList<Player> players = new ArrayList<>();
        for(String s:playerHashMap.keySet()){
            players.add(playerHashMap.get(s));
        }
        return players;
    }

    private boolean isGoodMapping(ArrayList<Player> a, ArrayList<Player> b){
        for(int i = 0; i < a.size(); i++){
            if(a.get(i) != b.get(i)){
                return false;
            }
        }
        return true;
    }

    private ArrayList<Player> shuffle(ArrayList<Player> players) {
        ArrayList<Player> res = new ArrayList<>(players);
        while(!isGoodMapping(res, players)){
            int id1 = (int)(Math.random() * players.size());
            int id2 = (int)(Math.random() * players.size());
            Player p = res.get(id1);
            res.set(id1, res.get(id2));
            res.set(id2, p);
        }
        return res;
    }



    @Override
    public String getLeadingCommand() {
        return "swp";
    }
}
