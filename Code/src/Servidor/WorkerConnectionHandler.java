package Servidor;

import Connector.Connector;
import Connector.Message;
import Worker.Job;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class WorkerConnectionHandler implements Runnable
{
    int id;
    Server server;
    JobManager jobManager;
    Socket socket;
    Connector connector;
    private int totalMemory;
    private int usedMemory;
    ReentrantLock memoryLock;

    public WorkerConnectionHandler(Server server, Socket socket, JobManager manager, int id) throws IOException {
        this.id = id;
        this.server = server;
        this.jobManager = manager;
        this.socket = socket;
        this.connector = new Connector(this.socket);
        this.totalMemory = 0;
        this.usedMemory = 0;
        this.memoryLock = new ReentrantLock();
    }

    public int getId()
    {
        return this.id;
    }

    private boolean receiveWorkerMemory() throws IOException {
        while (this.totalMemory == 0)
        {
            Message message = this.connector.receive();
            if (message == null) return false;
            if (message.getType() == Message.MEMORYINFO)
            {
                this.memoryLock.lock();
                try {
                    String m = new String(message.getMessage());
                    this.totalMemory = Integer.parseInt(m);
                    this.server.updateMaxMemory(this.totalMemory);
                } finally {
                    this.memoryLock.unlock();
                }
                System.out.println("Received Worker Server Memory: " + this.totalMemory);
                break;
            }
        }
        return true;
    }

    private void addCompletedJob(Job job)
    {
        this.removeMemory(job.getMemory());
        this.jobManager.addCompletedJob(job, this.id);
    }

    private void addMemory(int memory)
    {
        this.memoryLock.lock();
        try
        {
            this.usedMemory += memory;
        } finally {
            this.memoryLock.unlock();
        }
    }

    private void removeMemory(int memory)
    {
        this.memoryLock.lock();
        try
        {
            this.usedMemory -= memory;
        } finally {
            this.memoryLock.unlock();
        }
    }

    public int getUsedMemory()
    {
        this.memoryLock.lock();
        try {
            return this.usedMemory;
        } finally {
            this.memoryLock.unlock();
        }
    }

    public int getTotalMemory()
    {
        return this.totalMemory;
    }

    public void sendJobRequest(Job job)
    {
        this.addMemory(job.getMemory());
        this.connector.send(String.valueOf(job.getId()),Message.JOBREQUEST,"Server",job.serialize());
        System.out.println("Job " + job.getId() + " of user " + job.getUser() + " (memory nedded: " + job.getMemory() +  ") was sent to be executed in Worker Server " + this.getId() + " memory used " + this.getUsedMemory() + " bytes, of " + this.getTotalMemory() + " bytes.");
    }

    public boolean handle() throws IOException {
        while (true)
        {
            Message message = this.connector.receive();
            if (message == null) return false;
            if (message.getType() == Message.JOBRESULT)
            {
                Job job = new Job();
                job.deserialize(message.getMessage());
                this.addCompletedJob(job);
            }
            if (message.getType() == Message.CLOSECONNECTION)
            {
                break;
            }
        }
        return true;
    }

    public void close()
    {
        this.jobManager.removeWorker(this);
        try {
            this.connector.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run()
    {
        try
        {
            boolean flag = true;
            while (flag)
            {
                flag = this.receiveWorkerMemory();
                if (!flag) break;
                flag = this.handle();
            }
        } catch (Exception e)
        {
            System.out.println("Lost Connection with worker " + this.getId());
        }
        finally {
            this.close();
        }

    }
}
