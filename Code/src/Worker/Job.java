package Worker;

import Servidor.JobManager;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Job implements Serializable, Comparable<Job>
{
    public static final int PENDING = 0;
    public static final int EXECUTING = 1;
    public static final int SUCESS = 2;
    public static final int ERROR = 3;
    private int id; // Job identifier
    private String user; // User who requested the execution of the Job
    private byte[] jobCode; // Job code to be executed
    private int memory; // Necessary Memory for the job execution
    private int priority;
    private int state;
    private ReentrantLock jobLock;
    private Condition condition;

    public Job()
    {
        this.id = -1;
        this.user = "";
        this.jobCode = null;
        this.priority = 0;
        this.memory = 0;
        this.state = PENDING;
    }
    public Job(int id, String user, byte[] jobCode, int memory, int state) {
        this.id = id;
        this.user = user;
        this.jobCode = jobCode;
        this.memory = memory;
        this.jobLock = new ReentrantLock();
        this.priority = 0;
        this.state = state;
        this.condition = this.jobLock.newCondition();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getState()
    {
        return this.state;
    }

    public void setState(int state)
    {
        this.state = state;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public byte[] getJobCode() {
        return jobCode;
    }

    public void setJobCode(byte[] jobCode) {
        this.jobCode = jobCode;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public int getPriority()
    {
        return this.priority;
    }

    public byte[] serialize() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(baos);
            try {
                dataOutputStream.writeInt(this.getId());
                dataOutputStream.writeUTF(this.getUser());
                dataOutputStream.writeInt(this.memory);
                dataOutputStream.writeInt(this.state);
                dataOutputStream.writeInt(this.jobCode.length);
                dataOutputStream.write(this.jobCode);
                dataOutputStream.flush();

                return baos.toByteArray();
            } finally {
                baos.close();
                dataOutputStream.close();
            }
        } catch (IOException ignored)
        {
        }
        return null;
    }

    public void deserialize(byte[] data)
    {
        try
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dataInputStream = new DataInputStream(bais);
            try {
                this.id = dataInputStream.readInt();
                this.user = dataInputStream.readUTF();
                this.memory = dataInputStream.readInt();
                this.state = dataInputStream.readInt();
                int size = dataInputStream.readInt();
                this.jobCode = new byte[size];
                dataInputStream.readFully(this.jobCode);
            } finally {
                bais.close();
                dataInputStream.close();
            }
        } catch (IOException ignored) {

        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return id == job.id && memory == job.memory && user.equals(job.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, memory);
    }

    public int compareTo(Job o) {
        if (this.getPriority() == 3 || o.getPriority() == 3) {
            return Integer.compare(this.getPriority(), o.getPriority());
        }

        int memoryComp = Integer.compare(this.getMemory(), o.getMemory());

        if (memoryComp > 0) {
            this.priority++;
        }

        return memoryComp;
    }
}
