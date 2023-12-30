package Servidor;

import Worker.Job;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private static final String CONFIGPATH = System.getProperty("user.home") + "/CloudServiceApp/serverConfig/";
    private Accounts accounts;
    private JobManager jobManager;
    private ServerSocket clientSocket;
    private ArrayList<Thread> threads;
    private ServerSocket workerSocket;

    public Server() throws IOException {
        this.accounts = new Accounts(CONFIGPATH);
        this.threads = new ArrayList<>();
        this.jobManager = new JobManager();
        this.clientSocket = new ServerSocket();
        this.workerSocket = new ServerSocket();
    }

    /**
     * Updates the Max Job memory allowed in Job Manager.
     * @param memory
     */
    public void updateMaxMemory(int memory)
    {
        this.jobManager.updateMaxMemorySingle(memory);
    }

    /**
     * Starts the sockets for the client's and worker's connection and creates new thread to handle the connections
     * @param port Port to start the Client Socket
     */
    public void startSocket(int port)
    {
        Thread workerConnection = new Thread(() -> {
            try {
                int workers = 0;
                this.workerSocket = new ServerSocket(8080);
                System.out.println("Listening for workers connection at port " + workerSocket.getLocalPort());
                while (true)
                {
                    Socket worker = workerSocket.accept();
                    System.out.println("New Worker connected!");
                    WorkerConnectionHandler workerConnector = new WorkerConnectionHandler(this,worker,this.jobManager,workers);
                    Thread thread = new Thread(workerConnector);
                    thread.start();
                    this.jobManager.addWorker(workerConnector);
                    this.threads.add(thread);
                    workers++;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        workerConnection.start();

        try{
            this.clientSocket = new ServerSocket(port);
            System.out.println("Socket open at port: " + this.clientSocket.getLocalPort());
            while (true)
            {
                Socket client = clientSocket.accept();
                System.out.println("New client connection, creating new thread...");
                Thread thread = new Thread(new ClientConnectionHandler(this,client));
                this.threads.add(thread);
                thread.start();
                System.out.println("Thread started!");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to register an user account
     * @param username Username for the account
     * @param password Password for the account
     * @return True if registering is sucessfull and False otherwise
     */
    public boolean registerUser(String username, String password)
    {
        if (!this.accounts.verifyAccount(username))
        {
            this.accounts.registerAccount(username,password);
            return true;
        } else return false;
    }

    /**
     * Method to authenticate a given user
     * @param user username of the account
     * @param password password of the account
     * @return 0 if the authentication is sucessfull, 1 is the username is not registed and -1 if the the authentication was not sucessfull
     */
    public int autenticateUser(String user, String password)
    {
        if (this.accounts.credentialsMatch(user,password))
        {
            return 0;
        }
        else if (this.accounts.verifyAccount(user))
        {
            return 1;
        } else return -1;
    }

    /**
     * Reds the Server Configuration Files
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readConfig() throws IOException, ClassNotFoundException {
        System.out.println("Reading configuration files...");
        File config = new File(CONFIGPATH);
        if (!config.exists())
        {
            config.mkdirs();
            System.out.println("No configuration found!");
            return;
        }
        System.out.println("Reading accounts from file...");
        this.accounts.readFromFile(CONFIGPATH);
    }

    /**
     * Method to start the server, it reads all the config files, Creates a Thread for the Job Manager and start the sockets for the client's and worker's
     * @param port
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void start(int port) throws IOException, ClassNotFoundException {
        this.readConfig();
        Thread jobManager = new Thread(this.jobManager);
        jobManager.setName("JobManagerThread");
        jobManager.start();
        this.threads.add(jobManager);
        this.startSocket(port);
    }

    /**
     * Method to stop the server, it interrupts all the executing Threads and Join them and closes the client and workers sockets.
     * @throws InterruptedException
     * @throws IOException
     */
    public void stop() throws InterruptedException, IOException {
        for (Thread t : this.threads)
        {
            t.interrupt();
            t.join();
        }
        this.clientSocket.close();
        this.workerSocket.close();

    }

    /**
     * Adds a Job to be executed in the Job Manager.
     * @param job Job to be executed
     * @return true if the job can be executed, false otherwise
     */
    public boolean addJobtoExecute(Job job)
    {
        return this.jobManager.addPendingJob(job);
    }

    /**
     * Returns a Completed Job from a user request
     * @param user Username from the user who sent the Job
     * @return The Completed Job
     */
    public Job getUserJobResults(String user)
    {
        return this.jobManager.waitForJobCompletion(user);
    }

    /**
     * Gets the service status: the available memory and pending jobs of the service.
     * @return A string with the information separated by ';'( ex: availableMemory;NumberPendingJobs
     */
    public String getServiceStatus()
    {
        int memory = this.jobManager.getAvailableMemory();
        int pending = this.jobManager.countPendingJobs();
        return memory + ";" + pending;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        int port = 9090;
        if (args.length > 1)
        {
            port = Integer.parseInt(args[1]);
        }

        Server server = new Server();
        try {
            server.start(port);
        } finally {
            server.stop();
        }
    }
}
