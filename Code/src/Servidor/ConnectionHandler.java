package Servidor;

import Connector.*;
import Worker.Job;
import Worker.SingleWorker;

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

    public ConnectionHandler(Server server, Socket socket) throws IOException
    {
        this.server = server;
        this.socket = socket;
        this.connector = new Connector(this.socket);
        this.isLoggedIn = false;
    }

    public boolean logOutHandle()
    {
        do {
            String[] strings;
            Message message = this.connector.receive();
            if (message == null) return false;
            int type = message.getType();
            if (type > 2)
            {
                this.connector.send(message.getId(),Message.ERROR,message.getUser(),"Not logged in".getBytes());
            }
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
                        this.connector.send(message.getId(),message.getType(),message.getUser(), "Failed;The Account already exits!".getBytes());
                    }
                    break;
                case 2: // Autentication
                    strings = new String(message.getMessage()).split("[;]");
                    System.out.println("Trying to authenticate user.");
                    int flag = this.server.autenticateUser(strings[0],strings[1]);
                    if (flag == 0)
                    {
                        System.out.println("Authentication sucessfull in account with username: \"" + strings[0] + "\"");
                        this.connector.send(message.getId(),message.getType(),message.getUser(), "Sucess".getBytes());
                        this.isLoggedIn = true;
                    } else if (flag == 1)
                    {
                        System.out.println("Authentication failed in account with username: \"" + strings[0] + "\", wrong password");
                        this.connector.send(message.getId(),message.getType(),message.getUser(), "Failed;Wrong Password".getBytes());
                    } else
                    {
                        System.out.println("Authentication failed in account with username: \"" + strings[0] + "\", account not registered!");
                        this.connector.send(message.getId(),message.getType(),message.getUser(), "Failed;Account not registered.".getBytes());
                    }
                    break;
            }
        } while (!this.isLoggedIn);
        return true;
    }

    public boolean loggedInHandle()
    {
        do {
            String[] strings;
            Message message = this.connector.receive();
            if (message == null) break;
            int type = message.getType();
            switch (type)
            {
                case 3: // Job Request
                    System.out.println("Received a job request from user " + message.getUser());
                    Job job = new Job();
                    job.deserialize(message.getMessage());
                    if (job.getId() != -1)
                    {
                        this.server.addJobtoExecute(job);
                        new Thread(() -> {
                            byte[] response = this.server.getJobResponse(job);
                            Job result = new Job(job.getId(), job.getUser(), response, 0);
                            this.connector.send(message.getId(),Message.JOBRESULT,message.getUser(),result.serialize());
                            System.out.println("Response of Job " + job.getId() + " from user " + job.getUser() + " sent sucessfully!");
                        }).start(); // TODO Maybe saving this thread????
                    } else System.out.println("Something went wrong serializing Job object");
                    break;
                case 4: // Job status
                    // Do Something here
                    break;
                case 5: // Job list
                    // Do Something here
                    break;
                case 6: // Logout
                    this.isLoggedIn = false;
                    this.connector.send(message.getId(),Message.logOut,message.getUser(),"Sucess".getBytes());
                    System.out.println("User " + message.getUser() + " logged out.");
                    break;
                case 10: // Close Connection
                    // Do something here;
                    break;
            }
        } while (this.isLoggedIn);
        return true;
    }

    @Override
    public void run()
    {
        boolean connection = true;
        do
        {
            connection = logOutHandle();
            if (!connection)
            {
                break;
            }
            connection = loggedInHandle();
        } while (connection);
        System.out.println("Connection closed!");
        this.isLoggedIn = false;
        try {
            this.connector.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
