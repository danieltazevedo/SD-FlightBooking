import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Contas implements Serializable {
    private final HashMap<String, String> credentialsMap;
    public ReentrantReadWriteLock l = new ReentrantReadWriteLock();

    public Contas() {
        this.credentialsMap = new HashMap<>();
    }

    public Map<String,String> getCredentialsMap() {return credentialsMap;}

    public String getPassword(String nome) {return credentialsMap.get(nome);}

    public void addAccount(String nome, String password) {
        credentialsMap.put(nome, password);
    }

    public boolean accountExists(String nome) {
        return credentialsMap.containsKey(nome);
    }

    public List<String> getContasRegistadas(){
        List<String> contas = new ArrayList<>();
        for(String s : this.credentialsMap.keySet())
            contas.add(s);
        return contas;
    }


    public Map<String, String> getAccounts() { return this.credentialsMap;}

    public void serialize(String filepath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filepath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
        fos.close();
    }

    public static Contas deserialize(String filepath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filepath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Contas conta = (Contas) ois.readObject();
        ois.close();
        fis.close();
        return conta;
    }
}