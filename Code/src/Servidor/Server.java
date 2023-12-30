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

    public Server(int memory) throws IOException {
        this.accounts = new Accounts(CONFIGPATH);
        this.threads = new ArrayList<>();
        this.jobManager = new JobManager();
        this.clientSocket = new ServerSocket();
        this.workerSocket = new ServerSocket();
    }

    public void updateMaxMemory(int memory)
    {
        this.jobManager.updateMaxMemorySingle(memory);
    }

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

    public boolean registerUser(String email, String password)
    {
        if (!this.accounts.verifyAccount(email))
        {
            this.accounts.registerAccount(email,password);
            return true;
        } else return false;
    }

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

    public void start(int port) throws IOException, ClassNotFoundException {
        this.readConfig();
        Thread jobManager = new Thread(this.jobManager);
        jobManager.setName("JobManagerThread");
        jobManager.start();
        this.threads.add(jobManager);
        this.startSocket(port);
    }

    public void stop() throws InterruptedException, IOException {
        for (Thread t : this.threads)
        {
            t.interrupt();
            t.join();
        }
        this.clientSocket.close();
        this.workerSocket.close();

    }

    public boolean addJobtoExecute(Job job)
    {
        return this.jobManager.addPendingJob(job);
    }

    public Job getUserJobResults(String user)
    {
        return this.jobManager.waitForJobCompletion(user);
    }

    public String getServiceStatus()
    {
        int memory = this.jobManager.getAvailableMemory();
        int pending = this.jobManager.countPendingJobs();
        return memory + ";" + pending;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        int port = 9090;
        int memory = 1000;
        if (args.length < 1)
        {
            System.out.println("Few arguments, insert the memory size of the worker server and optionaly the port used in the TCP Connection!");
            System.out.println("Aborting Program...");
            return;
        }
        else
        {
            if (args.length > 1)
            {
                port = Integer.parseInt(args[1]);
            }
            memory = Integer.parseInt(args[0]);
        }

        Server server = new Server(memory);
        try {
            server.start(port);
        } finally {
            server.stop();
        }
    }
}
