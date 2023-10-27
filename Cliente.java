import java.util.Scanner;

public class Cliente {

    public static void main(String[] args) throws Exception {

    }

    public void registo() {
        System.out.println("Insere email:");
        Scanner scanner = new Scanner(System. in);
        String email = scanner. nextLine();
        System.out.println("Insere password:");
        scanner = new Scanner(System. in);
        String password = scanner.nextLine();
        // Envia a nova conta!
    }

    public void login() {
        int i = 1;
        while (i==1) {
            System.out.println("Insere email:");
            Scanner scanner = new Scanner(System.in);
            String email = scanner.nextLine();
            System.out.println("Insere password:");
            scanner = new Scanner(System.in);
            String password = scanner.nextLine();
            // Servidor verifica
            // Caso esteja correto retorna 0
            // E sai do ciclo
        }
    }





}
