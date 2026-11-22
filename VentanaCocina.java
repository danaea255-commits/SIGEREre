import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class VentanaCocina extends JFrame implements ActionListener {

    private JPanel panelPedidos = new JPanel(new GridLayout(0, 3, 15, 15));
    private JButton btnActualizar = Estilo.boton("Actualizar", Estilo.NARANJA);
    private JButton btnCerrar = Estilo.boton("Cerrar", Estilo.OSCURO);
    private Timer timer = new Timer(10000, this);
    private boolean esAdmin = "Administrador".equals(Sesion.rol);

    public VentanaCocina() {
        setTitle(esAdmin ? "Cocina (Solo consulta) - SIGERE" : "Pantalla de Cocina - SIGERE");
        setMinimumSize(new Dimension(700, 450));
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(45, 52, 54));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(33, 37, 41));
        encabezado.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        JPanel derecha = new JPanel();
        derecha.setOpaque(false);
        JLabel lblMonitoreo = new JLabel(esAdmin
                ? "Modo consulta · solo visualización"
                : "Se actualiza cada 10 segundos");
        lblMonitoreo.setForeground(new Color(189, 195, 199));
        derecha.add(lblMonitoreo);
        derecha.add(btnActualizar);
        derecha.add(btnCerrar);
        encabezado.add(Estilo.titulo("Estación de Cocina (KDS)"), BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        panelPedidos.setBackground(new Color(45, 52, 54));
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setBackground(new Color(45, 52, 54));
        envoltorio.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        envoltorio.add(panelPedidos, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(envoltorio);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(45, 52, 54));
        add(scroll, BorderLayout.CENTER);

        btnActualizar.addActionListener(this);
        btnCerrar.addActionListener(this);
        cargar();
        timer.start();
    }

    private void cargar() {
        panelPedidos.removeAll();
        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT p.id, m.nombre AS mesa, p.detalle, p.notas, p.estado_cocina, p.fecha "
                   + "FROM pedidos p JOIN mesas m ON m.id = p.mesa_id "
                   + "WHERE p.entregado = 0 ORDER BY p.id")) {

            int total = 0;
            while (rs.next()) {
                total++;
                String fecha = rs.getString("fecha");
                String hora = "";
                if (fecha != null && fecha.length() >= 16) {
                    hora = fecha.substring(11, 16);
                }
                panelPedidos.add(crearTarjeta(
                        rs.getInt("id"),
                        rs.getString("mesa"),
                        hora,
                        rs.getString("detalle"),
                        rs.getString("notas"),
                        rs.getString("estado_cocina")));
            }
            if (total == 0) {
                JLabel vacio = new JLabel("No hay comandas pendientes.");
                vacio.setForeground(Color.WHITE);
                panelPedidos.add(vacio);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar las comandas: " + e.getMessage());
            timer.stop();
        }
        panelPedidos.revalidate();
        panelPedidos.repaint();
    }

    private JPanel crearTarjeta(int id, String mesa, String hora, String detalle, String notas, String estado) {
        JPanel tarjeta = new JPanel(new BorderLayout(5, 5));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel lblMesa = new JLabel("<html><b>" + mesa + "</b><br>Pedido #" + id + " | " + hora + "</html>");
        JLabel lblEstado = new JLabel(" " + estado + " ");
        lblEstado.setOpaque(true);
        lblEstado.setForeground(Color.WHITE);

        if ("Pendiente".equals(estado)) {
            lblEstado.setBackground(new Color(231, 76, 60));
        } else if ("En Preparación".equals(estado)) {
            lblEstado.setBackground(new Color(243, 156, 18));
        } else {
            lblEstado.setBackground(new Color(39, 174, 96));
        }

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(lblMesa, BorderLayout.CENTER);
        arriba.add(lblEstado, BorderLayout.EAST);
        tarjeta.add(arriba, BorderLayout.NORTH);

        String html = "<html><b>" + (detalle != null ? detalle.replace(", ", "<br>") : "") + "</b>";
        if (notas != null && !notas.isEmpty()) {
            html += "<br><br><font color='#c0392b'>Nota: " + notas + "</font>";
        }
        html += "</html>";
        JLabel lblDetalle = new JLabel(html);
        lblDetalle.setVerticalAlignment(SwingConstants.TOP);
        lblDetalle.setPreferredSize(new Dimension(200, 90));
        tarjeta.add(lblDetalle, BorderLayout.CENTER);

        if (!esAdmin) {
            if ("Pendiente".equals(estado)) {
                JButton b = Estilo.boton("En Preparación", new Color(243, 156, 18));
                b.setActionCommand("P" + id);
                b.addActionListener(this);
                tarjeta.add(b, BorderLayout.SOUTH);
            } else if ("En Preparación".equals(estado)) {
                JButton b = Estilo.boton("Marcar Listo", new Color(39, 174, 96));
                b.setActionCommand("L" + id);
                b.addActionListener(this);
                tarjeta.add(b, BorderLayout.SOUTH);
            } else {
                JLabel listo = new JLabel("Listo: esperando al mesero", SwingConstants.CENTER);
                listo.setForeground(new Color(39, 174, 96));
                tarjeta.add(listo, BorderLayout.SOUTH);
            }
        } else {
            JLabel soloVista = new JLabel("Solo consulta", SwingConstants.CENTER);
            soloVista.setForeground(Estilo.GRIS);
            tarjeta.add(soloVista, BorderLayout.SOUTH);
        }
        return tarjeta;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer || e.getSource() == btnActualizar) {
            cargar();
        } else if (e.getSource() == btnCerrar) {
            dispose();
        } else if (!esAdmin) {
            String comando = e.getActionCommand();
            if (comando != null && comando.length() > 1) {
                int id = Integer.parseInt(comando.substring(1));
                if (comando.startsWith("P")) {
                    cambiarEstado(id, "En Preparación");
                } else if (comando.startsWith("L")) {
                    cambiarEstado(id, "Listo");
                }
            }
        }
    }

    private void cambiarEstado(int id, String nuevoEstado) {
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE pedidos SET estado_cocina = ? WHERE id = ?")) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, id);
            ps.executeUpdate();
            cargar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    @Override
    public void dispose() {
        timer.stop();
        super.dispose();
    }
}
