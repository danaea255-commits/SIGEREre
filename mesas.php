<?php
session_start();
require_once 'conexion.php';

// Validar que sea mesero o administrador
if (!isset($_SESSION['usuario'])) {
    header("Location: login.php");
    exit;
}

$nombreMesero = $_SESSION['usuario'];
$mensaje = '';

// Procesar comanda y cambiar estado de mesa a ocupada automáticamente
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['accion']) && $_POST['accion'] === 'crear_pedido') {
    $mesa_id = $_POST['mesa_id'] ?? 0;
    $cliente = trim($_POST['cliente'] ?? '');
    $notas = trim($_POST['notas'] ?? '');
    $platillos = $_POST['platillos'] ?? [];

    if (!empty($mesa_id) && !empty($platillos)) {
        try {
            $total = count($platillos) * 85.00; 

            // 1. Guardar pedido en base de datos
            $stmt = $pdo->prepare("INSERT INTO pedidos (mesa_id, cliente, notas, estado_cocina, total) VALUES (:mesa_id, :cliente, :notas, 'Pendiente', :total)");
            $stmt->execute(['mesa_id' => $mesa_id, 'cliente' => $cliente, 'notas' => $notas, 'total' => $total]);

            // 2. Ocupar mesa automáticamente (cambiar a rojo)
            $stmtMesa = $pdo->prepare("UPDATE mesas SET estado = 'ocupada' WHERE id = :id");
            $stmtMesa->execute(['id' => $mesa_id]);

            $mensaje = "¡Comanda enviada a cocina con éxito y mesa marcada como ocupada!";
        } catch (Exception $e) {
            $mensaje = "Error al enviar la comanda.";
        }
    } else {
        $mensaje = "Selecciona una mesa y al menos un platillo del menú.";
    }
}

// Obtener mesas desde la base de datos
$stmtMesas = $pdo->query("SELECT * FROM mesas");
$mesas = $stmtMesas->fetchAll(PDO::FETCH_ASSOC);

// Obtener platillos del menú
$stmtMenu = $pdo->query("SELECT * FROM platillos");
$menu = $stmtMenu->fetchAll(PDO::FETCH_ASSOC);

