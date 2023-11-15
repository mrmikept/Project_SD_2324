package Cliente;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    private String email;
    private String pass;
    private Socket socket;


    public boolean registo() {
        //fazer registo com o server primeiro
        return true;
    }

    public boolean login() {
        //fazer login primeiro

        return true;
    }

    public void start_socket(){
        try
        {
            String serverAddr = "localhost";
            int port = 8080;

            this.socket = new Socket(serverAddr,port);

            BufferedReader socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter socket_out = new PrintWriter(socket.getOutputStream());



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void set_email(String email) {
        this.email = email;
    }

    public void set_pass(String pass) {
        this.pass = pass;
    }
}
