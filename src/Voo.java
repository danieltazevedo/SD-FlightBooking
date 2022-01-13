import java.io.Serializable;

public class Voo implements Serializable {
    private int idVoo;
    private String origem;
    private String destino;
    private int capacidade;

    public Voo (int idVoo,String origem,String destino,int capacidade){
        this.idVoo = idVoo;
        this.origem=origem;
        this.destino=destino;
        this.capacidade=capacidade;
    }

    public Voo (String origem, String destino){
        this.origem = origem;
        this.destino = destino;
    }

    public String getOrigem() {return this.origem;}

    public String getDestino() {return this.destino;}

    public int getCapacidade() {return this.capacidade;}


    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.origem).append("->").append(this.destino).append("\n");
        return sb.toString();
    }

}
