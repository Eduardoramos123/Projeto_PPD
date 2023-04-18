import java.util.Scanner;

public class Authentication{
    // protocol for user registration
    // persist the registration data in a file.

    public static void main(String[] args){
        //register or login user
        Scanner scan = new Scanner(System.in);

        System.out.println("");
        System.out.println("--------------------------");
        System.out.println("        Tic-Tac-Tou");
        System.out.println("--------------------------");
        System.out.println("");
        System.out.println("Choose mode:");
        System.out.println("    1 - Register");
        System.out.println("    2 - Login");

        int mode = scan.nextInt();

        if(mode == 1){
            register(scan);
        } else if (mode == 2){
            login(scan);
        }

        //User new_user = new User();

    }

    public static void register(Scanner scan){

        System.out.println("");
        System.out.println("--------------------------");
        System.out.println("        Register");
        System.out.println("--------------------------");
        System.out.print("Username: ");
        String username = scan.nextLine();

        scan.nextLine();

        System.out.print("Password: ");
        String password = scan.nextLine();

        System.out.println("--------------------------");
        System.out.println("Tip: Say \"logout\" if you what to logout");
        System.out.println("");

    }

    public static void login(Scanner scan){

        System.out.println("");
        System.out.println("--------------------------");
        System.out.println("        Login");
        System.out.println("--------------------------");
        System.out.print("Username: ");
        String username = scan.nextLine();

        scan.nextLine();

        System.out.print("Password: ");
        String password = scan.nextLine();

        System.out.println("--------------------------");
        System.out.println("");
    }

    // public void User() {
    //     private String username;
    //     private String password;
        
    // }
    //map
    //session key durant x min??

}