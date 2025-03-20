package com.main;

import java.util.ArrayList;

public class PlayerManager {
    private static PlayerManager instance;

    private ArrayList<Player> players;
    private int currentPlayerIndex = 0;

    private PlayerManager() {
        players = new ArrayList<>();
    }

    // get the singleton instance
    public static PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    public Player getCurrentPlayer(){
        return players.get(currentPlayerIndex);
    }

    public void updateCurrentPlayer(int index) {
        currentPlayerIndex = index;
    }

    // add a player to the list
    public void addPlayer(Player player) {
        players.add(player);
    }

    // get the list of players
    public ArrayList<Player> getPlayers() {
        return players;
    }

    // clear the player list
    public void clearPlayers() {
        players.clear();
    }

    // get a specific player by index or name
    public Player getPlayer(int index) {
        if (index >= 0 && index < players.size()) {
            return players.get(index);
        }
        return null;
    }

    public Player getPlayerByName(String name) {
        for (Player player : players) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        return null;
    }

    // remove a player by index
    public void removePlayer(int index) {
        if (index >= 0 && index < players.size()) {
            players.remove(index);
        }
    }

    public void reset() {
        players.clear(); // Clear the list of players
        // Add any other reset logic here
    }

    // check if a player exists by name
    public boolean playerExists(String name) {
        return getPlayerByName(name) != null;
    }
}
