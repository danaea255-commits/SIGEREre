import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VentanaMenu extends JFrame implements ActionListener {

    private DefaultTableModel modelo = new DefaultTableModel(new String[]{"ID", "Platillo o Bebida", "Precio (MXN)"}, 0);
    private JTable tabla = new JTable(modelo);
    private JTextField txtNombre = new JTextField();
    private JTextField txtPrecio = new JTextField();
    private JButton btnGuardar = Estilo.boton("Guardar Platillo", Estilo.NARANJA);
    private JButton btnEliminar = Estilo.boton("Eliminar Seleccionado", Estilo.ROJO);

    public VentanaMenu() {
        setTitle("Gestión de Menú - SIGERE");
        setSize(700, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Estilo.FONDO);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        norte.setOpaque(false);
        norte.add(Estilo.titulo("Menú Interactivo del Restaurante"), BorderLayout.WEST);
        add(norte, BorderLayout.NORTH);

        tabla.setDefaultEditor(Object.class, null);
        tabla.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        add(scroll, BorderLayout.CENTER);

        if ("Administrador".equals(Sesion.rol)) {
            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.setBorder(BorderFactory.createTitledBorder("Agregar nuevo platillo (Administrador)"));
            form.setOpaque(false);
            form.add(new JLabel("Nombre del platillo o bebida:"));
            form.add(txtNombre);
            form.add(new JLabel("Precio ($ MXN):"));
            form.add(txtPrecio);
            form.add(btnEliminar);
            form.add(btnGuardar);
            JPanel sur = new JPanel(new BorderLayout());
            sur.setOpaque(false);
            sur.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
            sur.add(form, BorderLayout.CENTER);
            add(sur, BorderLayout.SOUTH);
            btnGuardar.addActionListener(this);
            btnEliminar.addActionListener(this);
        } else {
            JLabel aviso = new JLabel("Estás conectado como empleado. Solo el Administrador puede modificar el menú.", SwingConstants.CENTER);
            aviso.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 5));
            add(aviso, BorderLayout.SOUTH);
        }

        cargar();
    }

    private void cargar() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nombre, precio FROM platillos WHERE activo = 1 ORDER BY id")) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        String.format("$%.2f", rs.getDouble("precio"))
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar el menú: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnGuardar) {
            guardar();
        } else if (e.getSource() == btnEliminar) {
            eliminar();
        }
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim().replace(",", "."));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ingresa un precio válido.");
            return;
        }

        if (nombre.isEmpty() || precio <= 0) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa un nombre y un precio válido.");
            return;
        }

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO platillos (nombre, precio) VALUES (?, ?)")) {
            ps.setString(1, nombre);
            ps.setDouble(2, precio);
            ps.executeUpdate();
            txtNombre.setText("");
            txtPrecio.setText("");
            cargar();
            JOptionPane.showMessageDialog(this, "¡Platillo agregado al menú correctamente!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un platillo de la tabla.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar este platillo?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        int id = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
        // Soft delete (activo = 0) para no romper pedidos históricos
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement("UPDATE platillos SET activo = 0 WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            cargar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
        }
    }
}
