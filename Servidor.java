import java.util.HashMap;

public class Servidor {

    HashMap<String, String> registados;



    public int verificaConta(String email, String password) {
        if (registados.containsKey(email)) return 1; // Para verificar o login, retorna 1 se já está registado no sistema
        return -1; // Caso a conta não esteja iniciada retorma -1, e depois é adicionada à hashmap do servidor
    }


    public void adicionaConta(String email, String password) {
        registados.put(email,password);
    }





}
