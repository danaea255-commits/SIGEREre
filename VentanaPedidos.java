import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class VentanaPedidos extends JFrame implements ActionListener {

    private DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"#", "Mesa", "Cliente", "Platillos", "Cocina", "Total", "Pagado", "Fecha"}, 0);
    private JTable tabla = new JTable(modelo);
    private JLabel lblTotalHoy = new JLabel(" ");
    private JLabel lblPagados = new JLabel(" ");
    private JLabel lblPendientes = new JLabel(" ");
    private JButton btnActualizar = Estilo.boton("Actualizar", Estilo.NARANJA);
    private JButton btnCerrar = Estilo.boton("Cerrar", Estilo.GRIS);

    public VentanaPedidos() {
        setTitle("Órdenes y Control de Ventas - SIGERE");
        setMinimumSize(new Dimension(820, 480));
        setSize(980, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Estilo.FONDO);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setBackground(Estilo.ENCABEZADO);
        norte.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, Estilo.NARANJA),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        norte.add(Estilo.titulo("Órdenes y Control de Ventas"), BorderLayout.WEST);
        add(norte, BorderLayout.NORTH);

        JPanel metricas = new JPanel(new GridLayout(1, 3, 12, 0));
        metricas.setOpaque(false);
        metricas.setBorder(BorderFactory.createEmptyBorder(8, 15, 4, 15));
        metricas.add(tarjetaMetrica(lblTotalHoy, "Ventas de hoy"));
        metricas.add(tarjetaMetrica(lblPagados, "Pedidos pagados hoy"));
        metricas.add(tarjetaMetrica(lblPendientes, "Pendientes de cobro"));
        add(metricas, BorderLayout.CENTER);

        tabla.setDefaultEditor(Object.class, null);
        tabla.setRowHeight(26);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(260);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Estilo.BORDE),
                "Historial de comandas",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Estilo.TEXTO));

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        centro.add(metricas, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sur.setOpaque(false);
        sur.setBorder(BorderFactory.createEmptyBorder(0, 10, 12, 10));
        sur.add(btnActualizar);
        sur.add(btnCerrar);
        add(sur, BorderLayout.SOUTH);

        btnActualizar.addActionListener(this);
        btnCerrar.addActionListener(this);
        cargar();
    }

    private JPanel tarjetaMetrica(JLabel valor, String titulo) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setBackground(Estilo.FONDO_TARJETA);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.BORDE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel t = new JLabel(titulo);
        t.setForeground(Estilo.TEXTO_SECUNDARIO);
        t.setFont(new Font("Segoe UI", Font.BOLD, 11));
        valor.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valor.setForeground(Estilo.TEXTO);
        p.add(t);
        p.add(valor);
        return p;
    }

    private void cargar() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement()) {

            try (ResultSet rs = st.executeQuery(
                    "SELECT p.id, m.nombre AS mesa, p.cliente, p.detalle, "
                  + "p.estado_cocina, p.total, p.pagado, p.fecha "
                  + "FROM pedidos p JOIN mesas m ON m.id = p.mesa_id ORDER BY p.id DESC")) {
                while (rs.next()) {
                    String pagado = rs.getInt("pagado") == 1 ? "Sí" : "No";
                    String fecha = rs.getString("fecha");
                    if (fecha != null && fecha.length() > 16) {
                        fecha = fecha.substring(0, 16);
                    }
                    modelo.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("mesa"),
                            rs.getString("cliente"),
                            rs.getString("detalle"),
                            rs.getString("estado_cocina"),
                            String.format("$%.2f", rs.getDouble("total")),
                            pagado,
                            fecha
                    });
                }
            }

            try (ResultSet rs = st.executeQuery(
                    "SELECT COALESCE(SUM(total), 0) FROM pedidos WHERE pagado = 1 AND DATE(fecha) = CURDATE()")) {
                if (rs.next()) {
                    lblTotalHoy.setText("$" + String.format("%.2f", rs.getDouble(1)));
                }
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) FROM pedidos WHERE pagado = 1 AND DATE(fecha) = CURDATE()")) {
                if (rs.next()) {
                    lblPagados.setText(String.valueOf(rs.getInt(1)));
                }
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) FROM pedidos WHERE pagado = 0")) {
                if (rs.next()) {
                    lblPendientes.setText(String.valueOf(rs.getInt(1)));
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar las órdenes: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnActualizar) {
            cargar();
        } else if (e.getSource() == btnCerrar) {
            dispose();
        }
    }
}