// Simulación de avisos de cocina cuando el pedido ya está listo para recoger
$pedidos_listos = [
    ['id' => 103, 'mesa' => 'Terraza 1', 'platillos' => '1x Agua de Horchata 1L', 'hora' => '18:20 PM'],
    ['id' => 100, 'mesa' => 'General 1', 'platillos' => '2x Tacos al Pastor', 'hora' => '18:10 PM']
];
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Estación de Mesero - SIGERE</title>
    <style>
        :root {
            --bg-main: #f8f9fa;
            --accent-orange: #ff6600;
            --libre: #28a745;
            --ocupada: #dc3545;
            --reservada: #ffc107;
            --success-green: #10b981;
        }
        body { font-family: Arial, sans-serif; background-color: var(--bg-main); color: #222; margin: 0; padding: 20px; }
        .container { max-width: 1100px; margin: auto; background: #fff; padding: 30px; border-radius: 8px; border: 1px solid #e0e0e0; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        
        .header-mesero { display: flex; justify-content: space-between; align-items: center; border-bottom: 3px solid var(--accent-orange); padding-bottom: 15px; margin-bottom: 25px; }
        .header-mesero h2 { color: var(--accent-orange); margin: 0; }
        .btn-logout { background: #fee2e2; color: #991b1b; padding: 6px 12px; border-radius: 6px; text-decoration: none; font-weight: bold; font-size: 13px; }
        .btn-logout:hover { background: #fecaca; }

        /* Alertas de Cocina (Pedidos Listos) */
        .alertas-cocina-box { background: #ecfdf5; border: 1px solid #a7f3d0; padding: 20px; border-radius: 8px; margin-bottom: 25px; }
        .alertas-cocina-box h3 { margin-top: 0; color: #065f46; font-size: 16px; display: flex; align-items: center; gap: 8px; }
        .listos-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 15px; margin-top: 15px; }
        .listo-card { background: white; border-left: 5px solid var(--success-green); padding: 12px 15px; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.02); display: flex; justify-content: space-between; align-items: center; }
        .listo-info .mesa-target { font-weight: bold; color: #064e3b; font-size: 15px; }
        .listo-info .detalle { font-size: 13px; color: #4b5563; margin-top: 3px; }
        .btn-entregado { background: var(--success-green); color: white; border: none; padding: 6px 12px; border-radius: 4px; font-size: 12px; font-weight: bold; cursor: pointer; }
        .btn-entregado:hover { background: #059669; }

        /* Mapa de Mesas por Zonas */
        .leyenda { display: flex; gap: 20px; margin-bottom: 20px; font-size: 14px; align-items: center; }
        .dot { width: 15px; height: 15px; border-radius: 50%; display: inline-block; vertical-align: middle; margin-right: 5px; }
        .dot-libre { background: var(--libre); }
        .dot-ocupada { background: var(--ocupada); }
        .dot-reservada { background: var(--reservada); }

        .plano-container { border: 2px dashed #ccc; padding: 20px; border-radius: 8px; background: #fafafa; display: flex; flex-direction: column; gap: 25px; margin-bottom: 30px; }
        .zona-titulo { font-size: 16px; font-weight: bold; color: #444; border-bottom: 2px solid var(--accent-orange); padding-bottom: 5px; margin-bottom: 10px; }
        .mesas-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 15px; }
        
        .mesa-card { background: #fff; border: 2px solid #ddd; padding: 15px; border-radius: 8px; text-align: center; cursor: pointer; transition: all 0.2s ease; box-shadow: 0 2px 4px rgba(0,0,0,0.02); }
        .mesa-card:hover { transform: translateY(-3px); border-color: var(--accent-orange); box-shadow: 0 4px 8px rgba(255,102,0,0.15); }
        .mesa-card.libre { border-left: 6px solid var(--libre); }
        .mesa-card.ocupada { border-left: 6px solid var(--ocupada); background: #fff5f5; }
        .mesa-card.reservada { border-left: 6px solid var(--reservada); background: #fffdf0; }
        .mesa-num { font-size: 16px; font-weight: bold; color: #222; }
        .mesa-estado { font-size: 12px; color: #666; margin-top: 5px; font-weight: bold; }

        /* Modal Interactiva de Comanda */
        .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); justify-content: center; align-items: center; }
        .modal-content { background: #fff; padding: 30px; border-radius: 8px; width: 100%; max-width: 550px; max-height: 90vh; overflow-y: auto; position: relative; box-shadow: 0 4px 20px rgba(0,0,0,0.2); }
        .close { color: #aaa; float: right; font-size: 28px; font-weight: bold; cursor: pointer; }
        .close:hover { color: #000; }
        
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px; }
        .form-group input, .form-group textarea { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        .menu-check-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; max-height: 160px; overflow-y: auto; border: 1px solid #eee; padding: 10px; border-radius: 4px; background: #fdfdfd; }
        .btn-accion { background: var(--accent-orange); color: white; border: none; padding: 12px; width: 100%; font-weight: bold; border-radius: 4px; cursor: pointer; font-size: 15px; margin-top: 5px; }
        .btn-accion:hover { background: #e65c00; }
        .msg { background: #e2f0d9; color: #385723; padding: 10px; border-radius: 4px; margin-bottom: 15px; font-weight: bold; text-align: center; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header-mesero">
            <div>
                <h2>🗺️ Mapa de Mesas y Menú - Estación de Mesero</h2>
                <span style="font-size: 13px; color: #666;">Mesero en turno: <strong><?php echo htmlspecialchars($nombreMesero); ?></strong></span>
            </div>
            <a href="login.php" class="btn-logout">Cerrar Sesión</a>
        </div>

        <?php if (!empty($mensaje)): ?>
            <div class="msg"><?php echo $mensaje; ?></div>
        <?php endif; ?>

        <!-- AVISOS DE COCINA: Pedidos Listos -->
        <div class="alertas-cocina-box">
            <h3>🔔 Alertas de Cocina: ¡Pedidos Listos para Servir!</h3>
            <p style="margin: 0; font-size: 13px; color: #047857;">La cocina ha marcado los siguientes platillos como listos para llevar a la mesa:</p>
            
            <div class="listos-grid">
                <?php foreach ($pedidos_listos as $p_listo): ?>
                    <div class="listo-card">
                        <div class="listo-info">
                            <div class="mesa-target">📍 <?php echo htmlspecialchars($p_listo['mesa']); ?></div>
                            <div class="detalle"><?php echo htmlspecialchars($p_listo['platillos']); ?></div>
                            <div style="font-size: 11px; color: #6b7280; margin-top: 2px;">Comanda #<?php echo $p_listo['id']; ?> | <?php echo $p_listo['hora']; ?></div>
                        </div>
                        <button class="btn-entregado" onclick="alert('Pedido marcado como entregado.'); location.reload();">Entregado</button>
                    </div>
                <?php endforeach; ?>
            </div>
        </div>

        <!-- LEYENDA DEL MAPA -->
        <div class="leyenda">
            <span><span class="dot dot-libre"></span> Libre</span>
            <span><span class="dot dot-ocupada"></span> Ocupada</span>
            <span><span class="dot dot-reservada"></span> Reservada</span>
        </div>

        <!-- PLANO DE MESAS INTERACTIVO -->
        <div class="plano-container">
            <div class="mesas-grid">
                <?php foreach ($mesas as $m): ?>
                    <div class="mesa-card <?php echo $m['estado']; ?>" onclick="abrirModal(<?php echo $m['id']; ?>, '<?php echo htmlspecialchars($m['nombre']); ?>')">
                        <div class="mesa-num"><?php echo htmlspecialchars($m['nombre']); ?></div>
                        <div class="mesa-estado"><?php echo ucfirst($m['estado']); ?></div>
                        <div style="font-size: 11px; color: #888;"><?php echo htmlspecialchars($m['zona']); ?></div>
                    </div>
                <?php endforeach; ?>
            </div>
        </div>
    </div>

    <!-- MODAL DE MENÚ INTERACTIVO Y NOTAS -->
    <div id="mesaModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="cerrarModal()">×</span>
            <h3 id="modalTitulo" style="color: var(--accent-orange); margin-top: 0;">Atender Mesa</h3>
            
            <form action="mesas.php" method="POST">
                <input type="hidden" name="accion" value="crear_pedido">
                <input type="hidden" id="inputMesaId" name="mesa_id">

                <div class="form-group">
                    <label>Nombre del Cliente / Responsable:</label>
                    <input type="text" name="cliente" required placeholder="Ej. Roberto Gómez">
                </div>

                <div class="form-group">
                    <label>Menú Interactivo con Precios:</label>
                    <div class="menu-check-grid">
                        <?php foreach ($menu as $p): ?>
                            <label style="font-weight: normal; font-size: 13px; display: flex; align-items: center; gap: 6px;">
                                <input type="checkbox" name="platillos[]" value="<?php echo $p['id']; ?>">
                                <?php echo htmlspecialchars($p['nombre']); ?> ($<?php echo number_format($p['precio'], 2); ?>)
                            </label>
                        <?php endforeach; ?>
                    </div>
                </div>

                <div class="form-group">
                    <label>Notas Especiales / Especificaciones (Sin cebolla, extra salsa, etc.):</label>
                    <textarea name="notas" rows="3" placeholder="Peticiones especiales del cliente..."></textarea>
                </div>

                <button type="submit" class="btn-accion">Enviar a Cocina y Ocupar Mesa</button>
            </form>
        </div>
    </div>

    <script>
        function abrirModal(idMesa, nombreMesa) {
            document.getElementById('modalTitulo').innerText = 'Atender Mesa: ' + nombreMesa;
            document.getElementById('inputMesaId').value = idMesa;
            document.getElementById('mesaModal').style.display = 'flex';
        }

        function cerrarModal() {
            document.getElementById('mesaModal').style.display = 'none';
        }

        window.onclick = function(event) {
            let modal = document.getElementById('mesaModal');
            if (event.target == modal) {
                modal.style.display = 'none';
            }
        }
    </script>
</body>
</html>