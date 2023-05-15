import java.io.*;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.Attributes.Name;
import java.nio.file.Paths;
import java.io.BufferedWriter;

public class Game_Server {
    private static ServerSocket server;
    private static int port = 9877;
    //private static List<String> players_name = new ArrayList<>();
    private static Map<String, ConWrapper> tokens = new HashMap<>();
    private static List<Player> active_players = new ArrayList<>();


    public static void main(String[] args) throws IOException {
        start(2);
    }


    public static class ConWrapper {
        private List<List<Character>> gameSpace;
        private Socket op_socket;

        private String op_name;

        private boolean first; //is the the current player is the first player in the game??


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
        
        private List<List<Character>> gameSpace = new ArrayList<>();

        Player player_1;
        Player player_2;

        public Game() {
            for (int i = 0; i < 3; i++) {
                List<Character> v = new ArrayList<>();
                for (int j = 0; j < 3; j++) {
                    v.add(' ');
                }
                gameSpace.add(v);
            }

            player_1 = new Player();
            player_2 = new Player();
        }

        public Game(Socket player1, Socket player2, String name1, String name2, List<List<Character>> gs, boolean xf) throws IOException {
            player_1 = new Player(name1, player1, new DataInputStream(player1.getInputStream()), new DataOutputStream(player1.getOutputStream()), xf);
            player_2 = new Player(name2, player2, new DataInputStream(player2.getInputStream()), new DataOutputStream(player2.getOutputStream()), !xf);
            player_1.setRank(getRankByName(player_1.getName()));

            this.gameSpace = gs;
        }

        public static int getRankByName(String player_name) throws FileNotFoundException{
            File file = new File("rank.txt");
            Scanner scanner = new Scanner(file);

            while(scanner.hasNextLine()){
                String line = scanner.nextLine();

                String[] parts = line.split("\\|");
                String name = parts[0].trim();
                int rank = Integer.parseInt(parts[1].trim());

                if (name.equals(player_name)) {
                    return rank;
                }
            }
            return 0;
        }

        private void updateRankInFile(String name, int newRank) throws IOException{
            File file = new File("rank.txt");
            Scanner scanner = new Scanner(file);

            List<String> lines = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lines.add(line);
            }

            int index=-1;
            for(int i=0; i< lines.size();i++){
                if(lines.get(i).startsWith(name)){
                    index=i;
                    break;
                }
            }

            if (index != -1) {
                String oldLine = lines.get(index);
                String newLine = name + " | " + newRank;
                lines.set(index, newLine);

                FileWriter writer = new FileWriter(file);
                for (String line : lines) {
                    writer.write(line + "\n");
                }
                writer.close();

            } else {
                System.out.println("Username not found in file");
            }

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
                    // player 2 (0) wins
                    if(player_1.isX()){
                        player_1.decRank();
                        player_2.incRank();
                    }
                    // player 1 (0) wins
                    else{
                        player_1.incRank();
                        player_2.decRank();
                    }
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
                    // player 1 (x) wins
                    if(player_1.isX()){
                        player_1.incRank();
                        player_2.decRank();
                    }
                    // player 2 (x) wins
                    else{
                        player_1.decRank();
                        player_2.incRank();
                    }
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
            player_1.getDos().writeUTF("");
            player_1.getDos().writeUTF(player_1.getName() + " VS " + player_2.getName());
            player_1.getDos().writeUTF("");

            player_2.getDos().writeUTF("");
            player_2.getDos().writeUTF(player_1.getName() + " VS " + player_2.getName());
            player_2.getDos().writeUTF("");


