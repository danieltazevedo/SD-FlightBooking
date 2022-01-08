import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class Viagem {
    private List<Voo> escalas; 

    public Viagem() {
        this.escalas = new ArrayList<Voo>();
    }

    public void addVoo (Voo v) {
        this.escalas.add(v);
    }

    public static Viagem deserialize(String filepath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filepath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Viagem viagens = (Viagem) ois.readObject();
        ois.close();
        fis.close();
        return viagens;
    }
}