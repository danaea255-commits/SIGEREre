import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VentanaUsuarios extends JFrame implements ActionListener {

    private DefaultTableModel modelo = new DefaultTableModel(new String[]{"ID", "Nombre", "Correo", "Rol"}, 0);
    private JTable tabla = new JTable(modelo);
    private JTextField txtNombre = new JTextField();
    private JTextField txtCorreo = new JTextField();
    private JPasswordField txtClave = new JPasswordField();
    private JComboBox<String> cmbRol = new JComboBox<>(new String[]{"Mesero", "Administrador"});
    private JButton btnGuardar = Estilo.boton("Guardar Personal", Estilo.NARANJA);
    private JButton btnEliminar = Estilo.boton("Eliminar Seleccionado", Estilo.ROJO);

    public VentanaUsuarios() {
        setTitle("Control de Personal - SIGERE");
        setSize(760, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Estilo.FONDO);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        norte.add(Estilo.titulo("Control de Personal y Credenciales"), BorderLayout.WEST);
        add(norte, BorderLayout.NORTH);

        tabla.setDefaultEditor(Object.class, null);
        tabla.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        add(scroll, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createTitledBorder("Registrar nuevo personal"));
        form.add(new JLabel("Nombre completo:"));
        form.add(txtNombre);
        form.add(new JLabel("Correo electrónico (usuario):"));
        form.add(txtCorreo);
        form.add(new JLabel("Contraseña temporal:"));
        form.add(txtClave);
        form.add(new JLabel("Rol en el sistema:"));
        form.add(cmbRol);
        form.add(btnEliminar);
        form.add(btnGuardar);

        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);
        sur.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        sur.add(form, BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);

        btnGuardar.addActionListener(this);
        btnEliminar.addActionListener(this);
        cargar();
    }

    private void cargar() {
        modelo.setRowCount(0);
        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nombre, correo, rol FROM usuario ORDER BY id")) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getString("rol")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar el personal: " + e.getMessage());
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
        String correo = txtCorreo.getText().trim();
        String clave = new String(txtClave.getPassword()).trim();
        String rol = cmbRol.getSelectedItem().toString();

        if (nombre.isEmpty() || correo.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, completa todos los campos.");
            return;
        }

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO usuario (nombre, correo, clave, rol) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.setString(3, clave);
            ps.setString(4, rol);
            ps.executeUpdate();
            txtNombre.setText("");
            txtCorreo.setText("");
            txtClave.setText("");
            cargar();
            JOptionPane.showMessageDialog(this, "¡Personal registrado correctamente!");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                JOptionPane.showMessageDialog(this, "Error: El correo ya está registrado.");
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla.");
            return;
        }

        String correo = modelo.getValueAt(fila, 2).toString();
        if ("admin@sigere.com".equals(correo)) {
            JOptionPane.showMessageDialog(this, "Este usuario está protegido y no se puede eliminar.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar a este usuario?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        int id = Integer.parseInt(modelo.getValueAt(fila, 0).toString());
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement("DELETE FROM usuario WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            cargar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
        }
    }
}
