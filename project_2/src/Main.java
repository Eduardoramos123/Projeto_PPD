import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Game_Server server = new Game_Server();
        Client client1 = new Client();
        Client client2 = new Client();

        Thread t = new Thread(()-> {
            try {
                server.start(2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        t.start();

        Thread t2 = new Thread(()-> {
            try {
                client1.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        t2.start();

        Thread t3 = new Thread(()-> {
            try {
                client2.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        t3.start();
    }
}