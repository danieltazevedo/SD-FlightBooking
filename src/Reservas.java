import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Reservas implements Serializable {
    private final HashMap<String, Reserva> mapReservas ;
    public ReentrantReadWriteLock l = new ReentrantReadWriteLock();


    public Reservas() {
        this.mapReservas = new HashMap<>();
    }


    public Map<String,Reserva> getMapReservas() {return mapReservas;}

    public Reserva getReserva(String id) {return mapReservas.get(id);}

    public void addReserva(String id,Reserva r) {mapReservas.put(id,r);}


    public boolean removeReserva(String nome,String id,LocalDate dateOfCanceling) {
        Boolean b = false;

        Reserva r = mapReservas.get(id);
        if(r == null) return false;

        if(r.getDataReserva().isEqual(dateOfCanceling) && r.getNome().equals(nome)){
            b = true;
            mapReservas.remove(id);
        }

        return b;
    }

    public void cancelaReservasByDate(LocalDate dataCancelamento){
        for(Reserva r : this.mapReservas.values()){
            if(r.getDataVoo().isEqual(dataCancelamento)){
                mapReservas.remove(r.getCodigoReserva());
            }
        }
    }

    public String getListaReservas(){
        StringBuilder sb = new StringBuilder();
        for(Reserva r : this.mapReservas.values()){
            sb.append(r.toString());
        }
        return sb.toString();
    }

    public String getListaByName(String name){
        StringBuilder sb = new StringBuilder();
        for(Reserva r : this.mapReservas.values()){
            if(name.equals(r.getNome()))
            sb.append(r.toString());
        }
        return sb.toString();
    }


    public boolean reservaExists(String id) {
        return mapReservas.containsKey(id);
    }

    public Map<String, Reserva> getReservas() { return this.mapReservas;}

    // Retorna a ocupacao de um voo (quantas reservas foram feitas para esse voo)
    public int getOcupacaoVoo(String origem, String destino, LocalDate data){
        int ocupacao = 0;
        for(Reserva r : this.mapReservas.values()){
            if(r.getDataVoo().isEqual(data)){
                for(Voo v : r.getEscalasReserva()){
                    if(v.getOrigem().equals(origem) && v.getDestino().equals(destino)) {
                        ocupacao++;
                    }
                }
            }
        }
        return ocupacao;
    }

    public void serialize(String filepath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filepath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
        fos.close();
    }

    public static Reservas deserialize(String filepath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filepath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Reservas reservas = (Reservas) ois.readObject();
        ois.close();
        fis.close();
        return reservas;
    }



}
