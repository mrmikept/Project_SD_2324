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
            this.demultiplexer.send("logout",Message.LOGOUT,this.username,"Logging out".getBytes());
            byte[] bytes = this.demultiplexer.receive(Message.LOGOUT);
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
        this.saveJobResult(jobId,null);
        Job job = new Job(jobId, this.username, jobCode, memoryNedded);
        this.demultiplexer.send(String.valueOf(jobId),Message.JOBREQUEST,this.username, job.serialize());
    }

    public void saveJobResult(int jobID, Job job)
    {
        this.jobsMapLock.lock();
        try {
            this.jobsResult.put(jobID,job);
        } finally {
            this.jobsMapLock.unlock();
        }
    }

    public Job waitJobResult()
    {
        byte[] data = this.demultiplexer.receive(Message.JOBRESULT);
        Job result = new Job();
        result.deserialize(data);
        this.saveJobResult(result.getId(),result);
        return result;
    }

    public Map<Integer, Job> getJobsResultMap()
    {
        this.jobsMapLock.lock();
        try
        {
            return new HashMap<>(this.jobsResult);
        } finally {
            this.jobsMapLock.unlock();
        }
    }

    public List<String> getJobStatus() {
        List<String> list = new ArrayList<>();
        Map<Integer, Job> map = this.getJobsResultMap();

        for (int i = 1; i < map.size() + 1; i++) {
            Job job = map.get(i);
            if (job == null) {
                list.add("Job " + i + ": Waiting execution Result.");
            } else if (job.getMemory() != -1) {
                list.add("Job " + i + ": Sucess executing job, received response with " + job.getJobCode().length + " bytes.");
            } else {
                String[] strings = new String(job.getJobCode()).split(";");
                list.add("Job " + i + ": Failed executing job, Error Code: " + strings[1] + "; Message: " + strings[2]);
            }
        }
        return list;

    }

    public String RequestServiceStatus()
    {
        this.demultiplexer.send("Status",Message.SERVICESTATUS,this.username,"status".getBytes());
        byte[] data = this.demultiplexer.receive(Message.SERVICESTATUS);
        String[] strings = new String(data).split(";");
        return "Available Memory: " + strings[0] + "; Number of pending jobs: " + strings[1];
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
