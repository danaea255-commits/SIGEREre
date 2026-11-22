import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String HOST    = "localhost";
    private static final String PUERTO  = "3306"; 
    private static final String BASE    = "saboros";
    private static final String USUARIO = "sigere_user";
    private static final String CLAVE   = "123456";

    public static Connection conectar() throws SQLException {
        String url = "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE
                + "?useUnicode=true&characterEncoding=UTF-8"
                + "&serverTimezone=America/Mexico_City"
                + "&allowPublicKeyRetrieval=true&useSSL=false";
        return DriverManager.getConnection(url, USUARIO, CLAVE);
    }
}
