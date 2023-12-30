package Worker;

import Servidor.JobManager;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class for a Job.
 */
public class Job implements Serializable, Comparable<Job>
{
    public static final int PENDING = 0; // Identifies a pending state
    public static final int EXECUTING = 1; // Identifies a executing state
    public static final int SUCESS = 2; // Identifies a sucess state
    public static final int ERROR = 3; // Identifies a error on execution
    private int id; // Job identifier
    private String user; // User who requested the execution of the Job
    private byte[] jobCode; // Job code to be executed
    private int memory; // Necessary Memory for the job execution
    private int priority; // Priority value of the job to be executed
    private int state; // State of the Job, Pending, Executing, Sucess or Error.
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
        this.priority = 0;
        this.state = state;
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

    /**
     * Function to serialize a Job object into a byte array.
     * @return byte array with the job information.
     */
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

    /**
     * Function to deserialize a Job from a byte array
     * @param data array of bytes with the job information
     * @return A Job object
     */
    public static Job deserialize(byte[] data)
    {
        try
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dataInputStream = new DataInputStream(bais);
            try {
                int id = dataInputStream.readInt();
                String user = dataInputStream.readUTF();
                int memory = dataInputStream.readInt();
                int state = dataInputStream.readInt();
                int size = dataInputStream.readInt();
                byte[] jobCode = new byte[size];
                dataInputStream.readFully(jobCode);
                return new Job(id,user,jobCode,memory,state);
            } finally {
                bais.close();
                dataInputStream.close();
            }
        } catch (IOException e) {
            throw  new RuntimeException(e);
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
