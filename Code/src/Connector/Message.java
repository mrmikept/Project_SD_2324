package Connector;

/**
 * Message class to send messages with type
 */
public class Message
{
    public final static int CREATEACCOUT = 1;
    public final static int AUTENTICATION= 2;
    public final static int  JOBREQUEST= 3;
    public final static int  JOBRESULT = 4;
    public final static int  SERVICESTATUS = 5;
    public final static int LOGOUT = 6;
    public final static int MEMORYINFO = 7;
    public final static int  CLOSECONNECTION = 10;
    public final static int ERROR = 99;

    private String id;
    private int type;
    private String user;
    byte[] message;

    public Message(String id, int type, String user, byte[] messagem)
    {
        this.id = id;
        this.type = type;
        this.user = user;
        this.message = messagem.clone();
    }

    public Message(Message message)
    {
        this.id = message.getId();
        this.type = message.getType();
        this.user = message.getUser();
        this.message = message.getMessage().clone();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public byte[] getMessage() {
        return message.clone();
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public Message clone()
    {
        return new Message(this);
    }

}

