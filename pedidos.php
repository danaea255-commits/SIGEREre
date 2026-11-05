<?php
session_start();
require_once 'conexion.php';

if (!isset($_SESSION['usuario']) || $_SESSION['rol'] !== 'Mesero' && $_SESSION['rol'] !== 'Administrador') {
    header("Location: login.php");
    exit;
}

$nombreMesero = $_SESSION['usuario'];

// Procesar el envío de comanda si se selecciona desde aquí
$mensaje = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['accion']) && $_POST['accion'] === 'crear_pedido') {
    $mesa_id = $_POST['mesa_id'] ?? 0;
    $notas = trim($_POST['notas'] ?? '');
    $platillos = $_POST['platillos'] ?? [];

    if (!empty($mesa_id) && !empty($platillos)) {
        try {
            $total = count($platillos) * 85.00; // Simulación de cálculo o conexión al menú
            
            // 1. Guardar pedido en base de datos
            $stmt = $pdo->prepare("INSERT INTO pedidos (mesa_id, cliente, notas, estado_cocina, total) VALUES (:mesa_id, :cliente, :notas, 'Pendiente', :total)");
            $stmt->execute(['mesa_id' => $mesa_id, 'cliente' => 'Cliente en Mesa', 'notas' => $notas, 'total' => $total]);

            // 2. Cambiar estado de la mesa a ocupada automáticamente
            $stmtMesa = $pdo->prepare("UPDATE mesas SET estado = 'ocupada' WHERE id = :id");
            $stmtMesa->execute(['id' => $mesa_id]);

            $mensaje = "¡Comanda enviada a cocina con éxito!";
        } catch (Exception $e) {
            $mensaje = "Error al enviar la comanda.";
        }
    } else {
        $mensaje = "Selecciona una mesa y al menos un platillo.";
    }
}

// Obtener mesas para el selector
$stmtMesas = $pdo->query("SELECT * FROM mesas");
$mesas = $stmtMesas->fetchAll(PDO::FETCH_ASSOC);

// Obtener platillos del menú
$stmtMenu = $pdo->query("SELECT * FROM platillos");
$menu = $stmtMenu->fetchAll(PDO::FETCH_ASSOC);

