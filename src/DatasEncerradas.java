import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatasEncerradas implements Serializable {
    private List<LocalDate> datasEncerradas;
    public ReentrantReadWriteLock l = new ReentrantReadWriteLock();

    public DatasEncerradas(){
        this.datasEncerradas = new ArrayList<>();
    }

    public void addDataEncerrada(LocalDate data){this.datasEncerradas.add(data);}

    public boolean existeDataEncerrada(LocalDate data){

        for(LocalDate d : this.datasEncerradas){
            if(d.isEqual(data)) return true;
        }
        return false;
    }

    public List<LocalDate> getDatasEncerradas() {
        return datasEncerradas;
    }

    public void serialize(String filepath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filepath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
        fos.close();
    }

    public static DatasEncerradas deserialize(String filepath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filepath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        DatasEncerradas datasEncerradas = (DatasEncerradas) ois.readObject();
        ois.close();
        fis.close();
        return datasEncerradas;
    }
}
