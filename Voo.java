public class Voo {
    private String origem;
    private String destino;
    private int ocupacao;
    private int capacidade;

    public Voo (String origem,String destino,int ocupacao, int capacidade){
        this.origem=origem;
        this.destino=destino;
        this.ocupacao=ocupacao;
        this.capacidade=capacidade;
    }

    public String getOrigem() {return this.origem;}

    public String getDestino() {return this.destino;}

    public int getOcupacao() {return this.ocupacao;}

    public int getCapacidade() {return this.capacidade;}

    public boolean isFull() {
        return this.ocupacao==this.capacidade;
    }

    public boolean addPassageiro() {
        if(!isFull()) {
        this.ocupacao++;
        return true;}
        return false;
    }
}