// Simulación o consulta de pedidos listos en cocina para las alertas del mesero
$pedidos_listos = [
    ['id' => 103, 'mesa' => 'Terraza 1', 'platillos' => '1x Agua de Horchata 1L', 'hora' => '18:20 PM'],
    ['id' => 100, 'mesa' => 'Mesa General 1', 'platillos' => '2x Tacos al Pastor', 'hora' => '18:10 PM']
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
            --card-bg: #ffffff;
            --accent-orange: #ff6600;
            --text-dark: #222222;
            --text-gray: #666666;
            --border-color: #e0e0e0;
            --success-green: #10b981;
        }

        body { font-family: Arial, sans-serif; background-color: var(--bg-main); color: var(--text-dark); margin: 0; padding: 20px; }
        .container { max-width: 1000px; margin: auto; }
        
        .header-mesero { 
            background: var(--card-bg); padding: 20px 25px; border-radius: 8px; 
            display: flex; justify-content: space-between; align-items: center; 
            border-bottom: 3px solid var(--accent-orange); border: 1px solid var(--border-color);
            margin-bottom: 25px;
        }
        .header-mesero h2 { margin: 0; color: var(--accent-orange); }
        .nav-links { display: flex; gap: 15px; align-items: center; }
        .nav-btn { background: #f1f3f5; color: var(--text-dark); padding: 8px 14px; border-radius: 6px; text-decoration: none; font-size: 13px; font-weight: bold; border: 1px solid #ccc; }
        .nav-btn:hover { background: var(--accent-orange); color: white; border-color: var(--accent-orange); }
        .back-btn { color: var(--text-gray); text-decoration: none; font-weight: bold; font-size: 13px; }
        .back-btn:hover { color: var(--accent-orange); }

        /* Panel de Alertas de Cocina (Pedidos Listos) */
        .alertas-cocina-box {
            background: #ecfdf5;
            border: 1px solid #a7f3d0;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 25px;
        }
        .alertas-cocina-box h3 {
            margin-top: 0;
            color: #065f46;
            font-size: 16px;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .listos-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 15px;
            margin-top: 15px;
        }
        .listo-card {
            background: white;
            border-left: 5px solid var(--success-green);
            padding: 12px 15px;
            border-radius: 6px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.02);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .listo-info .mesa-target { font-weight: bold; color: #064e3b; font-size: 15px; }
        .listo-info .detalle { font-size: 13px; color: #4b5563; margin-top: 3px; }
        .btn-entregado {
            background: var(--success-green);
            color: white;
            border: none;
            padding: 6px 12px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: bold;
            cursor: pointer;
        }
        .btn-entregado:hover { background: #059669; }

        /* Formulario del Menú e Interacción */
        .form-section {
            background: var(--card-bg);
            padding: 30px;
            border-radius: 8px;
            border: 1px solid var(--border-color);
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; margin-bottom: 8px; font-weight: bold; }
        .form-group select, .form-group textarea { width: 100%; padding: 12px; border: 1px solid #ccc; border-radius: 6px; box-sizing: border-box; font-size: 14px; }
        
        .menu-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 15px; margin-bottom: 20px; }
        .menu-item { background: #fffaf5; border: 1px solid #e0e0e0; padding: 15px; border-radius: 6px; display: flex; justify-content: space-between; align-items: center; }
        .menu-item input { transform: scale(1.2); cursor: pointer; }
        .price { color: var(--accent-orange); font-weight: bold; }

        .btn-submit { background: var(--accent-orange); color: white; border: none; padding: 12px 20px; font-size: 16px; border-radius: 6px; cursor: pointer; width: 100%; font-weight: bold; }
        .btn-submit:hover { background: #e65c00; }
        .msg { background: #e2f0d9; color: #385723; padding: 12px; border-radius: 6px; margin-bottom: 20px; font-weight: bold; text-align: center; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header-mesero">
            <div>
                <h2>📝 Estación de Mesero - SIGERE</h2>
                <span style="font-size: 13px; color: var(--text-gray);">Atendiendo: <strong><?php echo htmlspecialchars($nombreMesero); ?></strong></span>
            </div>
            <div class="nav-links">
                <a href="mesas.php" class="nav-btn">🗺️ Ver Mapa de Mesas</a>
                <a href="index.php" class="back-btn">← Volver</a>
            </div>
        </div>

        <?php if (!empty($mensaje)): ?>
            <div class="msg"><?php echo $mensaje; ?></div>
        <?php endif; ?>

        <!-- AVISOS DE COCINA: Pedidos Listos -->
        <div class="alertas-cocina-box">
            <h3>🔔 Alertas de Cocina: ¡Pedidos Listos para Servir!</h3>
            <p style="margin: 0; font-size: 13px; color: #047857;">La cocina ha marcado los siguientes platillos como listos para recoger:</p>
            
            <div class="listos-grid">
                <?php foreach ($pedidos_listos as $p_listo): ?>
                    <div class="listo-card">
                        <div class="listo-info">
                            <div class="mesa-target">📍 <?php echo htmlspecialchars($p_listo['mesa']); ?></div>
                            <div class="detalle"><?php echo htmlspecialchars($p_listo['platillos']); ?></div>
                            <div style="font-size: 11px; color: #6b7280; margin-top: 2px;">Comanda #<?php echo $p_listo['id']; ?> | <?php echo $p_listo['hora']; ?></div>
                        </div>
                        <button class="btn-entregado" onclick="alert('Pedido entregado a la mesa.'); location.reload();">Entregado</button>
                    </div>
                <?php endforeach; ?>
            </div>
        </div>

        <!-- MENÚ INTERACTIVO Y TOMA DE COMANDAS -->
        <div class="form-section">
            <h3 style="margin-top: 0; color: var(--text-dark);">Seleccionar Mesa y Menú Interactivo</h3>
            <form action="pedidos.php" method="POST">
                <input type="hidden" name="accion" value="crear_pedido">
                
                <div class="form-group">
                    <label for="mesa_id">Seleccionar Mesa:</label>
                    <select name="mesa_id" id="mesa_id" required>
                        <option value="">-- Elige una mesa del restaurante --</option>
                        <?php foreach ($mesas as $m): ?>
                            <option value="<?php echo $m['id']; ?>"><?php echo htmlspecialchars($m['nombre'] . ' (' . $m['zona'] . ') - Estado: ' . ucfirst($m['estado'])); ?></option>
                        <?php endforeach; ?>
                    </select>
                </div>

                <div class="form-group">
                    <label>Menú con Precios Actualizados:</label>
                    <div class="menu-grid">
                        <?php foreach ($menu as $platillo): ?>
                            <div class="menu-item">
                                <div>
                                    <strong><?php echo htmlspecialchars($platillo['nombre']); ?></strong><br>
                                    <span class="price">$<?php echo number_format($platillo['precio'], 2); ?> MXN</span>
                                </div>
                                <input type="checkbox" name="platillos[]" value="<?php echo $platillo['id']; ?>">
                            </div>
                        <?php endforeach; ?>
                    </div>
                </div>

                <div class="form-group">
                    <label for="notas">Notas Especiales / Especificaciones del Cliente (Sin cebolla, extra salsa, etc.):</label>
                    <textarea name="notas" id="notas" rows="3" placeholder="Anota aquí cualquier petición especial..."></textarea>
                </div>

                <button type="submit" class="btn-submit">Enviar Comanda a Cocina (Ocupar Mesa Automáticamente)</button>
            </form>
        </div>
    </div>
</body>
</html>