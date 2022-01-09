import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class Viagem {
    private String codigoReserva;
    private List<Voo> escalas;

    // Construtor
    public Viagem() {
        this.escalas = new ArrayList<Voo>();
    }

    // Getters

    public String getCodigoReserva() { return this.codigoReserva;}
    public List<Voo> getEscalas() { return this.escalas;}


    // Métodos

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