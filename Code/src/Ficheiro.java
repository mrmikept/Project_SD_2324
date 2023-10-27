import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ficheiro {


    public static void escreverResultado(int resultaddo ,String path, String user) throws IOException {

        String pasta = Paths.get(path).toAbsolutePath().getParent().toString();
        String pasta2 = pasta + "/Resultados";
        File dir = new File(pasta2);

        if (dir.exists()) {
            File ficheiro = new File(pasta2, "teste.csv");
            ficheiro.createNewFile();
            FileWriter f = new FileWriter(ficheiro);
            f.write(Integer.toString(resultaddo));
            f.close();
        }
        else {
            dir.mkdirs();
            File ficheiro = new File(pasta2, "teste.csv");
            ficheiro.createNewFile();
            FileWriter f = new FileWriter(ficheiro);
            f.write(Integer.toString(resultaddo));
            f.close();

        }
    }


    public static void escreverErro(int tarefa, String mensagem, String path) throws IOException{
        String pasta = Paths.get(path).toAbsolutePath().getParent().toString();
        String pasta2 = pasta + "/Resultados";
        File dir = new File(pasta2);

        if (dir.exists()) {
            File ficheiro = new File(pasta2, "teste2.csv");
            ficheiro.createNewFile();
            FileWriter f = new FileWriter(ficheiro);
            f.write(Integer.toString(tarefa));
            f.append(";" + mensagem);
            f.close();
        }
        else {
            dir.mkdirs();
            File ficheiro = new File(pasta2, "teste2.csv");
            ficheiro.createNewFile();
            FileWriter f = new FileWriter(ficheiro);
            f.write(Integer.toString(tarefa));
            f.append(";" + mensagem);
            f.close();

        }
    }
}
