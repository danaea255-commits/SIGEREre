import java.sql.Connection;
import java.sql.Statement;

public class Plano {

    public static final int ANCHO = 900;
    public static final int ALTO = 520;
    public static final int ANCHO_MESA = 120;
    public static final int ALTO_MESA = 70;

    public static int posicionX(int numero) {
        return 20 + (numero % 6) * 140;
    }

    public static int posicionY(int numero) {
        return 20 + (numero / 6) * 100;
    }

    public static void sincronizarZonas() throws Exception {
        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement()) {
            st.executeUpdate("INSERT IGNORE INTO zonas (nombre) "
                    + "SELECT zona FROM mesas WHERE activa = 1 GROUP BY zona ORDER BY MIN(id)");
        }
    }
}
