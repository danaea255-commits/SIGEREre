import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class VentanaPanel extends JFrame implements ActionListener {

    private JButton btnMenu = Estilo.boton("Gestionar Menú", Estilo.NARANJA);
    private JButton btnMesas = Estilo.boton("Mapa de Mesas (Supervisión)", Estilo.NARANJA);
    private JButton btnPedidos = Estilo.boton("Órdenes y Ventas", Estilo.NARANJA);
    private JButton btnPersonal = Estilo.boton("Control de Personal", Estilo.NARANJA);
    private JButton btnCocina = Estilo.boton("Ver Cocina", Estilo.NARANJA);
    private JButton btnCaja = Estilo.boton("Ver Caja", Estilo.NARANJA);
    private JButton btnSalir = Estilo.boton("Cerrar Sesión", Estilo.ROJO);

    public VentanaPanel() {
        setTitle("Panel de Administración - SIGERE");
        setMinimumSize(new Dimension(780, 480));
        setSize(980, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Estilo.FONDO);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Estilo.ENCABEZADO);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, Estilo.NARANJA),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));
        JLabel lblBienvenida = Estilo.subtitulo("Bienvenido, " + Sesion.usuario + "   |   Rol: " + Sesion.rol);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        derecha.add(lblBienvenida);
        derecha.add(btnSalir);
        encabezado.add(Estilo.titulo("SIGERE - Panel de Administración"), BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(12, 12));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(8, 15, 15, 15));

        JPanel metricas = new JPanel(new GridLayout(1, 4, 12, 0));
        metricas.setOpaque(false);
        metricas.add(crearTarjeta("VENTAS DE HOY",
                "$" + obtenerValor("SELECT COALESCE(SUM(total), 0) FROM pedidos WHERE pagado = 1 AND DATE(fecha) = CURDATE()")));
        metricas.add(crearTarjeta("ÓRDENES PAGADAS HOY",
                obtenerValor("SELECT COUNT(*) FROM pedidos WHERE pagado = 1 AND DATE(fecha) = CURDATE()")));
        metricas.add(crearTarjeta("MESAS OCUPADAS",
                obtenerValor("SELECT COUNT(*) FROM mesas WHERE estado = 'ocupada'")));
        metricas.add(crearTarjeta("EN COCINA",
                obtenerValor("SELECT COUNT(*) FROM pedidos WHERE entregado = 0")));
        cuerpo.add(metricas, BorderLayout.NORTH);

        JPanel modulos = new JPanel(new GridLayout(2, 3, 12, 12));
        modulos.setOpaque(false);
        modulos.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Estilo.BORDE),
                "Módulos del Sistema",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Estilo.TEXTO));
        modulos.add(btnMenu);
        modulos.add(btnMesas);
        modulos.add(btnPersonal);
        modulos.add(btnPedidos);
        modulos.add(btnCocina);
        modulos.add(btnCaja);
        cuerpo.add(modulos, BorderLayout.CENTER);

        JPanel resumen = new JPanel(new GridLayout(0, 1, 4, 6));
        resumen.setBackground(Estilo.FONDO_TARJETA);
        resumen.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Estilo.BORDE),
                "Resumen de ventas",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Estilo.TEXTO));
        resumen.setPreferredSize(new Dimension(260, 120));
        String totalHoy = obtenerValor("SELECT COALESCE(SUM(total), 0) FROM pedidos WHERE pagado = 1 AND DATE(fecha) = CURDATE()");
        String totalMes = obtenerValor("SELECT COALESCE(SUM(total), 0) FROM pedidos WHERE pagado = 1 AND MONTH(fecha) = MONTH(CURDATE()) AND YEAR(fecha) = YEAR(CURDATE())");
        String ticketProm = obtenerValor(
                "SELECT COALESCE(ROUND(AVG(total), 2), 0) FROM pedidos WHERE pagado = 1 AND DATE(fecha) = CURDATE()");
        JLabel r1 = new JLabel("Hoy: $" + totalHoy);
        r1.setForeground(Estilo.TEXTO);
        r1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel r2 = new JLabel("Mes actual: $" + totalMes);
        r2.setForeground(Estilo.TEXTO);
        JLabel r3 = new JLabel("Ticket promedio hoy: $" + ticketProm);
        r3.setForeground(Estilo.TEXTO_SECUNDARIO);
        resumen.add(r1);
        resumen.add(r2);
        resumen.add(r3);
        cuerpo.add(resumen, BorderLayout.EAST);

        add(cuerpo, BorderLayout.CENTER);

        btnMenu.addActionListener(this);
        btnMesas.addActionListener(this);
        btnPedidos.addActionListener(this);
        btnPersonal.addActionListener(this);
        btnCocina.addActionListener(this);
        btnCaja.addActionListener(this);
        btnSalir.addActionListener(this);
    }

    private JPanel crearTarjeta(String titulo, String valor) {
        JPanel tarjeta = new JPanel(new GridLayout(2, 1, 0, 4));
        tarjeta.setBackground(Estilo.FONDO_TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(Estilo.TEXTO_SECUNDARIO);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValor.setForeground(Estilo.TEXTO);
        tarjeta.add(lblTitulo);
        tarjeta.add(lblValor);
        return tarjeta;
    }

    private String obtenerValor(String sql) {
        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString(1);
            }
            return "0";
        } catch (Exception e) {
            return "?";
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnMenu) {
            new VentanaMenu().setVisible(true);
        } else if (e.getSource() == btnMesas) {
            new VentanaMesas().setVisible(true);
        } else if (e.getSource() == btnPedidos) {
            new VentanaPedidos().setVisible(true);
        } else if (e.getSource() == btnPersonal) {
            new VentanaUsuarios().setVisible(true);
        } else if (e.getSource() == btnCocina) {
            new VentanaCocina().setVisible(true);
        } else if (e.getSource() == btnCaja) {
            new VentanaCaja().setVisible(true);
        } else if (e.getSource() == btnSalir) {
            Sesion.usuario = "";
            Sesion.rol = "";
            new VentanaLogin().setVisible(true);
            dispose();
        }
    }
}
