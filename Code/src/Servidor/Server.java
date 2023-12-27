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
    private ServerSocket serverSocket;
    private ArrayList<Thread> threads;

    //queue com jobs e o utilizador que os pediu

    public Server(int memory) {
        this.accounts = new Accounts(CONFIGPATH);
        this.threads = new ArrayList<>();
        this.jobManager = new JobManager();
    }

    public void startSocket(int port)
    {
        Thread workerConnection = new Thread(() -> {
            try {
                ServerSocket workerSocket = new ServerSocket(8080);
                System.out.println("Listening for workers connection at port " + workerSocket.getLocalPort());
                while (true)
                {
                    Socket worker = workerSocket.accept();
                    System.out.println("New Worker connected!");
                    Thread thread = new Thread(new WorkerConnectionHandler(this,worker,this.jobManager));
                    thread.start();
                    this.threads.add(thread);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        workerConnection.start();

        try{
            this.serverSocket = new ServerSocket(port);
            System.out.println("Socket open at port: " + this.serverSocket.getLocalPort());
            while (true)
            {
                Socket client = serverSocket.accept();
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

    //Importado do Example.java dado pelos professores
//    public void execJob(byte[] tarefa) {
//
//        try {
//            // executar a tarefa
//
//            byte[] output = sd23.JobFunction.execute(tarefa); //Output da tarefa
//
//            // utilizar o resultado ou reportar o erro
//            System.err.println("success, returned "+output.length+" bytes");
//        } catch (sd23.JobFunctionException e) {
//            System.err.println("job failed: code="+e.getCode()+" message="+e.getMessage());
//        }
//    }

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
        this.startSocket(port);
    }

    public void addJobtoExecute(Job job)
    {
        this.jobManager.addPendingJob(job);
    }

    public Job getJobResponse(Job job)
    {
        return this.jobManager.waitForJobCompletion(job);
    }

    public String getServiceStatus()
    {
//        int memory = this.workerServer.getTotalMemory() - this.workerServer.getUsedMemory();
//        int pending = this.jobManager.countPendingJobs();
//        return memory + ";" + pending;
        return ":)";
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
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
        server.start(port);

    }
}