            while (true) {

                try {
                    player_1.getDos().writeUTF(getBoard());
                } catch (IOException e) {
                    player1_disc();
                    return;
                }


                if (checkWin('o')) {
                    player_2.getDos().writeUTF(getBoard());
                    player_1.getDos().writeUTF("player O won!");
                    player_2.getDos().writeUTF("player O won!");

                    // player 2 (o) wins
                    if(player_1.isX()){
                        player_1.decRank();
                        player_2.incRank();
                        updateRankInFile(player_2.getName(), player_2.getRank());
                        updateRankInFile(player_1.getName(), player_1.getRank());
                    }
                    // player 1 (o) wins
                    else{
                        player_2.decRank();
                        player_1.incRank();
                        updateRankInFile(player_2.getName(), player_2.getRank());
                        updateRankInFile(player_1.getName(), player_1.getRank());
                    }

                    player_1.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");
                    player_2.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");

                    tokens.remove(player_1.getName());
                    tokens.remove(player_2.getName());

                    player_1.getDos().writeUTF("done");
                    player_2.getDos().writeUTF("done");
                    try
                    {
                        // closing resources
                        this.player_1.getDis().close();
                        this.player_2.getDis().close();
                        this.player_1.getDos().close();
                        this.player_2.getDos().close();

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }
                if (checkTie()) {
                    player_2.getDos().writeUTF(getBoard());
                    player_1.getDos().writeUTF("Tie!");
                    player_2.getDos().writeUTF("Tie!");

                    player_1.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");
                    player_2.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");

                    tokens.remove(player_1.getName());
                    tokens.remove(player_2.getName());


                    player_1.getDos().writeUTF("done");
                    player_2.getDos().writeUTF("done");
                    try
                    {
                        // closing resources
                        this.player_1.getDis().close();
                        this.player_2.getDis().close();
                        this.player_1.getDos().close();
                        this.player_2.getDos().close();

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }

                String received;
                int line;
                int row;

                try {
                    player_1.getDos().writeUTF("Select Line(0-2):\n");
                    player_1.getDos().writeUTF("done");


                    received = player_1.getDis().readUTF();

                    line = Integer.parseInt(received);


                    player_1.getDos().writeUTF("Select Row(0-2):\n");
                    player_1.getDos().writeUTF("done");

                    received = player_1.getDis().readUTF();

                    row = Integer.parseInt(received);


                    PlayerXPlace(line, row);
                } catch (IOException e) {
                    player1_disc();

                    return;
                }

                try {
                    player_2.getDos().writeUTF(getBoard());
                } catch (IOException e) {
                    player2_disc();
                    return;
                }


                if (checkWin('x')) {
                    player_1.getDos().writeUTF(getBoard());
                    player_1.getDos().writeUTF("player X won!");
                    player_2.getDos().writeUTF("player X won!");

                    // player 1 (x) wins
                    if(player_1.isX()){
                        player_1.incRank();
                        player_2.decRank();
                        updateRankInFile(player_1.getName(), player_1.getRank());
                        updateRankInFile(player_2.getName(), player_2.getRank());
                    }
                    // player 2 (x) wins
                    else{
                        player_1.decRank();
                        player_2.incRank();
                        updateRankInFile(player_2.getName(), player_2.getRank());
                        updateRankInFile(player_1.getName(), player_1.getRank());
                    }

                    player_1.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");
                    player_2.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");

                    tokens.remove(player_1.getName());
                    tokens.remove(player_2.getName());


                    player_1.getDos().writeUTF("done");
                    player_2.getDos().writeUTF("done");
                    try
                    {
                        // closing resources
                        this.player_1.getDis().close();
                        this.player_2.getDis().close();
                        this.player_1.getDos().close();
                        this.player_2.getDos().close();

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }
                if (checkTie()) {
                    player_1.getDos().writeUTF(getBoard());
                    player_1.getDos().writeUTF("Tie!");
                    player_2.getDos().writeUTF("Tie!");

                    player_1.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");
                    player_2.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");


                    tokens.remove(player_1.getName());
                    tokens.remove(player_2.getName());

                    player_1.getDos().writeUTF("done");
                    player_2.getDos().writeUTF("done");
                    try
                    {
                        // closing resources
                        this.player_1.getDis().close();
                        this.player_2.getDis().close();
                        this.player_1.getDos().close();
                        this.player_2.getDos().close();

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }

                try {
                    player_2.getDos().writeUTF("Select Line(0-2):\n");
                    player_2.getDos().writeUTF("done");


                    received = player_2.getDis().readUTF();



                    line = Integer.parseInt(received);

                    player_2.getDos().writeUTF("Select Row(0-2):\n");
                    player_2.getDos().writeUTF("done");

                    received = player_2.getDis().readUTF();

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
            player_1.getDos().writeUTF("");
            player_1.getDos().writeUTF(player_1.getName() + " VS " + player_2.getName());
            player_1.getDos().writeUTF("");

            player_2.getDos().writeUTF("");
            player_2.getDos().writeUTF(player_1.getName() + " VS " + player_2.getName());
            player_2.getDos().writeUTF("");


            while (true) {
                try {
                    player_2.getDos().writeUTF(getBoard());
                } catch (IOException e) {
                    player2_disc();
                    return;
                }


                if (checkWin('x')) {
                    player_1.getDos().writeUTF(getBoard());
                    player_1.getDos().writeUTF("player X won!");
                    player_2.getDos().writeUTF("player X won!");

                    // player 1 (x) wins
                    if(player_1.isX()){
                        player_1.incRank();
                        player_2.decRank();
                        updateRankInFile(player_2.getName(), player_2.getRank());
                        updateRankInFile(player_1.getName(), player_1.getRank());
                    }
                    // player 2 (x) wins
                    else{
                        player_1.decRank();
                        player_2.incRank();
                        updateRankInFile(player_2.getName(), player_2.getRank());
                        updateRankInFile(player_1.getName(), player_1.getRank());
                    }

                    player_1.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");
                    player_2.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");

                    tokens.remove(player_1.getName());
                    tokens.remove(player_2.getName());


                    player_1.getDos().writeUTF("done");
                    player_2.getDos().writeUTF("done");
                    try
                    {
                        // closing resources
                        this.player_1.getDis().close();
                        this.player_2.getDis().close();
                        this.player_1.getDos().close();
                        this.player_2.getDos().close();

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }
                if (checkTie()) {
                    player_1.getDos().writeUTF(getBoard());
                    player_1.getDos().writeUTF("Tie!");
                    player_2.getDos().writeUTF("Tie!");

                    player_1.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");
                    player_2.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");


                    tokens.remove(player_1.getName());
                    tokens.remove(player_2.getName());

                    player_1.getDos().writeUTF("done");
                    player_2.getDos().writeUTF("done");
                    try
                    {
                        // closing resources
                        this.player_1.getDis().close();
                        this.player_2.getDis().close();
                        this.player_1.getDos().close();
                        this.player_2.getDos().close();

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }

                String received;
                int line;
                int row;

                try {
                    player_2.getDos().writeUTF("Select Line(0-2):\n");
                    player_2.getDos().writeUTF("done");


                    received = player_2.getDis().readUTF();



                    line = Integer.parseInt(received);

                    player_2.getDos().writeUTF("Select Row(0-2):\n");
                    player_2.getDos().writeUTF("done");

                    received = player_2.getDis().readUTF();

                    row = Integer.parseInt(received);

                    PlayerOPlace(line, row);
                } catch (IOException e) {
                    player2_disc();
                    return;
                }

                try {
                    player_1.getDos().writeUTF(getBoard());
                } catch (IOException e) {
                    player1_disc();
                    return;
                }


                if (checkWin('o')) {
                    player_2.getDos().writeUTF(getBoard());
                    player_1.getDos().writeUTF("player O won!");
                    player_2.getDos().writeUTF("player O won!");

                    // player 2 (o) wins
                    if(player_1.isX()){
                        player_1.decRank();
                        player_2.incRank();
                        updateRankInFile(player_2.getName(), player_2.getRank());
                        updateRankInFile(player_1.getName(), player_1.getRank());
                    }
                    // player 1 (0) wins
                    else{
                        player_1.incRank();
                        player_2.decRank();
                        updateRankInFile(player_2.getName(), player_2.getRank());
                        updateRankInFile(player_1.getName(), player_1.getRank());
                    }

                    player_1.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");
                    player_2.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");

                    tokens.remove(player_1.getName());
                    tokens.remove(player_2.getName());


                    player_1.getDos().writeUTF("done");
                    player_2.getDos().writeUTF("done");
                    try
                    {
                        // closing resources
                        this.player_1.getDis().close();
                        this.player_2.getDis().close();
                        this.player_1.getDos().close();
                        this.player_2.getDos().close();

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }
                if (checkTie()) {
                    player_2.getDos().writeUTF(getBoard());
                    player_1.getDos().writeUTF("Tie!");
                    player_2.getDos().writeUTF("Tie!");

                    player_1.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");
                    player_2.getDos().writeUTF("You have been logged of Write \"Exit\" to quit!");

                    tokens.remove(player_1.getName());
                    tokens.remove(player_2.getName());


                    player_1.getDos().writeUTF("done");
                    player_2.getDos().writeUTF("done");
                    try
                    {
                        // closing resources
                        this.player_1.getDis().close();
                        this.player_2.getDis().close();
                        this.player_1.getDos().close();
                        this.player_2.getDos().close();

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    return;
                }

                try {
                    player_1.getDos().writeUTF("Select Line(0-2):\n");
                    player_1.getDos().writeUTF("done");


                    received = player_1.getDis().readUTF();

                    line = Integer.parseInt(received);


                    player_1.getDos().writeUTF("Select Row(0-2):\n");
                    player_1.getDos().writeUTF("done");

                    received = player_1.getDis().readUTF();

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
            ConWrapper wrapper = new ConWrapper(gameSpace, player_2.getSocket(), player_2.getName(), true);

            System.out.println(player_1.getName());

            tokens.put(player_1.getName(), wrapper);


            player_2.getDos().writeUTF("Player " + player_1.getName() + " Disconnected... Waiting to reconnect");


            return;
        }

        public void player2_disc() throws IOException {
            ConWrapper wrapper = new ConWrapper(gameSpace, player_1.getSocket(), player_1.getName(), false);

            System.out.println(player_2.getName());

            tokens.put(player_2.getName(), wrapper);

            player_1.getDos().writeUTF("Player " + player_2.getName() + " Disconnected... Waiting to reconnect");

            return;
        }

        @Override
        public void run()
        {
            try {
                if (player_1.isX()) {
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

    public static void start(int n) throws IOException {
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
                        }
                    }
                }.start();



                //auth.logout();
                // create a new thread object
                // Invoking the start() method

            }
        }

        public void fila(Socket socket, int n) throws IOException {
            DataOutputStream dos_central = new DataOutputStream(socket.getOutputStream());
            Authentication auth = new Authentication(socket);
            int check = auth.menu();
            Player player = auth.player;

            if (check == 1) { // 1 if register and login is done

                if (tokens.get(player.getName()).first) {
                    Thread q = new Game(socket, tokens.get(player.getName()).op_socket, player.getName(), tokens.get(player.getName()).op_name, tokens.get(player.getName()).gameSpace, true);
                    q.start();
                }
                else {
                    Thread q = new Game(tokens.get(player.getName()).op_socket, socket, tokens.get(player.getName()).op_name, player.getName(), tokens.get(player.getName()).gameSpace, false);
                    q.start();
                }

                return;
            }

            dos_central.writeUTF("In queue!");
            //players_name.add(player.getName());
            active_players.add(player);

            //System.out.println("Teste Players size: " + players.size());
            System.out.println("Active Players size: " + active_players.size());
            for(Player p: active_players){
                System.out.println("Player: " + p.getName() + ", " + p.getRank());
            }

            
            if (active_players.size() >= n) {
                dos_central.writeUTF("Starting Game...");

                List<List<Character>> gameSpace = new ArrayList<>();

                for (int i = 0; i < 3; i++) {
                    List<Character> v = new ArrayList<>();
                    for (int j = 0; j < 3; j++) {
                        v.add(' ');
                    }
                    gameSpace.add(v);
                }


                
                Comparator<Player> rankComparator = new rankComparator();
                Collections.sort(active_players, rankComparator);


                Thread t = new Game(active_players.get(0).getSocket(), active_players.get(1).getSocket(), active_players.get(0).getName(), active_players.get(1).getName(), gameSpace, true);

                t.start();
                
                tokens.remove(active_players.get(0).getName());
                tokens.remove(active_players.get(0).getName());
                active_players.remove(0);
                active_players.remove(0);
                

                // tokens.remove(players_name.get(0));
                // tokens.remove(players_name.get(0));
                // players_name.remove(0);
                // players_name.remove(0);


            }

            while (waiting(player.getName())) {
                try {
                    dos_central.writeUTF("ping");
                } catch (IOException e) {
                    break;
                }
            }

            int index = getIndexToRemove(player.getName());
            //active_players.remove(index);
            //players.remove(index);
            //players_name.remove(index);

            //keepAlive(socket, player.getName());
        }

        public boolean waiting(String name) {
            for (Player n : active_players) {
                if (name.equals(n.getName())) {
                    return true;
                }
            }
            return false;
        }
        public int getIndexToRemove(String name) {
            int i = 0;
            for (Player n : active_players) {
                if (name.equals(n.getName())) {
                    return i;
                }
            }

            return -1;
        }
        public void keepAlive(Socket socket, String name) throws IOException {
            DataOutputStream dos_central = new DataOutputStream(socket.getOutputStream());

            while (true) {
                try {
                    dos_central.writeUTF("ping");
                } catch (IOException e) {
                    int index = getIndexToRemove(name);

                    active_players.remove(index);

                    break;
                }
            }

            return;
        }
    }

    public static class rankComparator implements Comparator<Player>{

        @Override
        public int compare(Player p1, Player p2) {
            int rank1 = p1.getRank();
            int rank2 = p2.getRank();

            if (rank1 == rank2) {
                int index1 = active_players.indexOf(p1);
                int index2 = active_players.indexOf(p2);
                return Integer.compare(index1, index2);

            } else {
                return Integer.compare(rank2, rank1);
            }
        }
    }

    public static class Authentication{
        // protocol for user registration
        // persist the registration data in a file.

        public Player player;


        public Authentication(Socket socket) throws IOException {
            player = new Player();
            player.setSocket(socket);
            player.setDis_player(new DataInputStream(socket.getInputStream()));
            player.setDos_player(new DataOutputStream(socket.getOutputStream()));
        }

        public int menu() throws IOException {
            //return 1 if register and login is done

            while (true) {
                //write menu
                player.getDos().writeUTF("");
                player.getDos().writeUTF("--------------------------");
                player.getDos().writeUTF("        Tic-Tac-Toe");
                player.getDos().writeUTF("--------------------------");
                player.getDos().writeUTF("");
                player.getDos().writeUTF("Choose mode:");
                player.getDos().writeUTF("    1 - Register");
                player.getDos().writeUTF("    2 - Login");
                player.getDos().writeUTF("done");

                //collect answer
                int line = Integer.parseInt(player.getDis().readUTF());
                System.out.println("line: " + line);

                //deal with answer
                if (line == 1) {
                    if (register()) {
                        player.getDos().writeUTF("");
                        player.getDos().writeUTF("Registration Complete");
                        player.getDos().writeUTF("--------------------------");
                        player.getDos().writeUTF("");
                        break;
                    }
                }

                if (line == 2) {
                    if (login()) {
                        if (tokens.containsKey(player.getName())) {
                            System.out.println("IT IS WORKING");
                            return 1;
                        }


                        break;
                    }
                }

            }

            return 0;
        }

        public boolean register() throws IOException {

            player.getDos().writeUTF("");
            player.getDos().writeUTF("--------------------------");
            player.getDos().writeUTF("        Register");
            player.getDos().writeUTF("--------------------------");
            player.getDos().writeUTF("Username: ");
            player.getDos().writeUTF("done");

            String name = player.getDis().readUTF();
            player.setName(name);

            player.getDos().writeUTF("Password: ");
            player.getDos().writeUTF("done");


            String password = player.getDis().readUTF();
            player.setPassword(password);


            String fin = player.getName() + " | " + player.getPassword() + "\n";
            
            File file = new File("users.txt");
            FileWriter fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            br.write(fin);

            br.close();
            fr.close();

            String rank_line = player.getName() + " | " + player.getRank() + "\n";
            file = new File("rank.txt");
            fr = new FileWriter(file, true);
            br = new BufferedWriter(fr);
            br.write(rank_line);

            br.close();
            fr.close();

            return true;
        }

        public boolean login() throws IOException {

            player.getDos().writeUTF("");
            player.getDos().writeUTF("--------------------------");
            player.getDos().writeUTF("        Login");
            player.getDos().writeUTF("--------------------------");
            player.getDos().writeUTF("Username: ");
            player.getDos().writeUTF("done");

            String name = player.getDis().readUTF();
            player.setName(name);
            player.setRank(Game.getRankByName(name));

            player.getDos().writeUTF("Password: ");
            player.getDos().writeUTF("done");

            String pass = player.getDis().readUTF();
            player.setPassword(pass);
            

            File file = new File("users.txt");
            Scanner scanner = new Scanner(file);

            String fin = name + " | " + pass;

            int lineNum = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineNum++;
                if(line.equals(fin)) {
                    player.getDos().writeUTF("");
                    player.getDos().writeUTF("Login Successful!");
                    player.getDos().writeUTF("--------------------------");
                    player.getDos().writeUTF("");
                    return true;
                }
            }

            player.getDos().writeUTF("Login Failed!");
            player.getDos().writeUTF("--------------------------");
            player.getDos().writeUTF("");
            return false;
        }

        public void logout() throws IOException {
            player.getDos().writeUTF("");
            player.getDos().writeUTF("You Have been Logged out!");
            player.getDos().writeUTF("Please write \"Exit\"");
            player.getDos().writeUTF("done");
        }

    }

}

