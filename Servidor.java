import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Servidor implements Runnable{

    private HashMap<String, String> registados;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ArrayList<Thread> threads;
    private PriorityQueue<Messagem> queue; // Ler melhor sobre isto

    //queue com jobs e o utilizador que os pediu

    public Servidor()
    {
        this.registados = new HashMap<>();
        this.threads = new ArrayList<>();
        this.queue = new PriorityQueue<>();
    }

    public void startSocket()
    {
        try{
            this.serverSocket = new ServerSocket(8080); // Cria um socket na porta 8080
            System.out.println("Socket ativo na porta: " + this.serverSocket.getLocalPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int loginConta(String user, String password) {
        if ((registados.containsKey(user)) && (registados.get(user)==password )) return 1; // Para verificar o login, retorna 1 se já está registado no sistema
        return -1; // Caso a conta não esteja iniciada retorma -1, e depois é adicionada à hashmap do servidor
    }


    public int adicionaConta(String user, String password) {
        if loginConta(user,password){ //Confirmar se conta nao existe ja
            registados.put(user,password); //Adicionar conta a hashmap
            return 1; //Conta adicionada com sucesso
        }
        return -1; //Caso nao seja possivel adicionar conta
    }

    //Importado do Example.java dado pelos profs
    public execJob(byte[] tarefa) {
         
        try {

            // executar a tarefa
            byte[] output = JobFunction.execute(tarefa);

            // utilizar o resultado ou reportar o erro
            System.err.println("success, returned "+output.length+" bytes");
        } catch (JobFunctionException e) {
            System.err.println("job failed: code="+e.getCode()+" message="+e.getMessage());
        }
    }

    @Override
    public void run() {

    }
}
