package Cliente;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    public void registo() {
        System.out.println("Insere user:");
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
            System.out.println("Insere user:");
            Scanner scanner = new Scanner(System.in);
            String email = scanner.nextLine();
            System.out.println("Insere password:");
            scanner = new Scanner(System.in);
            String password = scanner.nextLine();
            // Servidor.Servidor verifica
            // Caso esteja correto retorna 0
            // E sai do ciclo
        }
    }


    public static void main(String[] args)
    {
        try
        {
            String serverAddr = "localhost";
            int port = 8080;

            Socket socket = new Socket(serverAddr,port);

            BufferedReader socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter socket_out = new PrintWriter(socket.getOutputStream());



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}
