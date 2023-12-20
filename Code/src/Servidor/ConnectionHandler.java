package Servidor;

import Connector.*;

import java.io.IOException;
import java.net.Socket;

/**
 * Class to handle the different types of Requests received from a client.
 */
public class ConnectionHandler implements Runnable
{
    private Server server;
    private Socket socket;
    private Connector connector;
    private boolean isLoggedIn;

    public ConnectionHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.connector = new Connector(this.socket);
        this.isLoggedIn = false;
    }

    @Override
    public void run()
    {
        do
        {
            String[] strings;
            Message message = this.connector.receive();
            if (message == null) break;
            int type = message.getType();
            switch (type)
            {
                case 1: // Create Account
                    System.out.println("Creating a new user account...");
                    strings = new String(message.getMessage()).split("[;]");
                    if (this.server.registerUser(strings[0],strings[1]))
                    {
                        System.out.println("Sucessfully registered new account with username: \"" + strings[0] + "\"");
                        this.connector.send(message.getId(),message.getType(), message.getUser(), "Sucess".getBytes());
                    }
                    else
                    {
                        System.out.println("Failed registering user, account with username \"" + strings[0] + "\" already exits!" );
                        this.connector.send(message.getId(),message.getType(),message.getUser(),"Failed;The Account already exits!".getBytes());
                    }
                    break;
                case 2: // Autentication
                    strings = new String(message.getMessage()).split("[;]");
                    System.out.println("Trying to authenticate user.");
                    int flag = this.server.autenticateUser(strings[0],strings[1]);
                    if (flag == 0)
                    {
                        System.out.println("Authentication sucessfull in account with username: \"" + strings[0] + "\"");
                        this.connector.send(message.getId(),message.getType(),message.getUser(),"Sucess".getBytes());
                        this.isLoggedIn = true;
                    } else if (flag == 1)
                    {
                        System.out.println("Authentication failed in account with username: \"" + strings[0] + "\", wrong password");
                        this.connector.send(message.getId(),message.getType(),message.getUser(),"Failed;Wrong Password".getBytes());
                    } else
                    {
                        System.out.println("Authentication failed in account with username: \"" + strings[0] + "\", wrong username/password");
                        this.connector.send(message.getId(),message.getType(),message.getUser(),"Failed;Wrong Username and/or Password.".getBytes());
                    }
                    break;
                case 3: // Job Request
                    // Do Somehting here
                    break;
                case 4: // Job status
                    // Do Something here
                    break;
                case 5: // Job list
                    // Do Something here
                    break;
                case 6: // Close Connection
                    // Do something here;
                    break;
            }
        } while (true);
        System.out.println("Connection closed!");
        this.isLoggedIn = false;
        try {
            this.connector.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
