import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class VentanaLogin extends JFrame implements ActionListener {

    private JTextField txtCorreo = new JTextField();
    private JPasswordField txtClave = new JPasswordField();
    private JLabel lblError = new JLabel(" ", SwingConstants.CENTER);
    private JButton btnEntrar = Estilo.boton("Iniciar Sesión", Estilo.NARANJA);
    private JButton btnCocina = Estilo.boton("Pantalla Cocina", Estilo.OSCURO);
    private JButton btnCaja = Estilo.boton("Caja y Cobros", Estilo.OSCURO);

    public VentanaLogin() {
        setTitle("Acceso - SIGERE");
        setMinimumSize(new Dimension(380, 420));
        setSize(440, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Estilo.FONDO);

        JPanel norte = new JPanel(new GridLayout(2, 1, 0, 4));
        norte.setBackground(Estilo.FONDO);
        norte.setBorder(BorderFactory.createEmptyBorder(28, 20, 10, 20));
        JLabel lblTitulo = new JLabel("SIGERE", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitulo.setForeground(Estilo.NARANJA);
        JLabel lblSub = new JLabel("Sistema de Gestión de Restaurante", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(Estilo.TEXTO_SECUNDARIO);
        norte.add(lblTitulo);
        norte.add(lblSub);
        add(norte, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(0, 1, 6, 8));
        centro.setBackground(Estilo.FONDO);
        centro.setBorder(BorderFactory.createEmptyBorder(10, 45, 10, 45));
        lblError.setForeground(Estilo.ROJO);
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel l1 = new JLabel("Correo Electrónico");
        l1.setForeground(Estilo.TEXTO);
        centro.add(l1);
        centro.add(txtCorreo);
        JLabel l2 = new JLabel("Contraseña");
        l2.setForeground(Estilo.TEXTO);
        centro.add(l2);
        centro.add(txtClave);
        centro.add(lblError);
        centro.add(btnEntrar);
        add(centro, BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        sur.setBackground(Estilo.FONDO);
        sur.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Estilo.BORDE),
                "Acceso directo a estaciones",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Segoe UI", Font.PLAIN, 11), Estilo.TEXTO_SECUNDARIO));
        sur.add(btnCocina);
        sur.add(btnCaja);
        add(sur, BorderLayout.SOUTH);

        btnEntrar.addActionListener(this);
        btnCocina.addActionListener(this);
        btnCaja.addActionListener(this);
        getRootPane().setDefaultButton(btnEntrar);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCocina) {
            new VentanaCocina().setVisible(true);
        } else if (e.getSource() == btnCaja) {
            new VentanaCaja().setVisible(true);
        } else if (e.getSource() == btnEntrar) {
            iniciarSesion();
        }
    }

    private void iniciarSesion() {
        String correo = txtCorreo.getText().trim();
        String clave = new String(txtClave.getPassword()).trim();

        if (correo.isEmpty() || clave.isEmpty()) {
            lblError.setText("Por favor, completa todos los campos.");
            return;
        }

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT nombre, clave, rol FROM usuario WHERE correo = ?")) {

            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && clave.equals(rs.getString("clave"))) {
                    Sesion.usuario = rs.getString("nombre");
                    Sesion.rol = rs.getString("rol");

                    if ("Administrador".equals(Sesion.rol)) {
                        new VentanaPanel().setVisible(true);
                    } else {
                        new VentanaMesas().setVisible(true);
                    }
                    dispose();
                } else {
                    lblError.setText("Correo o contraseña incorrectos.");
                }
            }
        } catch (Exception ex) {
            lblError.setText("Error de conexión a la base de datos");
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
