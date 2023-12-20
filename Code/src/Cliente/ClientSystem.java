package Cliente;


import Connector.Connector;
import Connector.Demultiplexer;
import Connector.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientSystem {
    private String username;
    private Demultiplexer demultiplexer;
    private boolean isLoggedin;
    private Socket socket;
    private List<Thread> threadList;

    public ClientSystem()
    {
        this.username = "";
        this.isLoggedin = false;
        this.threadList = new ArrayList<>();
    }

    public String register(String username, String password)
    {
        this.demultiplexer.send("reg",Message.CREATEACCOUT,username,(username + ";" + password).getBytes());
        byte[] bytes = this.demultiplexer.receive(Message.CREATEACCOUT);
        String message = new String(bytes);
        if (message.startsWith("Failed;"))
        {
            return "Failed: " + message.split("[;]")[1];
        } else return message;
    }

    public String login(String username, String password)
    {
        this.demultiplexer.send("auth",Message.AUTENTICATION,username,(username + ";" + password).getBytes());
        byte[] bytes = this.demultiplexer.receive(Message.AUTENTICATION);
        String message = new String(bytes);
        if (message.startsWith("Failed;"))
        {
            return "Failed: " + message.split("[;]")[1];
        }
        else
        {
            this.username = username;
            this.isLoggedin = true;
            return message;
        }
    }

    public void start(String serverAddr, int port)
    {
        this.start_connection(serverAddr, port);
    }

    public void start_connection(String serverAddr, int port){
        try
        {

            this.socket = new Socket(serverAddr,port);
            this.demultiplexer = new Demultiplexer(new Connector(this.socket));
            Thread demultiplexer = new Thread(this.demultiplexer);
            demultiplexer.setName("demultiplexerThread");
            this.threadList.add(demultiplexer);
            demultiplexer.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean isLoggedIn()
    {
        return isLoggedin;
    }

    public void close() throws IOException, InterruptedException {
        this.demultiplexer.close();
        this.socket.close();
        for (Thread t : this.threadList)
        {
            t.join();
        }
    }

}
