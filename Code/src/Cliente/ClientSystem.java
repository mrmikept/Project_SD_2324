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

/**
 * Client Class with all the methods and logic to execute the program functions.
 */
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
        this.socket = new Socket();
    }

    /**
     * Sends a message to the server to try to register an user.
     * @param username username of the user account
     * @param password password of the user account
     * @return a string with the server message response
     * @throws Exception
     */
    public String register(String username, String password) throws Exception {
        this.demultiplexer.send("reg",Message.CREATEACCOUT,username,(username + ";" + password).getBytes());
        byte[] bytes = this.demultiplexer.receive(Message.CREATEACCOUT);
        String message = new String(bytes);
        if (message.startsWith("Failed;"))
        {
            return "Failed: " + message.split(";")[1];
        } else return message;
    }

    /**
     * Sends a message to the server to try and login an user
     * @param username username of the account for the login
     * @param password password of the account for the login
     * @return A string with the server message response
     * @throws Exception
     */
    public String login(String username, String password) throws Exception {
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

    /**
     * Sends a message to the server to try and logout an user
     * @return True if logout was sucessful; False if logout not sucessfull.
     * @throws Exception
     */
    public boolean logout() throws Exception {
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

    /**
     * Calls all the methods to initiate the client system.
     * @param serverAddr Address of the CloudComputing Server
     * @param port port of the server socket
     * @return true if sucessful, false otherwise.
     */
    public boolean start(String serverAddr, int port)
    {
        return this.start_connection(serverAddr, port);
    }

    /**
     * Starts the socket connection with the server
     * @param serverAddr Address of the CloudComputing Server
     * @param port port of the server socket
     * @return true if sucessful, false otherwise.
     */
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

    /**
     * Sends a message to the server with a job request to be executed and waits for the server response if the job will be executed or not
     * @param jobId id of the job to be executed
     * @param jobCode code to be executed
     * @param memoryNedded amount of memory nedded to execute the job
     * @return True in case of sucess, False otherwise
     * @throws Exception
     */
    public boolean jobExecRequest(int jobId, byte[] jobCode, int memoryNedded) throws Exception {
        Job job = new Job(jobId, this.username, jobCode, memoryNedded, Job.PENDING);
        this.demultiplexer.send(String.valueOf(jobId),Message.JOBREQUEST,this.username, job.serialize());
        byte[] message = this.demultiplexer.receive(Message.JOBREQUEST);
        if (new String(message).equals("Sucess"))
        {
            this.saveJobResult(jobId,null);
            return true;
        }
        else
        {
            this.saveJobResult(jobId,new Job(jobId,this.username,"ERROR".getBytes(),memoryNedded,Job.ERROR));
            return false;
        }
    }

    /**
     * Saves a job result
     * @param jobID id of the job
     * @param job Job object with the result
     */
    public void saveJobResult(int jobID, Job job)
    {
        this.jobsMapLock.lock();
        try {
            this.jobsResult.put(jobID,job);
        } finally {
            this.jobsMapLock.unlock();
        }
    }

    /**
     * Waits for the result of a Job from the server
     * @return The job received from the Server
     * @throws Exception
     */
    public Job waitJobResult() throws Exception {
        byte[] data = this.demultiplexer.receive(Message.JOBRESULT);
        Job result = Job.deserialize(data);
        this.saveJobResult(result.getId(),result);
        return result;
    }

    /**
     * Creates a copy of all the jobs requested
     * @return A map with all the Jobs requested
     */
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

    /**
     * Creates a list with all the job status of all the jobs requested to be executed
     * @return List of String with the satatus of the job to be printed.
     */
    public List<String> getJobStatus() {
        List<String> list = new ArrayList<>();
        Map<Integer, Job> map = this.getJobsResultMap();

        for (int i = 1; i < map.size() + 1; i++) {
            Job job = map.get(i);
            if (job == null) {
                list.add("Job " + i + ": Waiting execution Result.");
            } else if (job.getState() != Job.ERROR) {
                list.add("Job " + i + ": Sucess executing job, received response with " + job.getJobCode().length + " bytes.");
            } else {
                String string = new String(job.getJobCode());
                if (string.equals("ERROR"))
                {
                    list.add("Job " + i + ": Error executing job, to much memory nedded.");
                }
                else
                {
                    String[] strings = string.split(";");
                    list.add("Job " + i + ": Failed executing job, Error Code: " + strings[1] + "; Message: " + strings[2]);
                }
            }
        }
        return list;

    }

    /**
     * Sends a message to the Server requesting the Service Status, how much memory is available and how many jobs are pending to be executed
     * @return Returs the message received from the Server
     * @throws Exception
     */
    public String RequestServiceStatus() throws Exception {
        this.demultiplexer.send("Status",Message.SERVICESTATUS,this.username,"status".getBytes());
        byte[] data = this.demultiplexer.receive(Message.SERVICESTATUS);
        String[] strings = new String(data).split(";");
        return "Available Memory: " + strings[0] + "; Number of pending jobs: " + strings[1];
    }

    /**
     * To verify if the user is logged
     * @return True if user is logged in or False otherwise
     */
    public boolean isLoggedIn()
    {
        return isLoggedin;
    }

    /**
     * Closes the client system. Closes the Demultiplexer Object and Connections and Waits for Threads to finish.
     * @throws IOException
     * @throws InterruptedException
     */
    public void close() throws IOException, InterruptedException {
        if (this.demultiplexer != null)
        {
            System.out.println("Closing Demultiplexer...");
            this.demultiplexer.close();
        }
        System.out.println("Closing Socket...");
        this.socket.close();
        for (Thread t : this.threadList)
        {
            System.out.println("Waiting for thread " + t.getName());
            t.join();
        }
    }

}
