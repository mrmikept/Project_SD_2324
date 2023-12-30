package Connector;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class Connector used to send and receive messages
 */
public class Connector
{
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private ReentrantLock readLock;
    private ReentrantLock writeLock;

    public Connector(Socket socket) throws IOException
    {
        this.inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.readLock = new ReentrantLock();
        this.writeLock = new ReentrantLock();
    }

    /**
     * Function to send a message
     * @param message Message to send
     */
    public void send(Message message)
    {
        this.writeLock.lock();
        try {
            this.outputStream.writeUTF(message.getId());
            this.outputStream.writeInt(message.getType());
            this.outputStream.writeUTF(message.getUser());
            this.outputStream.writeInt(message.getMessage().length);
            this.outputStream.write(message.getMessage());
            this.outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * Function to send a message
     * @param id id of the Message
     * @param type Message type
     * @param user user that sent the message
     * @param message the message to send
     */
    public void send(String id, int type, String user, byte[] message)
    {
        this.send(new Message(id, type, user, message));
    }

    /**
     * Reads a message from the inputStream
     * @return Message read from the inputStream
     */
    public Message receive()
    {
        this.readLock.lock();
        try {
            String id = this.inputStream.readUTF();
            int type = this.inputStream.readInt();
            String user = this.inputStream.readUTF();
            int messageSize = this.inputStream.readInt();
            byte[] message = new byte[messageSize];
            this.inputStream.readFully(message);

            return new Message(id,type,user,message);
        }
        catch (NullPointerException | IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            this.readLock.unlock();
        }
    }

    /**
     * Closes the input/output stream
     * @throws IOException
     */
    public void close() throws IOException {
        this.inputStream.close();
        this.outputStream.close();
    }


}
