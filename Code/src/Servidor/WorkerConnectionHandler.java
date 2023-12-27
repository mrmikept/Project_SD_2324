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
    Server server;
    JobManager jobManager;
    Socket socket;
    Connector connector;
    private int totalMemory;
    private int usedMemory;
    private ReentrantLock memoryLock;
    private Condition memoryCondition;

    public WorkerConnectionHandler(Server server, Socket socket, JobManager manager) throws IOException {
        this.server = server;
        this.jobManager = manager;
        this.socket = socket;
        this.connector = new Connector(this.socket);
        this.totalMemory = 0;
        this.usedMemory = 0;
        this.memoryLock = new ReentrantLock();
        this.memoryCondition = this.memoryLock.newCondition();
    }

    private boolean receiveWorkerMemory()
    {
        while (this.totalMemory == 0)
        {
            Message message = this.connector.receive();
            if (message == null) return false;
            if (message.getType() == Message.MEMORYINFO)
            {
                String m = new String(message.getMessage());
                this.totalMemory = Integer.parseInt(m);
                System.out.println("Received Worker Server Memory: " + this.totalMemory);
                break;
            }
        }
        return true;
    }

    private void addCompletedJob(Job job)
    {
        this.removeMemory(job.getMemory());
        this.jobManager.addCompletedJob(job);
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
            this.memoryCondition.signalAll();
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
        System.out.println("Job " + job.getId() + " of user " + job.getUser() + " was sent to be executed in Worker Server.");
    }

    public void test()
    {
        Thread jobSeeker = new Thread(() -> {
            while (true) // TODO SOMETHING TO EXIT THIS
            {
                this.memoryLock.lock();
                try {
                    Job job = this.jobManager.getPendingJob(this.getTotalMemory() - this.getUsedMemory());
                    while (job == null)
                    {
                        try {
                            this.memoryCondition.await();
                        } catch (InterruptedException e)
                        {
                            System.out.println(e.getMessage());
                        }
                        job = this.jobManager.getPendingJob(this.getTotalMemory() - this.getUsedMemory());
                    }
                    this.sendJobRequest(job);
                } finally {
                    this.memoryLock.unlock();
                }
            }
        });
        jobSeeker.start();
    }

    public boolean handle()
    {
        this.test();
        while (true)
        {
            Message message = this.connector.receive();
            if (message == null) return false;
            if (message.getType() == Message.JOBRESULT)
            {
                Job job = new Job();
                job.deserialize(message.getMessage());
                this.removeMemory(job.getMemory());
                this.jobManager.addCompletedJob(job);
            }
            if (message.getType() == Message.CLOSECONNECTION)
            {
                break;
            }
        }
        return true;
    }

    @Override
    public void run()
    {
        boolean sucess = false;
        do {
            sucess = this.receiveWorkerMemory();
            if (!sucess) break;
            this.handle();
        } while (true);
        try {
            this.connector.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}