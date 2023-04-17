import java.io.*;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game_Server {
    private static ServerSocket server;
    private static int port = 9876;

    public static class Game extends Thread {
        private Socket player1;
        private Socket player2;

        DataInputStream dis_player1;
        DataOutputStream dos_player1;
        DataInputStream dis_player2;
        DataOutputStream dos_player2;

        private List<List<Character>> gameSpace = new ArrayList<>();

        public Game() {
            for (int i = 0; i < 3; i++) {
                List<Character> v = new ArrayList<>();
                for (int j = 0; j < 3; j++) {
                    v.add(' ');
                }
                gameSpace.add(v);
            }
            dis_player1 = null;
            dis_player2 = null;
            dos_player1 = null;
            dos_player2 = null;
        }

        public Game(Socket player1, Socket player2) throws IOException {
            this.player1 = player1;
            this.player2 = player2;

            this.dis_player1 = new DataInputStream(player1.getInputStream());
            this.dis_player2 = new DataInputStream(player2.getInputStream());

            this.dos_player1 = new DataOutputStream(player1.getOutputStream());
            this.dos_player2 = new DataOutputStream(player2.getOutputStream());

            for (int i = 0; i < 3; i++) {
                List<Character> v = new ArrayList<>();
                for (int j = 0; j < 3; j++) {
                    v.add(' ');
                }
                gameSpace.add(v);
            }

        }

        private boolean horizontalWin(Character sym) {
            if (gameSpace.get(0).get(0) == sym && gameSpace.get(0).get(1) == sym && gameSpace.get(0).get(2) == sym) {
                return true;
            }
            if (gameSpace.get(1).get(0) == sym && gameSpace.get(1).get(1) == sym && gameSpace.get(1).get(2) == sym) {
                return true;
            }
            if (gameSpace.get(2).get(0) == sym && gameSpace.get(2).get(1) == sym && gameSpace.get(2).get(2) == sym) {
                return true;
            }
            return false;
        }

        private boolean verticalWin(Character sym) {
            if (gameSpace.get(0).get(0) == sym && gameSpace.get(1).get(0) == sym && gameSpace.get(2).get(0) == sym) {
                return true;
            }
            if (gameSpace.get(0).get(1) == sym && gameSpace.get(1).get(1) == sym && gameSpace.get(2).get(1) == sym) {
                return true;
            }
            if (gameSpace.get(0).get(2) == sym && gameSpace.get(1).get(2) == sym && gameSpace.get(2).get(2) == sym) {
                return true;
            }
            return false;
        }

        private boolean diagonalWin(Character sym) {
            if (gameSpace.get(0).get(0) == sym && gameSpace.get(1).get(1) == sym && gameSpace.get(2).get(2) == sym) {
                return true;
            }
            if (gameSpace.get(0).get(2) == sym && gameSpace.get(1).get(1) == sym && gameSpace.get(2).get(0) == sym) {
                return true;
            }
            return false;
        }

        public boolean checkWin(Character sym) {
            if (horizontalWin(sym) || verticalWin(sym) || diagonalWin(sym)) {
                return true;
            }
            return false;
        }

        public boolean checkTie() {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (gameSpace.get(i).get(j) == ' ') {
                        return false;
                    }
                }
            }
            return true;
        }

        public void printGame() {
            System.out.println("  0 1 2");
            System.out.println("0 " + gameSpace.get(0).get(0) + "|" + gameSpace.get(0).get(1) + "|" + gameSpace.get(0).get(2));
            System.out.println("  ------");
            System.out.println("1 " + gameSpace.get(1).get(0) + "|" + gameSpace.get(1).get(1) + "|" + gameSpace.get(1).get(2));
            System.out.println("  ------");
            System.out.println("2 " + gameSpace.get(2).get(0) + "|" + gameSpace.get(2).get(1) + "|" + gameSpace.get(2).get(2));
        }

        public String getBoard() {
            String s = "";
            s += "  0 1 2\n";
            s += "0 " + gameSpace.get(0).get(0) + "|" + gameSpace.get(0).get(1) + "|" + gameSpace.get(0).get(2) + "\n";
            s += "  ------\n";
            s += "1 " + gameSpace.get(1).get(0) + "|" + gameSpace.get(1).get(1) + "|" + gameSpace.get(1).get(2) + "\n";
            s += "  ------\n";
            s += "2 " + gameSpace.get(2).get(0) + "|" + gameSpace.get(2).get(1) + "|" + gameSpace.get(2).get(2) + "\n";

            return s;
        }

        public void PlayerXPlace() {
            Scanner scn = new Scanner(System.in);
            int line = -1;

            while (line < 0 || line > 3) {
                System.out.println("Choose Line:");
                String read = scn.nextLine();
                line = Integer.parseInt(read);
            }

            int row = -1;

            while (row < 0 || row > 3) {
                System.out.println("Choose Row:");
                String read = scn.nextLine();
                row = Integer.parseInt(read);
            }

            if (gameSpace.get(line).get(row) != ' ') {
                return;
            }

            gameSpace.get(line).set(row, 'x');
        }

        public void PlayerXPlace(int line, int col) {
            if (gameSpace.get(line).get(col) != ' ') {
                return;
            }

            gameSpace.get(line).set(col, 'x');
        }

        public void PlayerOPlace() {
            Scanner scn = new Scanner(System.in);
            int line = -1;

            while (line < 0 || line > 3) {
                System.out.println("Choose Line:");
                String read = scn.nextLine();
                line = Integer.parseInt(read);
            }

            int row = -1;

            while (row < 0 || row > 3) {
                System.out.println("Choose Row:");
                String read = scn.nextLine();
                row = Integer.parseInt(read);
            }

            if (gameSpace.get(line).get(row) != ' ') {
                return;
            }

            gameSpace.get(line).set(row, 'o');
        }

        public void PlayerOPlace(int line, int col) {
            if (gameSpace.get(line).get(col) != ' ') {
                return;
            }

            gameSpace.get(line).set(col, 'o');
        }

        public void gameLoop() {

            while (true) {

                printGame();

                if (checkWin('o')) {
                    System.out.println("player O won!");
                    return;
                }
                if (checkTie()) {
                    System.out.println("Tie!");
                    return;
                }

                PlayerXPlace();

                printGame();

                if (checkWin('x')) {
                    System.out.println("player X won!");
                    return;
                }
                if (checkTie()) {
                    System.out.println("Tie!");
                    return;
                }

                PlayerOPlace();
            }
        }

        public void TCP_gameLoop() throws IOException {

            while (true) {


                dos_player1.writeUTF(getBoard());

                System.out.println("HELLLLLLLLLP");

                if (checkWin('o')) {
                    dos_player1.writeUTF("player O won!");
                    dos_player2.writeUTF("player O won!");
                    try
                    {
                        // closing resources
                        this.dis_player1.close();
                        this.dis_player2.close();
                        this.dos_player1.close();
                        this.dos_player2.close();

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }
                if (checkTie()) {
                    dos_player1.writeUTF("Tie!");
                    dos_player2.writeUTF("Tie!");
                    try
                    {
                        // closing resources
                        this.dis_player1.close();
                        this.dis_player2.close();
                        this.dos_player1.close();
                        this.dos_player2.close();

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }

                dos_player1.writeUTF("Select Line(0-2):\n");

                String received;

                received = dis_player1.readUTF();

                int line = Integer.parseInt(received);


                dos_player1.writeUTF("Select Row(0-2):\n");

                received = dis_player1.readUTF();

                int row = Integer.parseInt(received);


                PlayerXPlace(line, row);



                dos_player2.writeUTF(getBoard());


                if (checkWin('x')) {
                    dos_player1.writeUTF("player X won!");
                    dos_player2.writeUTF("player X won!");
                    try
                    {
                        // closing resources
                        this.dis_player1.close();
                        this.dis_player2.close();
                        this.dos_player1.close();
                        this.dos_player2.close();

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }
                if (checkTie()) {
                    dos_player1.writeUTF("Tie!");
                    dos_player2.writeUTF("Tie!");
                    try
                    {
                        // closing resources
                        this.dis_player1.close();
                        this.dis_player2.close();
                        this.dos_player1.close();
                        this.dos_player2.close();

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }

                dos_player2.writeUTF("Select Line(0-2):\n");

                received = dis_player2.readUTF();


                line = Integer.parseInt(received);

                dos_player2.writeUTF("Select Row(0-2):\n");

                received = dis_player2.readUTF();

                row = Integer.parseInt(received);

                PlayerOPlace(line, row);
            }
        }

        @Override
        public void run()
        {
            try {
                TCP_gameLoop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public class Player {
        public String name;
        private String password;
        private int rank;
        private Socket socket;


        public Player(String name, String password, Socket socket) {
            this.name = name;
            this.password = password;
            this.rank = 0;
            this.socket = socket;
        }

        public String getName() {
            return name;
        }

        public Socket getSocket() {
            return socket;
        }

        public String getPass() {
            return password;
        }

        public void incRank() {
            this.rank = this.rank + 1;
        }

        public void decRank() {
            this.rank = this.rank -1;
        }

        public int getRank() {
            return rank;
        }

    }

    public void start(int n) throws IOException {
        Lobby lobby = new Lobby();
        lobby.start(n);
    }
    public static class Lobby {
        private static List<Socket> players = new ArrayList<>();

        public void start(int n) throws IOException{
            server = new ServerSocket(port);

            while(true){
                System.out.println("Waiting for the client request");
                Socket socket = server.accept();
                System.out.println("Client Received!");

                System.out.println("Assigning new thread for this client");

                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                players.add(socket);

                System.out.println("Teste size: " + players.size());

                if (players.size() >= n) {
                    Thread t = new Game(players.get(0), players.get(1));

                    t.start();

                    players.remove(0);
                    players.remove(0);
                }


                // create a new thread object


                // Invoking the start() method

            }
        }


    }


}

