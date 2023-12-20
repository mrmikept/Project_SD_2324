package Servidor;

import Connector.Message;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;

public class Server {

    private static final String CONFIGPATH = "../serverConfig/";
    private Accounts accounts;
    private ServerSocket serverSocket;
    private ArrayList<Thread> threads;
    private PriorityBlockingQueue<Message> queue; // Ler melhor sobre isto, ACHO QUE NÃO PODEMOS USAR ISTOOO LOL

    //queue com jobs e o utilizador que os pediu

    public Server() {
        this.accounts = new Accounts();
        this.threads = new ArrayList<>();
        this.queue = new PriorityBlockingQueue<>();
    }

    public void startSocket(int port)
    {
        try{
            this.serverSocket = new ServerSocket(port); // Cria um socket na porta 8080
            System.out.println("Socket open at port: " + this.serverSocket.getLocalPort());
            while (true)
            {
                Socket client = serverSocket.accept();
                System.out.println("New client connection, creating new thread...");
                Thread thread = new Thread(new ConnectionHandler(this,client));
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

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        int port = 9090;
        if (args.length == 1)
        {
            port = Integer.parseInt(args[0]);
        }
        Server server = new Server();
        server.start(port);

    }
}
