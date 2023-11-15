package Cliente;

public class ControladorCliente {
    private Cliente cliente;

    public void read_email() {
        Scanner scanner = new Scanner(System.in);
        String email = scanner.nextLine();
        this.cliente.set_email(email);
    }

    public void read_pass(){
        Scanner scanner = new Scanner(System. in);
        String pass = scanner. nextLine();
        this.cliente.set_pass(pass);
    }

    public boolean cliente_login() {
        //return 1 if sucessful 0 if not
        return this.cliente.login();

    }

    public boolean cliente_registo() {
        return this.cliente.registo();
    }

    public boolean do_job() {

    }

    public static void main(String[] args) {
        //cliente.start_socket();

    }

}