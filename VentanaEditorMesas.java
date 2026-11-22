import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class VentanaEditorMesas extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
    private VentanaMesas padre;
    private JComboBox<String> cmbZonas = new JComboBox<String>();
    private JPanel plano = new JPanel(null);
    private BotonMesa seleccionada = null;
    private boolean cargandoZonas = false;
    private int desplazamientoX = 0;
    private int desplazamientoY = 0;

    private JButton btnNuevaZona = Estilo.boton("Nueva zona", Estilo.NARANJA);
    private JButton btnRenombrarZona = Estilo.boton("Renombrar zona", Estilo.GRIS);
    private JButton btnEliminarZona = Estilo.boton("Eliminar zona", Estilo.ROJO);
    private JButton btnNuevaMesa = Estilo.boton("Agregar mesa", Estilo.VERDE);
    private JButton btnRenombrarMesa = Estilo.boton("Renombrar mesa", Estilo.GRIS);
    private JButton btnCambiarZona = Estilo.boton("Cambiar de zona", Estilo.GRIS);
    private JButton btnEliminarMesa = Estilo.boton("Eliminar mesa", Estilo.ROJO);
    private JButton btnCerrar = Estilo.boton("Cerrar", Estilo.OSCURO);

    public VentanaEditorMesas(VentanaMesas padre) {
        this.padre = padre;
        setTitle("Editar Plano de Mesas - SIGERE");
        setMinimumSize(new Dimension(850, 600));
        setSize(1000, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Estilo.FONDO);

        JPanel norte = new JPanel(new GridLayout(2, 1, 0, 5));
        norte.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        norte.setOpaque(false);
        norte.add(Estilo.titulo("Editor de zonas y mesas  (arrastra las mesas con el mouse para acomodarlas)"));
        JPanel zonas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        zonas.setOpaque(false);
        zonas.add(new JLabel("Zona:"));
        zonas.add(cmbZonas);
        zonas.add(btnNuevaZona);
        zonas.add(btnRenombrarZona);
        zonas.add(btnEliminarZona);
        norte.add(zonas);
        add(norte, BorderLayout.NORTH);

        plano.setPreferredSize(new Dimension(Plano.ANCHO, Plano.ALTO));
        plano.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(plano);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        add(scroll, BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sur.setOpaque(false);
        sur.add(btnNuevaMesa);
        sur.add(btnRenombrarMesa);
        sur.add(btnCambiarZona);
        sur.add(btnEliminarMesa);
        sur.add(btnCerrar);
        add(sur, BorderLayout.SOUTH);

        cmbZonas.addActionListener(this);
        btnNuevaZona.addActionListener(this);
        btnRenombrarZona.addActionListener(this);
        btnEliminarZona.addActionListener(this);
        btnNuevaMesa.addActionListener(this);
        btnRenombrarMesa.addActionListener(this);
        btnCambiarZona.addActionListener(this);
        btnEliminarMesa.addActionListener(this);
        btnCerrar.addActionListener(this);

        cargarZonas(null);
    }

    private void cargarZonas(String elegir) {
        cargandoZonas = true;
        cmbZonas.removeAllItems();
        try {
            Plano.sincronizarZonas();
            Connection con = Conexion.conectar();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT nombre FROM zonas ORDER BY id");
            while (rs.next()) {
                cmbZonas.addItem(rs.getString("nombre"));
            }
            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar las zonas: " + e.getMessage());
        }
        if (elegir != null) {
            cmbZonas.setSelectedItem(elegir);
        }
        cargandoZonas = false;
        cargarPlano();
    }

    private void cargarPlano() {
        plano.removeAll();
        seleccionada = null;
        String zona = (String) cmbZonas.getSelectedItem();
        if (zona != null) {
            try {
                Connection con = Conexion.conectar();
                PreparedStatement ps = con.prepareStatement("SELECT id, nombre, pos_x, pos_y FROM mesas WHERE zona = ? AND activa = 1 ORDER BY id");
                ps.setString(1, zona);
                ResultSet rs = ps.executeQuery();
                int numero = 0;
                while (rs.next()) {
                    int x = rs.getInt("pos_x");
                    boolean sinX = rs.wasNull();
                    int y = rs.getInt("pos_y");
                    boolean sinY = rs.wasNull();
                    BotonMesa b = new BotonMesa(rs.getInt("id"), rs.getString("nombre"));
                    if (sinX || sinY) {
                        x = Plano.posicionX(numero);
                        y = Plano.posicionY(numero);
                        guardarPosicion(b.getId(), x, y);
                    }
                    b.setBounds(x, y, Plano.ANCHO_MESA, Plano.ALTO_MESA);
                    b.addMouseListener(this);
                    b.addMouseMotionListener(this);
                    plano.add(b);
                    numero++;
                }
                con.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al cargar las mesas: " + e.getMessage());
            }
        }
        plano.revalidate();
        plano.repaint();
    }

    private void guardarPosicion(int idMesa, int x, int y) {
        try {
            Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement("UPDATE mesas SET pos_x = ?, pos_y = ? WHERE id = ?");
            ps.setInt(1, x);
            ps.setInt(2, y);
            ps.setInt(3, idMesa);
            ps.executeUpdate();
            con.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar la posición: " + e.getMessage());
        }
    }

    public void mousePressed(MouseEvent e) {
        BotonMesa b = (BotonMesa) e.getSource();
        if (seleccionada != null) {
            seleccionada.marcar(false);
        }
        seleccionada = b;
        b.marcar(true);
        desplazamientoX = e.getX();
        desplazamientoY = e.getY();
    }

    public void mouseDragged(MouseEvent e) {
        BotonMesa b = (BotonMesa) e.getSource();
        Point p = SwingUtilities.convertPoint(b, e.getPoint(), plano);
        int x = p.x - desplazamientoX;
        int y = p.y - desplazamientoY;
        x = Math.max(0, Math.min(x, Plano.ANCHO - Plano.ANCHO_MESA));
        y = Math.max(0, Math.min(y, Plano.ALTO - Plano.ALTO_MESA));
        b.setLocation(x, y);
    }

    public void mouseReleased(MouseEvent e) {
        BotonMesa b = (BotonMesa) e.getSource();
        int x = (b.getX() + 5) / 10 * 10;
        int y = (b.getY() + 5) / 10 * 10;
        b.setLocation(x, y);
        guardarPosicion(b.getId(), x, y);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    private boolean hayMesaSeleccionada() {
        if (seleccionada == null) {
            JOptionPane.showMessageDialog(this, "Primero haz clic en una mesa del plano.");
            return false;
        }
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cmbZonas) {
            if (!cargandoZonas) {
                cargarPlano();
            }
        } else if (e.getSource() == btnNuevaZona) {
            String nombre = JOptionPane.showInputDialog(this, "Nombre de la nueva zona:");
            if (nombre != null && !nombre.trim().isEmpty()) {
                crearZona(nombre.trim());
            }
        } else if (e.getSource() == btnRenombrarZona) {
            String actual = (String) cmbZonas.getSelectedItem();
            if (actual != null) {
                String nombre = JOptionPane.showInputDialog(this, "Nuevo nombre para la zona:", actual);
                if (nombre != null && !nombre.trim().isEmpty()) {
                    renombrarZona(nombre.trim());
                }
            }
        } else if (e.getSource() == btnEliminarZona) {
            if (cmbZonas.getSelectedItem() != null) {
                int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar la zona " + cmbZonas.getSelectedItem() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (respuesta == JOptionPane.YES_OPTION) {
                    eliminarZona();
                }
            }
        } else if (e.getSource() == btnNuevaMesa) {
            if (cmbZonas.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Primero crea una zona.");
            } else {
                String nombre = JOptionPane.showInputDialog(this, "Nombre de la nueva mesa:");
                if (nombre != null && !nombre.trim().isEmpty()) {
                    crearMesa(nombre.trim());
                }
            }
        } else if (e.getSource() == btnRenombrarMesa) {
            if (hayMesaSeleccionada()) {
                String nombre = JOptionPane.showInputDialog(this, "Nuevo nombre para la mesa:", seleccionada.getNombre());
                if (nombre != null && !nombre.trim().isEmpty()) {
                    renombrarMesa(nombre.trim());
                }
            }
        } else if (e.getSource() == btnCambiarZona) {
            if (hayMesaSeleccionada()) {
                Object[] opciones = new Object[cmbZonas.getItemCount()];
                for (int i = 0; i < opciones.length; i++) {
                    opciones[i] = cmbZonas.getItemAt(i);
                }
                Object elegida = JOptionPane.showInputDialog(this, "Mover " + seleccionada.getNombre() + " a la zona:", "Cambiar de zona",
                        JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
                if (elegida != null) {
                    moverAZona(elegida.toString());
                }
            }
        } else if (e.getSource() == btnEliminarMesa) {
            if (hayMesaSeleccionada()) {
                int respuesta = JOptionPane.showConfirmDialog(this, "¿Eliminar la mesa " + seleccionada.getNombre() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (respuesta == JOptionPane.YES_OPTION) {
                    eliminarMesa();
                }
            }
        } else if (e.getSource() == btnCerrar) {
            dispose();
        }
    }

    private void crearZona(String nombre) {
        try {
            Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement("INSERT INTO zonas (nombre) VALUES (?)");
            ps.setString(1, nombre);
            ps.executeUpdate();
            con.close();
            cargarZonas(nombre);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Esa zona ya existe.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void renombrarZona(String nuevo) {
        String actual = (String) cmbZonas.getSelectedItem();
        try {
            Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement("UPDATE zonas SET nombre = ? WHERE nombre = ?");
            ps.setString(1, nuevo);
            ps.setString(2, actual);
            ps.executeUpdate();
            PreparedStatement ps2 = con.prepareStatement("UPDATE mesas SET zona = ? WHERE zona = ?");
            ps2.setString(1, nuevo);
            ps2.setString(2, actual);
            ps2.executeUpdate();
            con.close();
            cargarZonas(nuevo);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Ya existe una zona con ese nombre.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void eliminarZona() {
        String zona = (String) cmbZonas.getSelectedItem();
        try {
            Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mesas WHERE zona = ? AND activa = 1");
            ps.setString(1, zona);
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                con.close();
                JOptionPane.showMessageDialog(this, "Esa zona todavía tiene mesas. Muévelas a otra zona o elimínalas primero.");
                return;
            }
            PreparedStatement ps2 = con.prepareStatement("DELETE FROM zonas WHERE nombre = ?");
            ps2.setString(1, zona);
            ps2.executeUpdate();
            con.close();
            cargarZonas(null);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void crearMesa(String nombre) {
        String zona = (String) cmbZonas.getSelectedItem();
        try {
            Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM mesas WHERE zona = ? AND activa = 1");
            ps.setString(1, zona);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int numero = rs.getInt(1);

            PreparedStatement ps2 = con.prepareStatement("INSERT INTO mesas (nombre, zona, estado, pos_x, pos_y) VALUES (?, ?, 'libre', ?, ?)");
            ps2.setString(1, nombre);
            ps2.setString(2, zona);
            ps2.setInt(3, Plano.posicionX(numero));
            ps2.setInt(4, Plano.posicionY(numero));
            ps2.executeUpdate();
            con.close();
            cargarPlano();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al crear la mesa: " + e.getMessage());
        }
    }

    private void renombrarMesa(String nuevo) {
        try {
            Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement("UPDATE mesas SET nombre = ? WHERE id = ?");
            ps.setString(1, nuevo);
            ps.setInt(2, seleccionada.getId());
            ps.executeUpdate();
            con.close();
            cargarPlano();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void moverAZona(String zonaNueva) {
        try {
            Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement("UPDATE mesas SET zona = ?, pos_x = NULL, pos_y = NULL WHERE id = ?");
            ps.setString(1, zonaNueva);
            ps.setInt(2, seleccionada.getId());
            ps.executeUpdate();
            con.close();
            cargarPlano();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void eliminarMesa() {
        try {
            Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM pedidos WHERE mesa_id = ? AND (pagado = 0 OR entregado = 0)");
            ps.setInt(1, seleccionada.getId());
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                con.close();
                JOptionPane.showMessageDialog(this, "Esa mesa tiene pedidos sin entregar o sin cobrar. No se puede eliminar todavía.");
                return;
            }
            PreparedStatement ps2 = con.prepareStatement("UPDATE mesas SET activa = 0 WHERE id = ?");
            ps2.setInt(1, seleccionada.getId());
            ps2.executeUpdate();
            con.close();
            cargarPlano();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public void dispose() {
        padre.cargar();
        super.dispose();
    }
}
