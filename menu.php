<?php
session_start();
require_once 'conexion.php';

if (!isset($_SESSION['usuario'])) {
    header("Location: login.php");
    exit;
}

$esAdmin = (isset($_SESSION['rol']) && $_SESSION['rol'] === 'Administrador');
$mensaje = '';

// Procesar acciones de Administrador (Agregar o Eliminar Platillo)
if ($esAdmin && $_SERVER['REQUEST_METHOD'] === 'POST') {
    $accion = $_POST['accion'] ?? '';

    if ($accion === 'crear') {
        $nombre = trim($_POST['nombre'] ?? '');
        $precio = floatval($_POST['precio'] ?? 0);

        if (!empty($nombre) && $precio > 0) {
            $stmt = $pdo->prepare("INSERT INTO platillos (nombre, precio) VALUES (:nombre, :precio)");
            $stmt->execute(['nombre' => $nombre, 'precio' => $precio]);
            $mensaje = "¡Platillo agregado al menú correctamente!";
        } else {
            $mensaje = "Por favor, ingresa un nombre y un precio válido.";
        }
    } elseif ($accion === 'eliminar') {
        $id_platillo = $_POST['id_platillo'] ?? 0;
        if ($id_platillo > 0) {
            $stmt = $pdo->prepare("DELETE FROM platillos WHERE id = :id");
            $stmt->execute(['id' => $id_platillo]);
            $mensaje = "Platillo eliminado del menú.";
        }
    }
}

// Obtener la lista actual de platillos desde la base de datos
try {
    $stmt = $pdo->query("SELECT * FROM platillos");
    $menu = $stmt->fetchAll(PDO::FETCH_ASSOC);
} catch (Exception $e) {
    $menu = [];
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Menú - SIGERE</title>
    <style>
        :root {
            --bg-main: #f8f9fa;
            --card-bg: #ffffff;
            --accent-orange: #ff6600;
            --text-dark: #222222;
            --text-gray: #666666;
            --border-color: #e0e0e0;
            --danger-red: #dc3545;
        }

        body { font-family: Arial, sans-serif; background-color: var(--bg-main); color: var(--text-dark); margin: 0; padding: 20px; }
        .container { max-width: 900px; margin: auto; }
        
        .header-menu { 
            background: var(--card-bg); padding: 20px 25px; border-radius: 8px; 
            display: flex; justify-content: space-between; align-items: center; 
            border-bottom: 3px solid var(--accent-orange); border: 1px solid var(--border-color);
            margin-bottom: 25px;
        }
        .header-menu h2 { margin: 0; color: var(--accent-orange); }
        .back-btn { color: var(--text-gray); text-decoration: none; font-weight: bold; }
        .back-btn:hover { color: var(--accent-orange); }

        .panel-box {
            background: var(--card-bg); padding: 25px; border-radius: 8px;
            border: 1px solid var(--border-color); box-shadow: 0 2px 6px rgba(0,0,0,0.02);
            margin-bottom: 25px;
        }
        .panel-box h3 { margin-top: 0; color: var(--text-dark); border-bottom: 1px solid #eee; padding-bottom: 10px; }

        .menu-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 15px; }
        .menu-card { background: #fffaf5; border: 1px solid var(--border-color); padding: 15px; border-radius: 6px; display: flex; justify-content: space-between; align-items: center; }
        .menu-info strong { font-size: 15px; color: var(--text-dark); }
        .price { color: var(--accent-orange); font-weight: bold; font-size: 14px; margin-top: 4px; display: block; }

        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px; }
        .form-group input { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }

        .btn-submit { background: var(--accent-orange); color: white; border: none; padding: 10px 15px; font-weight: bold; border-radius: 4px; cursor: pointer; }
        .btn-submit:hover { background: #e65c00; }
        
        .btn-delete { background: var(--danger-red); color: white; border: none; padding: 6px 10px; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: bold; }
        .btn-delete:hover { background: #bd2130; }

        .msg { background: #e2f0d9; color: #385723; padding: 10px; border-radius: 4px; margin-bottom: 15px; font-weight: bold; text-align: center; }
        .admin-badge { background: #e2e8f0; color: #334155; padding: 4px 8px; border-radius: 4px; font-size: 11px; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header-menu">
            <h2>🍽️ Menú Interactivo del Restaurante - SIGERE</h2>
            <a href="index.php" class="back-btn">← Volver al Panel</a>
        </div>

        <?php if (!empty($mensaje)): ?>
            <div class="msg"><?php echo $mensaje; ?></div>
        <?php endif; ?>

        <!-- Visualización del Menú para Todos -->
        <div class="panel-box">
            <h3>Platillos Actuales en el Menú</h3>
            <div class="menu-grid">
                <?php if (count($menu) > 0): ?>
                    <?php foreach ($menu as $platillo): ?>
                        <div class="menu-card">
                            <div class="menu-info">
                                <strong><?php echo htmlspecialchars($platillo['nombre']); ?></strong>
                                <span class="price">$<?php echo number_format($platillo['precio'], 2); ?> MXN</span>
                            </div>
                            <?php if ($esAdmin): ?>
                                <form action="menu.php" method="POST" onsubmit="return confirm('¿Estás seguro de eliminar este platillo?');">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id_platillo" value="<?php echo $platillo['id']; ?>">
                                    <button type="submit" class="btn-delete">Eliminar</button>
                                </form>
                            <?php endif; ?>
                        </div>
                    <?php endforeach; ?>
                <?php else: ?>
                    <p style="color: var(--text-gray); font-size: 14px;">No hay platillos registrados en la base de datos.</p>
                <?php endif; ?>
            </div>
        </div>

        <!-- Panel Exclusivo para Administrador: Agregar Platillos -->
        <?php if ($esAdmin): ?>
            <div class="panel-box" style="border-left: 4px solid var(--accent-orange);">
                <h3>➕ Agregar Nuevo Platillo (Panel de Administrador)</h3>
                <form action="menu.php" method="POST">
                    <input type="hidden" name="accion" value="crear">
                    
                    <div class="form-group">
                        <label for="nombre">Nombre del Platillo o Bebida:</label>
                        <input type="text" id="nombre" name="nombre" required placeholder="Ej. Enchiladas Suizas">
                    </div>

                    <div class="form-group">
                        <label for="precio">Precio ($ MXN):</label>
                        <input type="number" step="0.01" id="precio" name="precio" required placeholder="110.00">
                    </div>

                    <button type="submit" class="btn-submit">Guardar Platillo en el Menú</button>
                </form>
            </div>
        <?php else: ?>
            <div style="text-align: center; color: var(--text-gray); font-size: 13px;">
                * Estás conectado como empleado. La gestión y modificación del menú está restringida al rol de Administrador.
            </div>
        <?php endif; ?>
    </div>
</body>
</html>