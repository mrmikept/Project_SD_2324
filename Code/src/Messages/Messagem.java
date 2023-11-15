package Messages;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Messagem
{
    private final static byte[] TOKEN = ";".getBytes(); // , , job list, , fechar conexão, estados jobs
    private final static byte[] END = ".;;.".getBytes();
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
    String host;
    byte[] messagem;

    public Messagem(String host, byte[] messagem)
    {
        this.host = host;
        this.messagem = messagem.clone();
    }

    public Messagem(byte[] data)
    {
        List<byte[]> list = Messagem.split(Messagem.split(END,data).get(0),TOKEN);
        this.id = Integer.parseInt(new String(list.get(0)));
        this.type = Integer.parseInt(new String(list.get(1)));
        this.host = new String(list.get(2));
        list.subList(0,2).clear();
        int byteSize = 0;
        for (byte[] bt : list)
        {
            byteSize += bt.length;
        }
        this.messagem = new byte[byteSize];
        int i = 0;
        for (byte[] bt : list)
        {
            for (byte b : bt)
            {
                this.messagem[i++] = b;
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] convertByteArray()
    {
        List<byte[]> list = new ArrayList<>();
        list.add(String.valueOf(this.getId()).getBytes());
        list.add(TOKEN);
        list.add(String.valueOf(this.getType()).getBytes());
        list.add(TOKEN);
        list.add(this.getHost().getBytes());
        list.add(TOKEN);
        list.add(this.getMessagem());
        list.add(END);

        int byteSize = 0, i = 0;
        for (byte[] bt : list)
        {
            byteSize += bt.length;
        }
        byte[] converted = new byte[byteSize];
        for (byte[] bt : list)
        {
            for (byte b : bt)
            {
                converted[i++] = b;
            }
        }
        return converted;
    }


    public String getHost() {
        return host;
    }

    public byte[] getMessagem() {
        return messagem.clone();
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setMessagem(byte[] messagem) {
        this.messagem = messagem;
    }

    public static boolean isToken(byte[] token, byte[] input, int index)
    {
        for (int i = 0; i < token.length; i++)
        {
            if (token[i] != input[index+i])
            {
                return false;
            }
        }
        return true;
    }

    public static List<byte[]> split(byte[] token, byte[] input)
    {
        List<byte[]> list = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < input.length; i++)
        {
            if (isToken(token,input,i))
            {
                list.add(Arrays.copyOfRange(input,start, i));
                start = i + token.length;
                i = start;
            }
        }
        list.add(Arrays.copyOfRange(input,start, input.length));
        return list;
    }


}

