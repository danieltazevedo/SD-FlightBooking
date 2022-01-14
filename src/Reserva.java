import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Reserva implements Serializable {
    private String efetuadoPor;
    private String codigoReserva;
    private LocalDate dataReserva;
    private LocalDate dataVoo;
    private List<Voo> escalas;


    // Construtor
    public Reserva(String username, String codigoReserva, LocalDate dataVoo, LocalDate dataReserva,List<Voo> escalas) {
        this.efetuadoPor = username;
        this.codigoReserva = codigoReserva;
        this.dataVoo = dataVoo;
        this.dataReserva = dataReserva;
        this.escalas = escalas;
    }

    public Reserva(){
        this.escalas = new ArrayList<Voo>();
    }

    // Getters

    public String getCodigoReserva() { return this.codigoReserva;}
    public LocalDate getDataReserva() {return this.dataReserva;}
    public LocalDate getDataVoo() {return this.dataVoo;}
    public List<Voo> getEscalasReserva() { return this.escalas;}


    // Métodos
    public void addVooToReserva (Voo v) {
        this.escalas.add(v);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Nome cliente: ").append(this.efetuadoPor).append("\nCódigo: ").append(codigoReserva).append("\nEfetuada em: ").append(dataReserva).append("\nData de Voo: ").append(dataVoo).append("\nPercurso: ");
        for(Voo v : this.escalas){
            sb.append(v.toString());
        }
        sb.append("\n");
        return sb.toString();
    }
}