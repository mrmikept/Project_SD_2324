package Cliente;


import Connector.Connector;
import Connector.Demultiplexer;
import Connector.Message;
import Worker.Job;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ClientSystem {
    private Map<Integer, Job> jobsResult;
    private ReentrantLock jobsMapLock;
    private String username;
    private Demultiplexer demultiplexer;
    private boolean isLoggedin;
    private Socket socket;
    private List<Thread> threadList;

    public ClientSystem()
    {
        this.jobsResult = new HashMap<>();
        this.jobsMapLock = new ReentrantLock();
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
            return "Failed: " + message.split(";")[1];
        } else return message;
    }

    public String login(String username, String password)
    {
        this.demultiplexer.send("auth",Message.AUTENTICATION,username,(username + ";" + password).getBytes());
        byte[] bytes = this.demultiplexer.receive(Message.AUTENTICATION);
        String message = new String(bytes);
        if (message.startsWith("Failed;"))
        {
            return "Failed: " + message.split(";")[1];
        }
        else
        {
            this.username = username;
            this.isLoggedin = true;
            return message;
        }
    }

    public boolean logout()
    {
        if (this.isLoggedin)
        {
            this.demultiplexer.send("logout",Message.logOut,this.username,"Logging out".getBytes());
            byte[] bytes = this.demultiplexer.receive(Message.logOut);
            String message = new String(bytes);
            if (message.equals("Sucess"))
            {
                this.username = "";
                this.isLoggedin = false;
                return true;
            }
        }
        return false;
    }

    public boolean start(String serverAddr, int port)
    {
        return this.start_connection(serverAddr, port);
    }

    public boolean start_connection(String serverAddr, int port)
    {
        try
        {
            this.socket = new Socket(serverAddr,port);
            this.demultiplexer = new Demultiplexer(new Connector(this.socket));
            Thread demultiplexer = new Thread(this.demultiplexer);
            demultiplexer.setName("demultiplexerThread");
            this.threadList.add(demultiplexer);
            demultiplexer.start();
            return true;
        }
        catch (Exception e)
        {
            System.out.println("Couldn't connect to Server, closing program...");
            return false;
        }
    }

    public void jobExecRequest(int jobId, byte[] jobCode, int memoryNedded)
    {
        this.jobsMapLock.lock();
        try {
            this.jobsResult.put(jobId,null);
        } finally {
            this.jobsMapLock.unlock();
        }
        Job job = new Job(jobId, this.username, jobCode, memoryNedded);
        this.demultiplexer.send(String.valueOf(jobId),Message.JOBREQUEST,this.username, job.serialize());
    }

    public Job waitJobResult()
    {
        byte[] data = this.demultiplexer.receive(Message.JOBRESULT);
        Job result = new Job();
        result.deserialize(data);
        this.jobsMapLock.lock();
        try {
            this.jobsResult.put(result.getId(),result);
        } finally {
            this.jobsMapLock.unlock();
        }
        return result;
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
