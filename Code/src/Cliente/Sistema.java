package Cliente;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import Messages.*;

public class Sistema {
    private String email;
    private Socket socket;

    private Messagem message;

    public boolean registo(String email, String pass) {
        //fazer registo com o server primeiro
        this.email = email;
        return true;
    }

    public boolean login(String email, String pass) {
        //fazer login primeiro
        this.email = email;
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
}
