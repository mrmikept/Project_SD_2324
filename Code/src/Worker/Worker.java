package Worker;

import Connector.Connector;
import Connector.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Worker
{
    public int workerMemory;
    public Socket socket;
    public Connector connector;
    public ReentrantLock writeLock;
    private List<Thread> threadList;

    public Worker(int memory, String serverAddr, int port) throws IOException {
        this.workerMemory = memory;
        this.socket = new Socket(serverAddr,port);
        this.connector = new Connector(this.socket);
        this.writeLock = new ReentrantLock();
        this.threadList = new ArrayList<>();
    }

    public void sendCompletedJob(Job job)
    {
        this.writeLock.lock();
        try
        {
            System.out.println("Sending result for job " + job.getId() + " of user " + job.getUser() + " to central server!");
            this.connector.send("worker",Message.JOBRESULT,"worker",job.serialize());
        } finally {
            this.writeLock.unlock();
        }
    }

    public void sendServerMemory()
    {
        this.writeLock.lock();
        try
        {
            this.connector.send("Worker", Message.MEMORYINFO,"Worker",String.valueOf(this.workerMemory).getBytes());
            System.out.println("Memory sent to Central Server!");
        } finally {
            this.writeLock.unlock();
        }
    }

    public void start() throws IOException {
        System.out.println("Starting Worker Server...");
        this.sendServerMemory();
        while (true)
        {
            Message message = this.connector.receive();
            if (message == null)
            {
                System.out.println("Lost Connection with Server, closing program.");
                break;
            }
            if (message.getType() == Message.JOBREQUEST)
            {
                System.out.println("Received a new Job request from Server.");
                Job job = new Job();
                job.deserialize(message.getMessage());
                Thread jobExecutor = new Thread(new JobExecutor(this,job));
                jobExecutor.setName("Job Executor for job " + job.getId() + " from user " + job.getUser());
                jobExecutor.start();
                this.threadList.add(jobExecutor);
            }
            else
            {
                System.out.println("Received an unknown request from server, message type: " + message.getType() + ", ignoring...");
            }
        }
    }

    public void stop() throws IOException, InterruptedException {
        System.out.println("Joining threads...");
        for (Thread t : this.threadList)
        {
            t.join();
        }
        System.out.println("Sucess... Closing connector...");
        this.connector.close();
    }

    public static void main(String[] args)
    {
        int memory = 1000, port = 8080;
        String serverAddr = "localhost";
        if (args.length < 1)
        {
            System.out.println("Not enought arguments! Please give worker server memory and optionally the address and port of the Central Server socket!");
            System.out.println("Exemple: java Worker <memory> <Central Server Address(optional)> <Central Server Socket Port(optional)>");
        }
        else
        {
            memory = Integer.parseInt(args[0]);
            if (args.length > 1)
            {
                serverAddr = args[1];
            }
            if (args.length >= 2)
            {
                port = Integer.parseInt(args[2]);
            }
        }
        try {
            Worker worker = new Worker(memory, serverAddr, port);
            worker.start();
            try {
                worker.stop();
            } catch (InterruptedException e)
            {
                System.out.println(e.getMessage());
            }
        } catch (IOException e)
        {
            System.out.println(e.getMessage());
            return;
        }

    }
}
