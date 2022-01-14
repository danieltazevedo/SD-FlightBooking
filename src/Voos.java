import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Voos implements Serializable {
    private final HashMap<Integer, Voo> mapVoos ;
    public ReentrantReadWriteLock l = new ReentrantReadWriteLock();


    public Voos() {this.mapVoos = new HashMap<>();}


    public Map<Integer,Voo> getMapVoos() {return mapVoos;}

    public Voo getVoo(int id) {return mapVoos.get(id);}

    public void addVoo(int id,Voo v) {mapVoos.put(id,v);}

    public boolean flightExists(int id) {return mapVoos.containsKey(id);}

    public Map<Integer, Voo> getVoos() { return this.mapVoos;}

    public boolean verificaSeExiste(String origem,String destino){
        for(Voo v : this.mapVoos.values()){
            if(v.getOrigem().equals(origem) && v.getDestino().equals(destino)) {
                return true;
            }
        }
        return false;
    }

    public boolean escalaPossivel(String origem, String destino,int ocupacao){
        boolean b = false;
        int capacidade = 0;

        // verificar se este voo existe
        for(Voo v : this.mapVoos.values()){
            if(v.getOrigem().equals(origem) && v.getDestino().equals(destino)) {
                b = true;
                capacidade = v.getCapacidade();
                break;
            }
        }

        // verificar se há lugares disponiveis
        if(b){
            if(ocupacao >= capacidade) return false;
        }

        return b;
    }

    // lisboa -> porto -> toquio  -> paris

    public List<String> percursosPossiveis(String origem, String destino){
        List<String> percursos = new ArrayList<>();
        List<Voo> comDestinoFim = new ArrayList<>();
        List<Voo> comInicioOrigem = new ArrayList<>();


        for(Voo v : this.mapVoos.values()){
            if(v.getOrigem().equals(origem)) comInicioOrigem.add(v);
            else if (v.getDestino().equals(destino)) comDestinoFim.add(v);
        }

        for(Voo v : comDestinoFim){
            for(Voo vs : comInicioOrigem){
                if(verificaSeExiste(vs.getDestino(),v.getOrigem())){
                    StringBuilder sb = new StringBuilder();
                    sb.append(origem + "->" + vs.getDestino() + "->" + v.getOrigem() + "->" + destino + "\n");
                    percursos.add(sb.toString());
                }
            }
        }

        return percursos;
    }



    public void serialize(String filepath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filepath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
        fos.close();
    }

    public static Voos deserialize(String filepath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filepath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Voos voos = (Voos) ois.readObject();
        ois.close();
        fis.close();
        return voos;
    }

}
