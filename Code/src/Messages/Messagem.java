package Messages;

import java.net.InetAddress;

public class Messagem
{
    private static byte[] TOKEN = ";".getBytes(); // , , job list, , fechar conexão, estados jobs
    public final static int CREATEACCOUT_REQUEST = 1;
    public final static int CREATEACCOUT_REPLY = 2;
    public final static int AUTENTICATION_REQUEST = 3;
    public final static int AUTENTICATION_REPLY = 4;
    public final static int  JOBREQUEST_REQUEST= 5;
    public final static int  JOBREQUEST_REPLY= 6;
    public final static int  JOBSTATUS_REQUEST = 7;
    public final static int  JOBSTATUS_REPLY = 8;
    public final static int  JOBLIST_REQUEST = 9;
    public final static int  JOBLIST_REPLY = 10;
    public final static int  CLOSECONNECTION = 11;

    private int id;
    private int type;
    InetAddress host;

    byte[] messagem; //Get job list; ExecJob size; JobReport report; CreateAccout;<utilizador>;<palavrapasse>



    public Messagem(InetAddress host, byte[] messagem)
    {
        this.host = host;
        this.messagem = messagem.clone();
    }

    public InetAddress getHost() {
        return host;
    }

    public byte[] getMessagem() {
        return messagem.clone();
    }

    public void setHost(InetAddress host) {
        this.host = host;
    }

    public void setMessagem(byte[] messagem) {
        this.messagem = messagem;
    }
}

