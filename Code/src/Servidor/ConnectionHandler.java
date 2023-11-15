package Servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionHandler implements Runnable
{
    Servidor servidor;
    Socket socket;

    public ConnectionHandler(Servidor servidor, Socket socket)
    {
        this.servidor = servidor;
        this.socket = socket;
    }

    @Override
    public void run()
    {
        try {
            BufferedReader socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter socket_out = new PrintWriter(socket.getOutputStream());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
