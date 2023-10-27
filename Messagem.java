import java.net.InetAddress;

public class Messagem
{
    InetAddress host;
    byte[] messagem; //Get job list; ExecJob size;
    int size;

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

