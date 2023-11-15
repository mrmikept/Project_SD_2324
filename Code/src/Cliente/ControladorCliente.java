package Cliente;

import java.util.*;

public class ControladorCliente {
    private Sistema cliente;

    public ControladorCliente(Sistema cliente) {
        this.cliente = cliente;
    }

    public String read_line() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    public boolean cliente_login(String email, String pass) {
        //return 1 if sucessful 0 if not
        return cliente.login(email,pass);

    }

    public boolean cliente_registo(String email, String pass) {
        return cliente.registo(email,pass);
    }

    public boolean do_job(String s) {
        return true;

    }
}
