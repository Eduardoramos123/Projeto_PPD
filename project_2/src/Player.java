import java.io.*;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Player {
    public String name;
    private String password;
    private int rank;
    private Socket socket;
    private DataInputStream dis_player;
    private DataOutputStream dos_player;
    private boolean isX;

    public Player(){
        this.name = null;
        this.password = null;
        this.rank = 0;
        this.socket = null;
        this.dis_player = null;
        this.dos_player = null;
    }


    public Player(String name, String password, Socket socket, DataInputStream dis_player, DataOutputStream dos_player, boolean isX) {
        this.name = name;
        this.password = password;
        this.rank = 0;
        this.socket = socket;
        this.dis_player = dis_player;
        this.dos_player = dos_player;
        this.isX = isX;
    }

    public Player(String name, Socket socket, DataInputStream dis_player, DataOutputStream dos_player, boolean isX) {
        this.name = name;
        this.rank = 0;
        this.socket = socket;
        this.dis_player = dis_player;
        this.dos_player = dos_player;
        this.isX = isX;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void incRank() {
        this.rank = this.rank + 1;
    }

    public void decRank() {
        if(this.rank>0)
            this.rank = this.rank -1;
    }

    public int getRank() {
        return rank;
    }

    public DataInputStream getDis() {
        return dis_player;
    }

    public void setDis_player(DataInputStream dis_player) {
        this.dis_player = dis_player;
    }

    public DataOutputStream getDos() {
        return dos_player;
    }

    public void setDos_player(DataOutputStream dos_player) {
        this.dos_player = dos_player;
    }

    public boolean isX() {
        return isX;
    }

}

