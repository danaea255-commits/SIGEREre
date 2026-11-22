import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public class BotonMesa extends JButton {
    private int id;
    private String nombre;

    public BotonMesa(int id, String nombre) {
        super(nombre);
        this.id = id;
        this.nombre = nombre;
        setBackground(Estilo.NARANJA);
        setForeground(Color.WHITE);
        setOpaque(true);
        setFocusPainted(false);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        setBorderPainted(false);
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void marcar(boolean marcada) {
        setBorderPainted(marcada);
    }
}
