import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class VentanaCaja extends JFrame implements ActionListener {

    private JPanel panelCuentas = new JPanel(new GridLayout(0, 1, 8, 8));
    private JTextField txtMesa = new JTextField();
    private JTextField txtTotal = new JTextField();
    private JComboBox<String> cmbMetodo = new JComboBox<>(new String[]{
            "Efectivo", "Tarjeta de Crédito/Débito", "Transferencia / SPEI"
    });
    private JTextField txtRecibido = new JTextField();
    private JTextArea areaTicket = new JTextArea(10, 20);
    private JButton btnCobrar = Estilo.boton("Registrar Cobro y Generar Ticket", Estilo.NARANJA);
    private JButton btnActualizar = Estilo.boton("Actualizar", Estilo.NARANJA);
    private JButton btnCerrar = Estilo.boton("Cerrar", Estilo.GRIS);

    private int pedidoActual = 0;
    private int mesaActual = 0;
    private double totalActual = 0;

    public VentanaCaja() {
        setTitle("Caja y Cobros - SIGERE");
        setMinimumSize(new Dimension(800, 500));
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Estilo.FONDO);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Estilo.ENCABEZADO);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, Estilo.NARANJA),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        JPanel botones = new JPanel();
        botones.setOpaque(false);
        botones.add(btnActualizar);
        botones.add(btnCerrar);
        encabezado.add(Estilo.titulo("Estación de Caja y Cobros - SIGERE"), BorderLayout.WEST);
        encabezado.add(botones, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(1, 2, 15, 0));
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.add(panelCuentas, BorderLayout.NORTH);
        JScrollPane scrollCuentas = new JScrollPane(envoltorio);
        scrollCuentas.setBorder(BorderFactory.createTitledBorder("Cuentas pendientes de cobro"));
        centro.add(scrollCuentas);

        txtMesa.setEditable(false);
        txtTotal.setEditable(false);
        areaTicket.setEditable(false);
        areaTicket.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaTicket.setBackground(new Color(255, 251, 235));

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.add(new JLabel("Mesa / Cuenta:"));
        form.add(txtMesa);
        form.add(new JLabel("Total a pagar ($ MXN):"));
        form.add(txtTotal);
        form.add(new JLabel("Método de pago:"));
        form.add(cmbMetodo);
        form.add(new JLabel("Monto recibido del cliente ($ MXN):"));
        form.add(txtRecibido);
        form.add(btnCobrar);

        JPanel derecha = new JPanel(new BorderLayout(0, 10));
        derecha.setBorder(BorderFactory.createTitledBorder("Procesar pago y ticket"));
        derecha.add(form, BorderLayout.NORTH);
        derecha.add(new JScrollPane(areaTicket), BorderLayout.CENTER);
        centro.add(derecha);
        add(centro, BorderLayout.CENTER);

        btnCobrar.addActionListener(this);
        btnActualizar.addActionListener(this);
        btnCerrar.addActionListener(this);
        cargarCuentas();
    }

    private void cargarCuentas() {
        panelCuentas.removeAll();
        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT p.id, m.nombre AS mesa, p.cliente, p.detalle, p.total "
                   + "FROM pedidos p JOIN mesas m ON m.id = p.mesa_id "
                   + "WHERE p.pagado = 0 ORDER BY p.id")) {

            int cuantas = 0;
            while (rs.next()) {
                cuantas++;
                JPanel tarjeta = new JPanel(new BorderLayout(10, 0));
                tarjeta.setBackground(new Color(250, 250, 250));
                tarjeta.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(229, 231, 235)),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));

                JLabel info = new JLabel("<html><b>" + rs.getString("mesa") + " (" + rs.getString("cliente") + ")</b><br>"
                        + rs.getString("detalle") + "<br><font color='#ff6600'><b>$"
                        + String.format("%.2f", rs.getDouble("total")) + " MXN</b></font></html>");

                JButton btnCuenta = Estilo.boton("Cobrar", Estilo.VERDE);
                btnCuenta.setActionCommand("C" + rs.getInt("id"));
                btnCuenta.addActionListener(this);

                tarjeta.add(info, BorderLayout.CENTER);
                tarjeta.add(btnCuenta, BorderLayout.EAST);
                panelCuentas.add(tarjeta);
            }
            if (cuantas == 0) {
                panelCuentas.add(new JLabel("No hay cuentas pendientes."));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar las cuentas: " + e.getMessage());
        }
        panelCuentas.revalidate();
        panelCuentas.repaint();
    }

    private void cargarCuenta(int idPedido) {
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT p.mesa_id, m.nombre, p.total FROM pedidos p "
                   + "JOIN mesas m ON m.id = p.mesa_id WHERE p.id = ?")) {

            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    pedidoActual = idPedido;
                    mesaActual = rs.getInt("mesa_id");
                    totalActual = rs.getDouble("total");
                    txtMesa.setText(rs.getString("nombre") + " (Pedido #" + idPedido + ")");
                    txtTotal.setText(String.format("%.2f", totalActual));
                    txtRecibido.setText("");
                    txtRecibido.requestFocus();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCobrar) {
            cobrar();
        } else if (e.getSource() == btnActualizar) {
            cargarCuentas();
        } else if (e.getSource() == btnCerrar) {
            dispose();
        } else if (e.getActionCommand() != null && e.getActionCommand().startsWith("C")) {
            cargarCuenta(Integer.parseInt(e.getActionCommand().substring(1)));
        }
    }

    private void cobrar() {
        if (pedidoActual == 0) {
            JOptionPane.showMessageDialog(this, "Primero elige una cuenta con el botón Cobrar.");
            return;
        }

        double recibido;
        try {
            recibido = Double.parseDouble(txtRecibido.getText().trim().replace(",", "."));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ingresa un monto válido recibido.");
            return;
        }

        if (recibido < totalActual) {
            JOptionPane.showMessageDialog(this, "El monto recibido es menor al total. Verifica el importe.");
            return;
        }

        double cambio = recibido - totalActual;
        Connection con = null;
        boolean mesaLiberada = false;

        try {
            con = Conexion.conectar();
            con.setAutoCommit(false);

            // 1. Marcar pedido como pagado
            try (PreparedStatement ps = con.prepareStatement("UPDATE pedidos SET pagado = 1 WHERE id = ?")) {
                ps.setInt(1, pedidoActual);
                ps.executeUpdate();
            }

            // 2. Verificar si quedan pedidos sin pagar en esa mesa
            try (PreparedStatement ps2 = con.prepareStatement(
                    "SELECT COUNT(*) FROM pedidos WHERE mesa_id = ? AND pagado = 0")) {
                ps2.setInt(1, mesaActual);
                try (ResultSet rs = ps2.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        try (PreparedStatement ps3 = con.prepareStatement(
                                "UPDATE mesas SET estado = 'limpieza' WHERE id = ?")) {
                            ps3.setInt(1, mesaActual);
                            ps3.executeUpdate();
                            mesaLiberada = true;
                        }
                    }
                }
            }

            con.commit();

            // Generar ticket
            String fecha = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
            String ticket = "======= TICKET DE VENTA #" + pedidoActual + " =======\n"
                    + "Mesa:    " + txtMesa.getText() + "\n"
                    + "Fecha:   " + fecha + "\n"
                    + "Método:  " + cmbMetodo.getSelectedItem() + "\n"
                    + "-----------------------------------\n"
                    + String.format("Total cobrado:  $%.2f\n", totalActual)
                    + String.format("Recibido:       $%.2f\n", recibido)
                    + String.format("Cambio:         $%.2f\n", cambio)
                    + "===================================";
            areaTicket.setText(ticket);

            pedidoActual = 0;
            mesaActual = 0;
            totalActual = 0;
            txtMesa.setText("");
            txtTotal.setText("");
            txtRecibido.setText("");
            cargarCuentas();

            String mensaje = "¡Cobro registrado con éxito!";
            if (mesaLiberada) {
                mensaje += "\nLa mesa quedó marcada para limpieza.";
            } else {
                mensaje += "\nAún hay pedidos pendientes de pago en esa mesa.";
            }
            JOptionPane.showMessageDialog(this, mensaje);

        } catch (Exception ex) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ignored) {}
            }
            JOptionPane.showMessageDialog(this, "Error al registrar el cobro: " + ex.getMessage());
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException ignored) {}
            }
        }
    }
}
