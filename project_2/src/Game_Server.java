import java.io.*;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Game_Server {
    private static ServerSocket server;
    private static int port = 9876;
    private static List<String> players_name = new ArrayList<>();
    private static List<String> players_name_ranked = new ArrayList<>();
    private static Map<String, ConWrapper> tokens = new HashMap<>();

    public static void main(String[] args) throws IOException {
        start(2);
    }



    public static class ConWrapper {
        private List<List<Character>> gameSpace;
        private Socket op_socket;

        private String op_name;

        private boolean first;


        public ConWrapper(List<List<Character>> gs, Socket oponent, String op, boolean f) {
            gameSpace = gs;
            op_socket = oponent;
            op_name = op;
            first = f;
        }

        public List<List<Character>> getGameSpace() {
            return gameSpace;
        }

        public Socket getOp_socket() {
            return op_socket;
        }

        public String getOp_name() {
            return op_name;
        }
    }

    public static class Game extends Thread {
        private Socket player1;
        private Socket player2;

        DataInputStream dis_player1;
        DataOutputStream dos_player1;
        DataInputStream dis_player2;
        DataOutputStream dos_player2;
        private String player1_name;
        private String player2_name;

        private List<List<Character>> gameSpace = new ArrayList<>();
        private boolean xfirst;
        private boolean ranked;

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
            player1_name = null;
            player2_name = null;
        }

        public Game(Socket player1, Socket player2, String name1, String name2, List<List<Character>> gs, boolean xf, boolean r) throws IOException {
            this.player1 = player1;
            this.player2 = player2;

            this.dis_player1 = new DataInputStream(player1.getInputStream());
            this.dis_player2 = new DataInputStream(player2.getInputStream());

            this.dos_player1 = new DataOutputStream(player1.getOutputStream());
            this.dos_player2 = new DataOutputStream(player2.getOutputStream());

            this.player1_name = name1;
            this.player2_name = name2;

            this.gameSpace = gs;

            this.xfirst = xf;

            this.ranked = r;
        }

        private void changeGameSpace(List<List<Character>> gs) {
            gameSpace = gs;
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
            dos_player1.writeUTF("");
            dos_player1.writeUTF(player1_name + " VS " + player2_name);
            dos_player1.writeUTF("");

            dos_player2.writeUTF("");
            dos_player2.writeUTF(player1_name + " VS " + player2_name);
            dos_player2.writeUTF("");


            while (true) {

                try {
                    dos_player1.writeUTF(getBoard());
                } catch (IOException e) {
                    player1_disc();
                    return;
                }


                if (checkWin('o')) {
                    dos_player2.writeUTF(getBoard());
                    dos_player1.writeUTF("player O won!");
                    dos_player2.writeUTF("player O won!");

                    dos_player1.writeUTF("You have been logged of Write \"Exit\" to quit!");
                    dos_player2.writeUTF("You have been logged of Write \"Exit\" to quit!");

                    tokens.remove(player1_name);
                    tokens.remove(player2_name);


                    dos_player1.writeUTF("done");
                    dos_player2.writeUTF("done");

                    if (ranked) {
                        File file2 = new File("ranked.txt");
                        Scanner scanner2 = new Scanner(file2);
                        int rank;
                        List<String> lines = new ArrayList<>();



                        while (scanner2.hasNextLine()) {
                            String line2 = scanner2.nextLine();
                            if (line2.contains(player2_name)) {
                                String[] arrOfStr = line2.split("\\| ");
                                int nr = Integer.parseInt((arrOfStr[1])) + 1;
                                line2 = player2_name + " | " + nr + "\n";
                            }
                            lines.add(line2);
                        }

                        FileWriter writerObj = new FileWriter("ranked.txt", false);
                        writerObj.write("");
                        writerObj.close();


                        FileWriter fr2 = new FileWriter(file2, true);
                        BufferedWriter br2 = new BufferedWriter(fr2);

                        for (String l : lines) {
                            br2.write(l + "\n");
                        }

                        br2.close();
                        fr2.close();
                    }

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
                    dos_player2.writeUTF(getBoard());
                    dos_player1.writeUTF("Tie!");
                    dos_player2.writeUTF("Tie!");

                    dos_player1.writeUTF("You have been logged of Write \"Exit\" to quit!");
                    dos_player2.writeUTF("You have been logged of Write \"Exit\" to quit!");

                    tokens.remove(player1_name);
                    tokens.remove(player2_name);


                    dos_player1.writeUTF("done");
                    dos_player2.writeUTF("done");
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

                String received;
                int line;
                int row;

                try {
                    dos_player1.writeUTF("Select Line(0-2):\n");
                    dos_player1.writeUTF("done");


                    received = dis_player1.readUTF();

                    line = Integer.parseInt(received);


                    dos_player1.writeUTF("Select Row(0-2):\n");
                    dos_player1.writeUTF("done");

                    received = dis_player1.readUTF();

                    row = Integer.parseInt(received);


                    PlayerXPlace(line, row);
                } catch (IOException e) {
                    player1_disc();

                    return;
                }

                try {
                    dos_player2.writeUTF(getBoard());
                } catch (IOException e) {
                    player2_disc();
                    return;
                }


                if (checkWin('x')) {
                    dos_player1.writeUTF(getBoard());
                    dos_player1.writeUTF("player X won!");
                    dos_player2.writeUTF("player X won!");

                    dos_player1.writeUTF("You have been logged of Write \"Exit\" to quit!");
                    dos_player2.writeUTF("You have been logged of Write \"Exit\" to quit!");

                    tokens.remove(player1_name);
                    tokens.remove(player2_name);


                    dos_player1.writeUTF("done");
                    dos_player2.writeUTF("done");

                    if (ranked) {
                        File file2 = new File("ranked.txt");
                        Scanner scanner2 = new Scanner(file2);
                        int rank;
                        List<String> lines = new ArrayList<>();



                        while (scanner2.hasNextLine()) {
                            String line2 = scanner2.nextLine();
                            if (line2.contains(player1_name)) {
                                String[] arrOfStr = line2.split("\\| ");
                                int nr = Integer.parseInt((arrOfStr[1])) + 1;
                                line2 = player1_name + " | " + nr + "\n";
                            }
                            lines.add(line2);
                        }

                        FileWriter writerObj = new FileWriter("ranked.txt", false);
                        writerObj.write("");
                        writerObj.close();


                        FileWriter fr2 = new FileWriter(file2, true);
                        BufferedWriter br2 = new BufferedWriter(fr2);

                        for (String l : lines) {
                            br2.write(l + "\n");
                        }

                        br2.close();
                        fr2.close();
                    }


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
                    dos_player1.writeUTF(getBoard());
                    dos_player1.writeUTF("Tie!");
                    dos_player2.writeUTF("Tie!");

                    dos_player1.writeUTF("You have been logged of Write \"Exit\" to quit!");
                    dos_player2.writeUTF("You have been logged of Write \"Exit\" to quit!");


                    tokens.remove(player1_name);
                    tokens.remove(player2_name);

                    dos_player1.writeUTF("done");
                    dos_player2.writeUTF("done");
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

                try {
                    dos_player2.writeUTF("Select Line(0-2):\n");
                    dos_player2.writeUTF("done");


                    received = dis_player2.readUTF();



                    line = Integer.parseInt(received);

                    dos_player2.writeUTF("Select Row(0-2):\n");
                    dos_player2.writeUTF("done");

                    received = dis_player2.readUTF();

                    row = Integer.parseInt(received);

                    PlayerOPlace(line, row);
                } catch (IOException e) {
                    player2_disc();
                    return;
                }


                System.out.flush();
            }
        }

        public void TCP_reverse_gameLoop() throws IOException {
            dos_player1.writeUTF("");
            dos_player1.writeUTF(player1_name + " VS " + player2_name);
            dos_player1.writeUTF("");

            dos_player2.writeUTF("");
            dos_player2.writeUTF(player1_name + " VS " + player2_name);
            dos_player2.writeUTF("");


            while (true) {
                try {
                    dos_player2.writeUTF(getBoard());
                } catch (IOException e) {
                    player2_disc();
                    return;
                }


                if (checkWin('x')) {
                    dos_player1.writeUTF(getBoard());
                    dos_player1.writeUTF("player X won!");
                    dos_player2.writeUTF("player X won!");

                    dos_player1.writeUTF("You have been logged of Write \"Exit\" to quit!");
                    dos_player2.writeUTF("You have been logged of Write \"Exit\" to quit!");

                    tokens.remove(player1_name);
                    tokens.remove(player2_name);


                    dos_player1.writeUTF("done");
                    dos_player2.writeUTF("done");

                    if (ranked) {
                        File file2 = new File("ranked.txt");
                        Scanner scanner2 = new Scanner(file2);
                        int rank;
                        List<String> lines = new ArrayList<>();



                        while (scanner2.hasNextLine()) {
                            String line2 = scanner2.nextLine();
                            if (line2.contains(player1_name)) {
                                String[] arrOfStr = line2.split("\\| ");
                                int nr = Integer.parseInt((arrOfStr[1])) + 1;
                                line2 = player1_name + " | " + nr + "\n";
                            }
                            lines.add(line2);
                        }

                        FileWriter writerObj = new FileWriter("ranked.txt", false);
                        writerObj.write("");
                        writerObj.close();


                        FileWriter fr2 = new FileWriter(file2, true);
                        BufferedWriter br2 = new BufferedWriter(fr2);

                        for (String l : lines) {
                            br2.write(l + "\n");
                        }

                        br2.close();
                        fr2.close();
                    }



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
                    dos_player1.writeUTF(getBoard());
                    dos_player1.writeUTF("Tie!");
                    dos_player2.writeUTF("Tie!");

                    dos_player1.writeUTF("You have been logged of Write \"Exit\" to quit!");
                    dos_player2.writeUTF("You have been logged of Write \"Exit\" to quit!");


                    tokens.remove(player1_name);
                    tokens.remove(player2_name);

                    dos_player1.writeUTF("done");
                    dos_player2.writeUTF("done");
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

                String received;
                int line;
                int row;

                try {
                    dos_player2.writeUTF("Select Line(0-2):\n");
                    dos_player2.writeUTF("done");


                    received = dis_player2.readUTF();



                    line = Integer.parseInt(received);

                    dos_player2.writeUTF("Select Row(0-2):\n");
                    dos_player2.writeUTF("done");

                    received = dis_player2.readUTF();

                    row = Integer.parseInt(received);

                    PlayerOPlace(line, row);
                } catch (IOException e) {
                    player2_disc();
                    return;
                }

                try {
                    dos_player1.writeUTF(getBoard());
                } catch (IOException e) {
                    player1_disc();
                    return;
                }


                if (checkWin('o')) {
                    dos_player2.writeUTF(getBoard());
                    dos_player1.writeUTF("player O won!");
                    dos_player2.writeUTF("player O won!");

                    dos_player1.writeUTF("You have been logged of Write \"Exit\" to quit!");
                    dos_player2.writeUTF("You have been logged of Write \"Exit\" to quit!");

                    tokens.remove(player1_name);
                    tokens.remove(player2_name);


                    dos_player1.writeUTF("done");
                    dos_player2.writeUTF("done");

                    if (ranked) {
                        File file2 = new File("ranked.txt");
                        Scanner scanner2 = new Scanner(file2);
                        int rank;
                        List<String> lines = new ArrayList<>();



                        while (scanner2.hasNextLine()) {
                            String line2 = scanner2.nextLine();
                            if (line2.contains(player2_name)) {
                                String[] arrOfStr = line2.split("\\| ");
                                int nr = Integer.parseInt((arrOfStr[1])) + 1;
                                line2 = player2_name + " | " + nr + "\n";
                            }
                            lines.add(line2);
                        }

                        FileWriter writerObj = new FileWriter("ranked.txt", false);
                        writerObj.write("");
                        writerObj.close();


                        FileWriter fr2 = new FileWriter(file2, true);
                        BufferedWriter br2 = new BufferedWriter(fr2);

                        for (String l : lines) {
                            br2.write(l + "\n");
                        }

                        br2.close();
                        fr2.close();
                    }



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
                    dos_player2.writeUTF(getBoard());
                    dos_player1.writeUTF("Tie!");
                    dos_player2.writeUTF("Tie!");

                    dos_player1.writeUTF("You have been logged of Write \"Exit\" to quit!");
                    dos_player2.writeUTF("You have been logged of Write \"Exit\" to quit!");

                    tokens.remove(player1_name);
                    tokens.remove(player2_name);


                    dos_player1.writeUTF("done");
                    dos_player2.writeUTF("done");
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

                try {
                    dos_player1.writeUTF("Select Line(0-2):\n");
                    dos_player1.writeUTF("done");


                    received = dis_player1.readUTF();

                    line = Integer.parseInt(received);


                    dos_player1.writeUTF("Select Row(0-2):\n");
                    dos_player1.writeUTF("done");

                    received = dis_player1.readUTF();

                    row = Integer.parseInt(received);


                    PlayerXPlace(line, row);
                } catch (IOException e) {
                    player1_disc();

                    return;
                }


                System.out.flush();
            }
        }

        public void player1_disc() throws IOException {
            ConWrapper wrapper = new ConWrapper(gameSpace, player2, player2_name, true);

            System.out.println(player1_name);

            tokens.put(player1_name, wrapper);


            dos_player2.writeUTF("Player " + player1_name + " Disconnected... Waiting to reconnect");


            return;
        }

        public void player2_disc() throws IOException {
            ConWrapper wrapper = new ConWrapper(gameSpace, player1, player1_name, false);

            System.out.println(player2_name);

            tokens.put(player2_name, wrapper);

            dos_player1.writeUTF("Player " + player2_name + " Disconnected... Waiting to reconnect");

            return;
        }



        @Override
        public void run()
        {
            try {
                if (xfirst) {
                    TCP_gameLoop();
                }
                else {
                    TCP_reverse_gameLoop();
                }
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

    public static void start(int n) throws IOException {
        Lobby lobby = new Lobby();
        lobby.start(n);
    }
    public static class Lobby {
        private static List<Socket> players = new ArrayList<>();
        private static List<Socket> players_ranked = new ArrayList<>();
        private static List<Integer> ranks = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(100); // Create a thread pool with 10 threads

        public void start(int n) throws IOException{
            server = new ServerSocket(port);

            while(true){
                System.out.println("Waiting for the client request");
                Socket socket = server.accept();
                System.out.println("Client Received!");

                System.out.println("Assigning new thread for this client");



                //authenticate
                // 0. Class Super vai ter um mapa com conta/token se tiver 1 quer dizer que a ligação caiu
                // 0.1 func que recupera sessão
                // 1. verifica se ja tem conta: file.txt nome | pass
                // 2. Se tiver fixe, dalhe token
                // 3. Regista e mete no file.txt e log-in


                new Thread() {
                    public void run() {
                        try {
                            fila(socket, n);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }.start();


                //auth.logout();


                // create a new thread object


                // Invoking the start() method

            }
        }

        public void fila(Socket socket, int n) throws IOException, InterruptedException {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            Authentication auth = new Authentication(socket);
            int check = auth.menu();

            if (check == 2) {
                fila_ranked(socket, n, auth);
                return;
            }

            if (check == 1) {
                if (tokens.get(auth.name).first) {
                    Runnable gameTask = new Game(socket, tokens.get(auth.name).op_socket, auth.name, tokens.get(auth.name).op_name, tokens.get(auth.name).gameSpace, true, false);
                    executor.submit(gameTask);
                }
                else {
                    Runnable gameTask = new Game(tokens.get(auth.name).op_socket, socket, tokens.get(auth.name).op_name, auth.name, tokens.get(auth.name).gameSpace, false, false);
                    executor.submit(gameTask);
                }



                return;
            }

            dos.writeUTF("In queue!");

            // Lock objects for synchronization
            Lock playersLock = new ReentrantLock();
            Lock playersNameLock = new ReentrantLock();
            Lock tokensLock = new ReentrantLock();

            playersLock.lock();
            try {
                players.add(socket);
            } finally {
                playersLock.unlock();
            }

            playersNameLock.lock();
            try {
                players_name.add(auth.name);
            } finally {
                playersNameLock.unlock();
            }


            System.out.println("Teste size: " + players.size());

            if (players.size() >= n) {
                dos.writeUTF("Starting Game...");

                List<List<Character>> gameSpace = new ArrayList<>();

                for (int i = 0; i < 3; i++) {
                    List<Character> v = new ArrayList<>();
                    for (int j = 0; j < 3; j++) {
                        v.add(' ');
                    }
                    gameSpace.add(v);
                }

                Runnable gameTask = new Game(players.get(0), players.get(1), players_name.get(0), players_name.get(1), gameSpace, true, false);
                executor.submit(gameTask);

                playersLock.lock();
                try {
                    players.remove(0);
                    players.remove(0);
                } finally {
                    playersLock.unlock();
                }

                tokensLock.lock();
                try {
                    tokens.remove(players_name.get(0));
                    tokens.remove(players_name.get(1));
                } finally {
                    tokensLock.unlock();
                }

                playersNameLock.lock();
                try {
                    players_name.remove(0);
                    players_name.remove(0);
                } finally {
                    playersNameLock.unlock();
                }





            }

            playersNameLock.lock();
            try {
                while (waiting(auth.name)) {
                    try {
                        dos.writeUTF("ping");
                    } catch (IOException e) {
                        break;
                    }
                }
            } finally {
                playersNameLock.unlock();
            }


            int index = getIndexToRemove(auth.name);
            if (index != -1) {

                playersLock.lock();
                try {
                    players.remove(index);
                } finally {
                    playersLock.unlock();
                }

                playersNameLock.lock();
                try {
                    players_name.remove(index);
                } finally {
                    playersNameLock.unlock();
                }

            }

            //keepAlive(socket, auth.name);
        }

        public int getNumberRanks(int rank) {
            int res = 0;
            for (int r : ranks) {
                if (r == rank) {
                    res++;
                }
            }

            return res;
        }

        public void fila_ranked(Socket socket, int n, Authentication auth) throws IOException, InterruptedException {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());


            dos.writeUTF("In Ranked queue!");
            Lock playersRankedLock = new ReentrantLock();
            Lock playersNameRankedLock = new ReentrantLock();
            Lock rankedLock = new ReentrantLock();
            Lock tokensLock = new ReentrantLock();

            int index = ranks.size();

            playersRankedLock.lock();
            try {
                players_ranked.add(socket);
            } finally {
                playersRankedLock.unlock();
            }

            playersNameRankedLock.lock();
            try {
                players_name_ranked.add(auth.name);
            } finally {
                playersNameRankedLock.unlock();
            }

            rankedLock.lock();
            try {
                ranks.add(auth.rank);
            } finally {
                rankedLock.unlock();
            }

            System.out.println("Teste size: " + players_ranked.size());

            int interval = 0;
            int curr_rank = auth.rank;
            List<Integer> indexes = new ArrayList<>();

            if (getNumberRanks(curr_rank) >= n) {


                int z = 0;
                for (int r : ranks) {
                    if (r == curr_rank) {
                        indexes.add(z);
                    }

                    z++;
                }

                dos.writeUTF("Starting Ranked Game...");

                List<List<Character>> gameSpace = new ArrayList<>();

                for (int i = 0; i < 3; i++) {
                    List<Character> v = new ArrayList<>();
                    for (int j = 0; j < 3; j++) {
                        v.add(' ');
                    }
                    gameSpace.add(v);
                }

                Runnable gameTask = new Game(players_ranked.get(indexes.get(0)), players_ranked.get(indexes.get(1)), players_name_ranked.get(indexes.get(0)), players_name_ranked.get(indexes.get(1)), gameSpace, true, true);
                executor.submit(gameTask);

                playersRankedLock.lock();
                try {
                    players_ranked.remove(players_ranked.get(indexes.get(0)));
                    players_ranked.remove(players_ranked.get(indexes.get(1)));
                } finally {
                    playersRankedLock.unlock();
                }

                tokensLock.lock();
                try {
                    tokens.remove(players_name_ranked.get(indexes.get(0)));
                    tokens.remove(players_name_ranked.get(indexes.get(1)));
                } finally {
                    tokensLock.unlock();
                }

                playersNameRankedLock.lock();
                try {
                    players_name_ranked.remove(players_name_ranked.get(indexes.get(0)));
                    players_name_ranked.remove(players_name_ranked.get(indexes.get(1)));
                } finally {
                    playersNameRankedLock.unlock();
                }

                rankedLock.lock();
                try {
                    ranks.remove(indexes.get(0));
                    ranks.remove(indexes.get(1) - 1);
                } finally {
                    rankedLock.unlock();
                }



            }

            playersNameRankedLock.lock();
            try {
                while (waiting_ranked(auth.name)) {
                    try {
                        dos.writeUTF("ping");
                    } catch (IOException e) {
                        break;
                    }
                }
            } finally {
                playersNameRankedLock.unlock();
            }

            playersRankedLock.lock();
            try {
                players_ranked.remove(socket);
            } finally {
                playersRankedLock.unlock();
            }

            playersNameRankedLock.lock();
            try {
                players_name_ranked.remove(auth.name);
            } finally {
                playersNameRankedLock.unlock();
            }

            rankedLock.lock();
            try {
                ranks.remove(index);
            } finally {
                rankedLock.unlock();
            }

        }

        public boolean waiting(String name) {
            Lock playersNameLock = new ReentrantLock();
            playersNameLock.lock();
            try {
                Iterator<String> iterator = players_name.iterator();
                while (iterator.hasNext()) {
                    String n = iterator.next();
                    if (name.equals(n)) {
                        return true;
                    }
                }
                return false;
            } finally {
                playersNameLock.unlock();
            }
        }

        public boolean waiting_ranked(String name) {
            Lock playersNameRankedLock = new ReentrantLock();
            playersNameRankedLock.lock();
            try {
                Iterator<String> iterator = players_name_ranked.iterator();
                while (iterator.hasNext()) {
                    String n = iterator.next();
                    if (name.equals(n)) {
                        return true;
                    }
                }
                return false;
            } finally {
                playersNameRankedLock.unlock();
            }

        }

        public int getIndexToRemove(String name) {
            int i = 0;
            for (String str : players_name) {
                if (name.equals(str)) {
                    return i;
                }
            }

            return -1;
        }


        public void keepAlive(Socket socket, String name) throws IOException {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            while (true) {
                try {
                    dos.writeUTF("ping");
                } catch (IOException e) {
                    int index = getIndexToRemove(name);

                    players.remove(index);
                    players_name.remove(index);

                    break;
                }
            }

            return;
        }


    }

    public static class Authentication{
        // protocol for user registration
        // persist the registration data in a file.
        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;
        public String name;
        public Integer rank;
        public boolean ranked_match;


        public Authentication(Socket socket) throws IOException {
            this.socket = socket;

            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.name = null;
        }

        public int menu() throws IOException {
            while (true) {
                dos.writeUTF("");
                dos.writeUTF("--------------------------");
                dos.writeUTF("        Tic-Tac-Toe");
                dos.writeUTF("--------------------------");
                dos.writeUTF("");
                dos.writeUTF("Choose mode:");
                dos.writeUTF("    1 - Register");
                dos.writeUTF("    2 - Login");
                dos.writeUTF("done");

                String received;

                received = dis.readUTF();

                int line = Integer.parseInt(received);

                System.out.println(line);

                if (line == 1) {
                    if (register()) {
                        dos.writeUTF("Registration Complete");
                        break;
                    }
                }

                if (line == 2) {
                    if (login()) {
                        if (tokens.containsKey(name)) {
                            System.out.println("IT IS WORKING");
                            return 1;
                        }

                        break;
                    }
                }

            }
            dos.writeUTF("");
            dos.writeUTF("--------------------------");
            dos.writeUTF("      Queue Selection");
            dos.writeUTF("--------------------------");
            dos.writeUTF("");
            dos.writeUTF("Choose mode:");
            dos.writeUTF("    1 - Simple");
            dos.writeUTF("    2 - Ranked");
            dos.writeUTF("done");

            String received;

            received = dis.readUTF();

            int line = Integer.parseInt(received);

            System.out.println(line);

            if (line == 1) {
                ranked_match = false;
                return 0;
            }
            ranked_match = true;
            return 2;
        }

        public boolean register() throws IOException {

            dos.writeUTF("");
            dos.writeUTF("--------------------------");
            dos.writeUTF("        Register");
            dos.writeUTF("--------------------------");
            dos.writeUTF("Username: ");
            dos.writeUTF("done");

            String name;

            name = dis.readUTF();

            this.name = name;



            dos.writeUTF("Password: ");
            dos.writeUTF("done");

            String pass;

            pass = dis.readUTF();

            String fin = "\n" + name + " | " + pass + "\n";

            File file = new File("users.txt");
            FileWriter fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(fin);

            br.close();
            fr.close();

            String fin2 = "\n" + name + " | " + 0 + "\n";

            File file2 = new File("ranked.txt");
            FileWriter fr2 = new FileWriter(file2, true);
            BufferedWriter br2 = new BufferedWriter(fr2);
            br2.write(fin2);

            br2.close();
            fr2.close();

            rank = 0;

            return true;
        }

        public boolean login() throws IOException {

            dos.writeUTF("");
            dos.writeUTF("--------------------------");
            dos.writeUTF("        Login");
            dos.writeUTF("--------------------------");
            dos.writeUTF("Username: ");
            dos.writeUTF("done");

            String name;

            name = dis.readUTF();

            this.name = name;


            dos.writeUTF("Password: ");
            dos.writeUTF("done");

            String pass;

            pass = dis.readUTF();

            String fin = name + " | " + pass;



            File file = new File("users.txt");
            Scanner scanner = new Scanner(file);

            int lineNum = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineNum++;
                if(line.equals(fin)) {
                    dos.writeUTF("Login Successful!");

                    File file2 = new File("ranked.txt");
                    Scanner scanner2 = new Scanner(file2);

                    while (scanner2.hasNextLine()) {
                        String line2 = scanner2.nextLine();
                        if (line2.contains(name)) {
                            String[] arrOfStr = line2.split("\\| ");
                            rank = Integer.valueOf(arrOfStr[1]);

                            System.out.println("Rank: " + rank);
                        }
                    }







                    return true;
                }
            }

            dos.writeUTF("Login Failed!");
            return false;
        }

        public void logout() throws IOException {
            dos.writeUTF("");
            dos.writeUTF("You Have been Logged out!");
            dos.writeUTF("Please write \"Exit\"");
            dos.writeUTF("done");
        }

    }

}

