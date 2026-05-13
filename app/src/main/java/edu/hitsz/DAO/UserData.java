package edu.hitsz.DAO;

import java.io.Serializable;

public class UserData implements Serializable {
    private int id;
    private String username;
    private int score;
    private String time;

    public UserData(int id, String username, int score, String time) {
        this.id = id;
        this.username = username;
        this.score = score;
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public int getId() {
        return id;
    }

    public String getTime() {
        return time;
    }
}
