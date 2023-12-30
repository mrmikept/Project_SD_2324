package Worker;

import Connector.Connector;
import Connector.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Worker Server Class to receive job request to execute and send result
 */
public class Worker
{
    public int workerMemory; // Memory of the worker server
    public Socket socket; // Socket with the connection with the central server
    public Connector connector; // Connector to send and receive Messages
    private List<Thread> threadList;
    private ReentrantLock threadListLock;

    public Worker(int memory, String serverAddr, int port) throws IOException {
        this.workerMemory = memory;
        this.socket = new Socket(serverAddr,port);
        this.connector = new Connector(this.socket);
        this.threadList = new ArrayList<>();
        this.threadListLock = new ReentrantLock();
    }

    /**
     * Sends a job result to the central server
     * @param job Job result of a Job
     */
    public void sendCompletedJob(Job job)
    {
        System.out.println("Sending result for job " + job.getId() + " of user " + job.getUser() + " to central server!");
        try {
            this.connector.send("worker",Message.JOBRESULT,"worker",job.serialize());
        } catch (RuntimeException e)
        {
            System.out.println("Something went wrong sending Job Result to the central server...");
        }
    }

    /**
     * Sends the total memory information of the worker server to the central server
     */
    public void sendServerMemory()
    {
        this.connector.send("Worker", Message.MEMORYINFO,"Worker",String.valueOf(this.workerMemory).getBytes());
        System.out.println("Memory sent to Central Server!");

    }

    /**
     * Start fucntion to start the communications between the worker server and central server.
     * It also handles messages from the central server.
     * @throws IOException
     */
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
                Job job = Job.deserialize(message.getMessage());
                Thread jobExecutor = new Thread(new JobExecutor(this,job));
                jobExecutor.setName("Job Executor for job " + job.getId() + " from user " + job.getUser());
                jobExecutor.start();
                this.threadListLock.lock();
                try {
                    this.threadList.add(jobExecutor);
                } finally {
                    threadListLock.unlock();
                }
            }
            else
            {
                System.out.println("Received an unknown request from server, message type: " + message.getType() + ", ignoring...");
            }
        }
    }

    /**
     * Function to stop the worker server
     * @throws IOException
     * @throws InterruptedException
     */
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
        int memory, port = 8080;
        String serverAddr = "localhost";
        if (args.length < 1)
        {
            System.out.println("Not enought arguments! Please give worker server memory and optionally the address and port of the Central Server socket!");
            System.out.println("Exemple: java Worker <memory> <Central Server Address(optional)> <Central Server Socket Port(optional)>");
            return;
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
        } catch (Exception e)
        {
            System.out.println("Something went wrong... Closing..");
            return;
        }

    }
}
