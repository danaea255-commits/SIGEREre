import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class VentanaMesas extends JFrame implements ActionListener {

    private JPanel panelListos = new JPanel(new GridLayout(0, 2, 10, 10));
    private JTabbedPane pestanas = new JTabbedPane();
    private JButton btnEditar = Estilo.boton("Editar Plano de Mesas", Estilo.OSCURO);
    private JButton btnActualizar = Estilo.boton("Actualizar", Estilo.NARANJA);
    private JButton btnSalir = Estilo.boton("Cerrar Sesión", Estilo.ROJO);
    private Timer timer = new Timer(10000, this);
    private boolean esAdmin = "Administrador".equals(Sesion.rol);

    public VentanaMesas() {
        setTitle(esAdmin ? "Mapa de Mesas (Supervisión) - SIGERE" : "Estación de Mesero - SIGERE");
        setMinimumSize(new Dimension(800, 550));
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Estilo.FONDO);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Estilo.ENCABEZADO);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, Estilo.NARANJA),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));
        JPanel izquierda = new JPanel(new GridLayout(2, 1, 0, 2));
        izquierda.setOpaque(false);
        izquierda.add(Estilo.titulo(esAdmin ? "Mapa de Mesas - Supervisión" : "Mapa de Mesas y Comandas"));
        izquierda.add(Estilo.subtitulo((esAdmin ? "Administrador: " : "Mesero en turno: ") + Sesion.usuario));
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        if (esAdmin) {
            derecha.add(btnEditar);
        }
        derecha.add(btnActualizar);
        derecha.add(btnSalir);
        encabezado.add(izquierda, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        panelListos.setOpaque(false);
        JPanel envoltorioListos = new JPanel(new BorderLayout());
        envoltorioListos.setOpaque(false);
        envoltorioListos.add(panelListos, BorderLayout.NORTH);
        JScrollPane scrollListos = new JScrollPane(envoltorioListos);
        scrollListos.setPreferredSize(new Dimension(100, 130));
        scrollListos.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Estilo.BORDE),
                "Alertas de Cocina: pedidos listos para servir",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Estilo.TEXTO));
        scrollListos.getViewport().setBackground(Estilo.FONDO);
        centro.add(scrollListos, BorderLayout.NORTH);

        String tituloMapa = esAdmin
                ? "Mapa de mesas (clic para cambiar estado o reservar)"
                : "Mapa de mesas (clic para comanda, reservar o liberar)";
        pestanas.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Estilo.BORDE),
                tituloMapa,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), Estilo.TEXTO));
        centro.add(pestanas, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        leyenda.setBackground(Estilo.ENCABEZADO);
        leyenda.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE));
        leyenda.add(crearLeyenda("Libre", Estilo.VERDE, Color.WHITE));
        leyenda.add(crearLeyenda("Ocupada", Estilo.ROJO, Color.WHITE));
        leyenda.add(crearLeyenda("Reservada", Estilo.AMARILLO, Color.BLACK));
        leyenda.add(crearLeyenda("Limpieza", Estilo.AZUL, Color.WHITE));
        add(leyenda, BorderLayout.SOUTH);

        btnEditar.addActionListener(this);
        btnActualizar.addActionListener(this);
        btnSalir.addActionListener(this);
        cargar();
        timer.start();
    }

    private JLabel crearLeyenda(String texto, Color fondo, Color letra) {
        JLabel l = new JLabel("  " + texto + "  ");
        l.setOpaque(true);
        l.setBackground(fondo);
        l.setForeground(letra);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return l;
    }

    public void cargar() {
        int pestanaActual = pestanas.getSelectedIndex();
        panelListos.removeAll();
        pestanas.removeAll();

        try {
            Plano.sincronizarZonas();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al preparar las zonas: " + e.getMessage());
            timer.stop();
            return;
        }

        try (Connection con = Conexion.conectar();
             Statement st = con.createStatement()) {

            try (ResultSet rs = st.executeQuery(
                    "SELECT p.id, m.nombre, p.detalle FROM pedidos p "
                  + "JOIN mesas m ON m.id = p.mesa_id "
                  + "WHERE p.estado_cocina = 'Listo' AND p.entregado = 0 ORDER BY p.id")) {

                boolean hayListos = false;
                while (rs.next()) {
                    hayListos = true;
                    JPanel tarjeta = new JPanel(new BorderLayout(10, 0));
                    tarjeta.setBackground(Estilo.FONDO_TARJETA);
                    tarjeta.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Estilo.VERDE, 2),
                            BorderFactory.createEmptyBorder(8, 12, 8, 12)));

                    JLabel info = new JLabel("<html><b style='color:#1e7e34'>" + rs.getString("nombre") + "</b><br>"
                            + rs.getString("detalle") + "<br>Comanda #" + rs.getInt("id") + "</html>");
                    JButton btnEntregado = Estilo.boton("Entregado", Estilo.VERDE);
                    btnEntregado.setActionCommand("E" + rs.getInt("id"));
                    btnEntregado.addActionListener(this);

                    tarjeta.add(info, BorderLayout.CENTER);
                    if (!esAdmin) {
                        tarjeta.add(btnEntregado, BorderLayout.EAST);
                    }
                    panelListos.add(tarjeta);
                }
                if (!hayListos) {
                    JLabel vacio = new JLabel("No hay pedidos listos por el momento.");
                    vacio.setForeground(Estilo.TEXTO_SECUNDARIO);
                    panelListos.add(vacio);
                }
            }

            ArrayList<String> zonas = new ArrayList<>();
            ArrayList<JPanel> planos = new ArrayList<>();
            try (ResultSet rs = st.executeQuery("SELECT nombre FROM zonas ORDER BY id")) {
                while (rs.next()) {
                    JPanel plano = new JPanel(null);
                    plano.setPreferredSize(new Dimension(Plano.ANCHO, Plano.ALTO));
                    plano.setBackground(Estilo.FONDO_TARJETA);
                    plano.setBorder(BorderFactory.createLineBorder(Estilo.BORDE));
                    zonas.add(rs.getString("nombre"));
                    planos.add(plano);
                }
            }

            try (ResultSet rs = st.executeQuery(
                    "SELECT id, nombre, zona, estado, pos_x, pos_y FROM mesas WHERE activa = 1 ORDER BY zona, id")) {
                String zonaAnterior = "";
                int numero = 0;
                while (rs.next()) {
                    String zona = rs.getString("zona");
                    if (!zona.equals(zonaAnterior)) {
                        numero = 0;
                        zonaAnterior = zona;
                    }
                    int x = rs.getInt("pos_x");
                    boolean sinX = rs.wasNull();
                    int y = rs.getInt("pos_y");
                    boolean sinY = rs.wasNull();
                    if (sinX || sinY) {
                        x = Plano.posicionX(numero);
                        y = Plano.posicionY(numero);
                    }
                    numero++;

                    String estado = rs.getString("estado");
                    if (estado == null) estado = "libre";
                    JButton btnMesa = new JButton("<html><center><b>" + rs.getString("nombre")
                            + "</b><br><small>" + estado.toUpperCase() + "</small></center></html>");
                    btnMesa.setBounds(x, y, Plano.ANCHO_MESA, Plano.ALTO_MESA);
                    btnMesa.setOpaque(true);
                    btnMesa.setBorderPainted(false);
                    btnMesa.setFocusPainted(false);
                    btnMesa.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    btnMesa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    btnMesa.setBackground(Estilo.colorEstado(estado));
                    btnMesa.setForeground(Estilo.textoEstado(estado));

                    btnMesa.setName(rs.getString("nombre"));
                    btnMesa.setActionCommand("M" + rs.getInt("id") + "|" + estado);
                    btnMesa.addActionListener(this);

                    int indice = zonas.indexOf(zona);
                    if (indice >= 0) {
                        planos.get(indice).add(btnMesa);
                    }
                }
            }

            for (int i = 0; i < zonas.size(); i++) {
                JScrollPane sp = new JScrollPane(planos.get(i));
                sp.getViewport().setBackground(Estilo.FONDO_TARJETA);
                pestanas.addTab(zonas.get(i), sp);
            }
            if (zonas.isEmpty()) {
                pestanas.addTab("Sin zonas", new JLabel("  El administrador todavía no ha creado zonas."));
            }
            if (pestanaActual >= 0 && pestanaActual < pestanas.getTabCount()) {
                pestanas.setSelectedIndex(pestanaActual);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos: " + e.getMessage());
            timer.stop();
        }

        panelListos.revalidate();
        panelListos.repaint();
        pestanas.revalidate();
        pestanas.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer || e.getSource() == btnActualizar) {
            cargar();
        } else if (e.getSource() == btnEditar) {
            new VentanaEditorMesas(this).setVisible(true);
        } else if (e.getSource() == btnSalir) {
            Sesion.usuario = "";
            Sesion.rol = "";
            new VentanaLogin().setVisible(true);
            dispose();
        } else {
            String comando = e.getActionCommand();
            if (comando != null && comando.startsWith("M")) {
                String[] partes = comando.substring(1).split("\\|", 2);
                int idMesa = Integer.parseInt(partes[0]);
                String estado = partes.length > 1 ? partes[1] : "libre";
                JButton boton = (JButton) e.getSource();
                String nombreMesa = boton.getName();

                if (esAdmin) {
                    mostrarMenuAdmin(idMesa, nombreMesa, estado);
                } else {
                    mostrarMenuMesero(idMesa, nombreMesa, estado);
                }
            } else if (comando != null && comando.startsWith("E") && !esAdmin) {
                marcarEntregado(Integer.parseInt(comando.substring(1)));
            }
        }
    }

    private void mostrarMenuAdmin(int idMesa, String nombreMesa, String estadoActual) {
        String[] opciones = {
                "Libre",
                "Reservar mesa",
                "Ocupada",
                "Limpieza",
                "Cancelar"
        };
        int eleccion = JOptionPane.showOptionDialog(
                this,
                "Mesa: " + nombreMesa + "\nEstado actual: " + estadoActual.toUpperCase()
                        + "\n\nEl administrador solo supervisa. Elija el nuevo estado:",
                "Supervisar mesa",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (eleccion < 0 || eleccion == 4) return;

        String nuevo;
        switch (eleccion) {
            case 0: nuevo = "libre"; break;
            case 1: nuevo = "reservada"; break;
            case 2: nuevo = "ocupada"; break;
            case 3: nuevo = "limpieza"; break;
            default: return;
        }

        if (!nuevo.equals(estadoActual)) {
            cambiarEstadoMesa(idMesa, nuevo);
        }
    }

    private void mostrarMenuMesero(int idMesa, String nombreMesa, String estado) {
        if ("libre".equals(estado)) {
            String[] opciones = {"Tomar comanda", "Reservar mesa", "Cancelar"};
            int eleccion = JOptionPane.showOptionDialog(
                    this,
                    "Mesa libre: " + nombreMesa + "\n¿Qué desea hacer?",
                    "Mesa libre",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]);
            if (eleccion == 0) {
                new VentanaComanda(this, idMesa, nombreMesa).setVisible(true);
            } else if (eleccion == 1) {
                cambiarEstadoMesa(idMesa, "reservada");
            }
        } else if ("reservada".equals(estado)) {
            String[] opciones = {"Tomar comanda", "Cancelar reserva", "Cancelar"};
            int eleccion = JOptionPane.showOptionDialog(
                    this,
                    "Mesa reservada: " + nombreMesa + "\n¿Qué desea hacer?",
                    "Mesa reservada",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]);
            if (eleccion == 0) {
                new VentanaComanda(this, idMesa, nombreMesa).setVisible(true);
            } else if (eleccion == 1) {
                cambiarEstadoMesa(idMesa, "libre");
            }
        } else if ("limpieza".equals(estado)) {
            String[] opciones = {"Marcar como libre (terminada)", "Nueva comanda", "Cancelar"};
            int eleccion = JOptionPane.showOptionDialog(
                    this,
                    "Mesa en limpieza: " + nombreMesa
                            + "\n\nCuando la mesa esté lista para el siguiente cliente,\nmárquela como libre.",
                    "Terminar mesa",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]);
            if (eleccion == 0) {
                cambiarEstadoMesa(idMesa, "libre");
            } else if (eleccion == 1) {
                new VentanaComanda(this, idMesa, nombreMesa).setVisible(true);
            }
        } else if ("ocupada".equals(estado)) {
            JOptionPane.showMessageDialog(this,
                    "La mesa " + nombreMesa + " está ocupada.\n"
                            + "1. Cocina prepara el pedido.\n"
                            + "2. Usted marca entregado cuando esté listo.\n"
                            + "3. Caja cobra y la mesa pasa a limpieza.\n"
                            + "4. Luego puede marcarla como libre (terminada).");
        }
    }

    private void cambiarEstadoMesa(int idMesa, String nuevo) {
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement("UPDATE mesas SET estado = ? WHERE id = ?")) {
            ps.setString(1, nuevo);
            ps.setInt(2, idMesa);
            ps.executeUpdate();
            cargar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cambiar estado: " + ex.getMessage());
        }
    }

    private void marcarEntregado(int idPedido) {
        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement("UPDATE pedidos SET entregado = 1 WHERE id = ?")) {
            ps.setInt(1, idPedido);
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
