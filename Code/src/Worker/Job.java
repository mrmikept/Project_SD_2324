package Worker;

import java.io.*;
import java.time.LocalDateTime;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Job implements Serializable, Comparable<Job>
{
    private int id; // Job identifier
    private String user; // User who requested the execution of the Job
    private byte[] jobCode; // Job code to be executed
    private int memory; // Necessary Memory for the job execution
    private int priority;
    private ReentrantLock jobLock;
    private Condition condition;

    public Job()
    {
        this.id = -1;
        this.user = "";
        this.jobCode = null;
        this.priority = 0;
        this.memory = 0;
    }
    public Job(int id, String user, byte[] jobCode, int memory) {
        this.id = id;
        this.user = user;
        this.jobCode = jobCode;
        this.memory = memory;
        this.jobLock = new ReentrantLock();
        this.priority = 0;
        this.condition = this.jobLock.newCondition();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
    public int compareTo(Job o) {
        if (this.getPriority() == 3) {
            return 1;
        }

        int memoryDiff = Integer.compare(this.getMemory(), o.getMemory());

        if (memoryDiff < 0) {
            this.priority++;
            return -1; // Job atual tem menor memória, então deve estar em primeiro na fila.
        } else if (memoryDiff > 0) {
            return 1; // Job atual tem maior memória, então deve estar em segundo na fila.
        } else {
            return 0; // Empate em memória, o Job atual e o Job comparado são considerados iguais.
        }
    }
}
