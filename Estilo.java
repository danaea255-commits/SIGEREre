import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

public class Estilo {

    public static final Color NARANJA = new Color(230, 126, 34);
    public static final Color NARANJA_SUAVE = new Color(243, 156, 18);
    public static final Color VERDE = new Color(39, 174, 96);
    public static final Color ROJO = new Color(192, 57, 43);
    public static final Color AMARILLO = new Color(241, 196, 15);
    public static final Color AZUL = new Color(52, 152, 219);
    public static final Color OSCURO = new Color(44, 62, 80);
    public static final Color GRIS = new Color(127, 140, 141);
    public static final Color FONDO = new Color(253, 246, 236);
    public static final Color FONDO_TARJETA = new Color(255, 252, 247);
    public static final Color TEXTO = new Color(44, 62, 80);
    public static final Color TEXTO_SECUNDARIO = new Color(127, 140, 141);
    public static final Color BORDE = new Color(235, 222, 200);
    public static final Color ENCABEZADO = new Color(255, 250, 245);

    public static JButton boton(String texto, Color color) {
        JButton b = new JButton(texto);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setMargin(new Insets(8, 14, 8, 14));
        return b;
    }

    public static JButton botonSecundario(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(FONDO_TARJETA);
        b.setForeground(TEXTO);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createLineBorder(BORDE, 1));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setMargin(new Insets(6, 12, 6, 12));
        return b;
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l.setForeground(NARANJA);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXTO_SECUNDARIO);
        return l;
    }

    public static Color colorEstado(String estado) {
        if ("ocupada".equalsIgnoreCase(estado)) return ROJO;
        if ("reservada".equalsIgnoreCase(estado)) return AMARILLO;
        if ("limpieza".equalsIgnoreCase(estado)) return AZUL;
        return VERDE;
    }

    public static Color textoEstado(String estado) {
        if ("reservada".equalsIgnoreCase(estado)) return Color.BLACK;
        return Color.WHITE;
    }
}
