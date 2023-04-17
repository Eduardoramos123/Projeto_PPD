import java.io.*;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game_Server {
    private static ServerSocket server;
    private static int port = 9876;
    private static List<Player> players = new ArrayList<>();

    public static void main(String[] args) {
        Game game = new Game();
        game.gameLoop();
    }

    public static class Game {
        private List<List<Character>> gameSpace = new ArrayList<>();

        public Game() {
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

    }

    public class Player {
        public String name;
        private String password;
        private int rank;


        public Player(String name, String password) {
            this.name = name;
            this.password = password;
            this.rank = 0;
        }

        public String getName() {
            return name;
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

   
}

