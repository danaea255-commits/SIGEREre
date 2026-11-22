import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;

public class VentanaComanda extends JDialog implements ActionListener {

    private VentanaMesas padre;
    private int idMesa;
    private ArrayList<Platillo> platillos = new ArrayList<>();
    private ArrayList<JSpinner> cantidades = new ArrayList<>();
    private JTextField txtCliente = new JTextField();
    private JTextArea txtNotas = new JTextArea(3, 20);
    private JButton btnEnviar = Estilo.boton("Enviar a Cocina y Ocupar Mesa", Estilo.NARANJA);

    public VentanaComanda(VentanaMesas padre, int idMesa, String nombreMesa) {
        super(padre, "Atender Mesa: " + nombreMesa, true);
        this.padre = padre;
        this.idMesa = idMesa;

        setSize(500, 580);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel norte = new JPanel(new GridLayout(2, 1, 0, 5));
        norte.add(new JLabel("Nombre del Cliente / Responsable:"));
        norte.add(txtCliente);
        add(norte, BorderLayout.NORTH);

        JPanel panelMenu = new JPanel(new GridLayout(0, 1, 4, 4));
        cargarMenu(panelMenu);
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.add(panelMenu, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(envoltorio);
        scroll.setBorder(BorderFactory.createTitledBorder("Menú con precios (elige la cantidad)"));
        add(scroll, BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout(0, 8));
        sur.add(new JLabel("Notas especiales (sin cebolla, extra salsa, etc.):"), BorderLayout.NORTH);
        sur.add(new JScrollPane(txtNotas), BorderLayout.CENTER);
        sur.add(btnEnviar, BorderLayout.SOUTH);
        add(sur, BorderLayout.SOUTH);

        btnEnviar.addActionListener(this);
    }

    private void cargarMenu(JPanel panelMenu) {
        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nombre, precio FROM platillos WHERE activo = 1 ORDER BY id")) {

            while (rs.next()) {
                Platillo p = new Platillo(rs.getInt("id"), rs.getString("nombre"), rs.getDouble("precio"));
                JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
                JPanel fila = new JPanel(new BorderLayout());
                fila.add(new JLabel(p.getNombre() + "  ($" + String.format("%.2f", p.getPrecio()) + ")"), BorderLayout.CENTER);
                fila.add(spinner, BorderLayout.EAST);
                panelMenu.add(fila);
                platillos.add(p);
                cantidades.add(spinner);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar el menú: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnEnviar) {
            enviar();
        }
    }

    private void enviar() {
        String cliente = txtCliente.getText().trim();
        String notas = txtNotas.getText().trim();
        double total = 0;
        StringBuilder detalle = new StringBuilder();
        int cuantos = 0;

        ArrayList<Object[]> items = new ArrayList<>();

        for (int i = 0; i < platillos.size(); i++) {
            int cantidad = Integer.parseInt(cantidades.get(i).getValue().toString());
            if (cantidad > 0) {
                Platillo p = platillos.get(i);
                total += p.getPrecio() * cantidad;
                if (cuantos > 0) {
                    detalle.append(", ");
                }
                detalle.append(cantidad).append("x ").append(p.getNombre());
                cuantos++;
                items.add(new Object[]{p.getId(), cantidad, p.getPrecio()});
            }
        }

        if (cliente.isEmpty() || cuantos == 0) {
            JOptionPane.showMessageDialog(this, "Escribe el nombre del cliente y elige al menos un platillo.");
            return;
        }

        Connection con = null;
        try {
            con = Conexion.conectar();
            con.setAutoCommit(false);   // Inicio de transacción

            // 1. Insertar cabecera del pedido
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO pedidos (mesa_id, cliente, notas, estado_cocina, total, detalle) VALUES (?, ?, ?, 'Pendiente', ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idMesa);
            ps.setString(2, cliente);
            ps.setString(3, notas);
            ps.setDouble(4, total);
            ps.setString(5, detalle.toString());
            ps.executeUpdate();

            // Obtener el id generado del pedido
            int idPedido = 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    idPedido = keys.getInt(1);
                }
            }
            ps.close();

            // 2. Insertar detalle normalizado
            PreparedStatement psDet = con.prepareStatement(
                    "INSERT INTO detalle_pedido (pedido_id, platillo_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)");
            for (Object[] item : items) {
                psDet.setInt(1, idPedido);
                psDet.setInt(2, (Integer) item[0]);
                psDet.setInt(3, (Integer) item[1]);
                psDet.setDouble(4, (Double) item[2]);
                psDet.addBatch();
            }
            psDet.executeBatch();
            psDet.close();

            // 3. Marcar mesa como ocupada
            PreparedStatement psMesa = con.prepareStatement("UPDATE mesas SET estado = 'ocupada' WHERE id = ?");
            psMesa.setInt(1, idMesa);
            psMesa.executeUpdate();
            psMesa.close();

            con.commit();   // Todo bien

            JOptionPane.showMessageDialog(this, "¡Comanda enviada a cocina con éxito y mesa marcada como ocupada!");
            padre.cargar();
            dispose();

        } catch (Exception ex) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ignored) {}
            }
            JOptionPane.showMessageDialog(this, "Error al enviar la comanda: " + ex.getMessage());
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
