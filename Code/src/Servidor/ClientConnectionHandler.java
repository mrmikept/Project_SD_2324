package Servidor;

import Connector.*;
import Worker.Job;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to handle the different types of Requests received from a client.
 */
public class ClientConnectionHandler implements Runnable
{
    private Server server;
    private Socket socket;
    private Connector connector;
    private String username;
    private boolean isLoggedIn;
    private Thread jobWaiter;

    public ClientConnectionHandler(Server server, Socket socket) throws IOException
    {
        this.server = server;
        this.socket = socket;
        this.connector = new Connector(this.socket);
        this.username = "";
        this.isLoggedIn = false;
    }

    /**
     * Handle for the login and register messages
     * @return True if the authentication was sucessfull and False otherwise
     * @throws IOException
     */
    public boolean logOutHandle() throws IOException {
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
                        this.username = strings[0];
                        System.out.println("Authentication sucessfull in account with username: \"" + this.username + "\"");
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

    /**
     * Handle for the Job execution request, Service Status and Logout messages.
     * @return
     * @throws IOException
     */
    public boolean loggedInHandle() throws IOException {
        this.waitJobResults();
        do {
            String[] strings;
            Message message = this.connector.receive();
            if (message == null) break;
            int type = message.getType();
            switch (type)
            {
                case 3: // Job Request
                    System.out.println("Received a job request from user " + message.getUser());
                    Job job = Job.deserialize(message.getMessage());
                    if (job.getId() != -1)
                    {
                        if (this.server.addJobtoExecute(job))
                        {
                            this.connector.send(message.getId(),Message.JOBREQUEST,message.getUser(),"Sucess".getBytes());
                        } else this.connector.send(message.getId(),Message.JOBREQUEST,message.getUser(),"Failed".getBytes());
                    } else System.out.println("Something went wrong serializing Job object");
                    break;
                case 5: // Service status
                    System.out.println("Received a service status request from user " + message.getUser());
                    this.connector.send(message.getId(),Message.SERVICESTATUS,message.getUser(),this.server.getServiceStatus().getBytes());
                    break;
                case 6: // Logout
                    this.username = "";
                    this.isLoggedIn = false;
                    this.connector.send(message.getId(),Message.LOGOUT,message.getUser(),"Sucess".getBytes());
                    System.out.println("User " + message.getUser() + " logged out.");
                    break;
                case 10: // Close Connection
                    return false;
            }
        } while (this.isLoggedIn);
        return true;
    }

    /**
     * Method to create a Thread to wait for job results messages and sends them to the Client.
     */
    public void waitJobResults()
    {
        System.out.println("[Client Connection] Starting Thread to wait for user job results from user " + this.username);
        Thread jobWaiter = new Thread(() -> {
           while (this.isLoggedIn)
           {
               System.out.println("Waiting for job results from user " + this.username);
               Job jobResult = this.server.getUserJobResults(this.username);
               if (jobResult == null)
               {
                   break;
               }
               Message response = new Message(String.valueOf(jobResult.getId()),Message.JOBRESULT,this.username,jobResult.serialize());
               this.connector.send(response);
               System.out.println("[Job Result] Result for Job " + jobResult.getId() + " sent sucessfully to user " + this.username);
           }
        });
        jobWaiter.start();
        this.jobWaiter = jobWaiter;
    }

    /**
     * Method to close all the connections with a Client and Waits for the Threads to finish.
     * @throws InterruptedException
     * @throws IOException
     */
    public void close() throws InterruptedException, IOException {
        if (this.jobWaiter != null)
        {
            this.jobWaiter.interrupt();
            this.jobWaiter.join();
        }
        this.connector.close();
        System.out.println("[Client Connector] Connection with client closed.");
    }

    @Override
    public void run()
    {
        try {
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
        } catch (Exception e)
        {
            System.out.println("[Client Connector] Something went wrong with connection with user.");
        } finally {
            try {
                this.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
