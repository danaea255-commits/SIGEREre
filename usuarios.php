<?php
session_start();
require_once 'conexion.php';

// Validar que sea administrador
if (!isset($_SESSION['usuario']) || $_SESSION['rol'] !== 'Administrador') {
    header("Location: login.php");
    exit;
}

$mensaje = '';

// Procesar el formulario para agregar un nuevo usuario
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['accion']) && $_POST['accion'] === 'crear') {
    $nombre = trim($_POST['nombre'] ?? '');
    $correo = trim($_POST['correo'] ?? '');
    $clave = trim($_POST['clave'] ?? '');
    $rol = trim($_POST['rol'] ?? '');

    if (!empty($nombre) && !empty($correo) && !empty($clave) && !empty($rol)) {
        try {
            $stmt = $pdo->prepare("INSERT INTO usuario (nombre, correo, clave, rol) VALUES (:nombre, :correo, :clave, :rol)");
            $stmt->execute(['nombre' => $nombre, 'correo' => $correo, 'clave' => $clave, 'rol' => $rol]);
            $mensaje = "¡Personal registrado correctamente!";
        } catch (PDOException $e) {
            $mensaje = "Error: El correo ya está registrado.";
        }
    } else {
        $mensaje = "Por favor, completa todos los campos.";
    }
}

// Procesar eliminación de usuario
if (isset($_GET['eliminar'])) {
    $id_eliminar = $_GET['eliminar'];
    $stmt = $pdo->prepare("DELETE FROM usuario WHERE id = :id");
    $stmt->execute(['id' => $id_eliminar]);
    header("Location: usuarios.php");
    exit;
}

// Obtener lista de usuarios
$stmt = $pdo->query("SELECT * FROM usuario");
$usuarios = $stmt->fetchAll(PDO::FETCH_ASSOC);
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Control de Personal - SIGERE</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f8f9fa; color: #222; margin: 0; padding: 20px; }
        .container { max-width: 1000px; margin: auto; background: #fff; padding: 30px; border-radius: 8px; border: 1px solid #e0e0e0; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        h2 { color: #ff6600; margin-top: 0; }
        .back-btn { display: inline-block; margin-bottom: 20px; color: #666; text-decoration: none; font-weight: bold; }
        .back-btn:hover { color: #ff6600; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 12px; border-bottom: 1px solid #ddd; text-align: left; }
        th { background: #f1f3f5; color: #333; }
        .form-container { background: #fdfdfd; padding: 20px; border: 1px solid #e0e0e0; border-radius: 6px; margin-top: 25px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px; }
        .form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        .btn { background: #ff6600; color: white; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; font-weight: bold; }
        .btn:hover { background: #e65c00; }
        .btn-delete { background: #d9534f; color: white; padding: 6px 10px; text-decoration: none; border-radius: 4px; font-size: 13px; }
        .btn-delete:hover { background: #c9302c; }
        .msg { background: #e2f0d9; color: #385723; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
    </style>
</head>
<body>
    <div class="container">
        <a href="index.php" class="back-btn">← Volver al Panel Principal</a>
        <h2>👥 Control de Personal y Credenciales</h2>
        <p>Añade, supervisa o da de baja al personal autorizado del sistema.</p>

        <?php if (!empty($mensaje)): ?>
            <div class="msg"><?php echo $mensaje; ?></div>
        <?php endif; ?>

        <h3>Personal Registrado</h3>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Nombre</th>
                    <th>Correo</th>
                    <th>Rol</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                <?php foreach ($usuarios as $u): ?>
                <tr>
                    <td><?php echo $u['id']; ?></td>
                    <td><?php echo htmlspecialchars($u['nombre']); ?></td>
                    <td><?php echo htmlspecialchars($u['correo']); ?></td>
                    <td><strong><?php echo htmlspecialchars($u['rol']); ?></strong></td>
                    <td>
                        <?php if ($u['correo'] !== 'admin@sigere.com'): ?>
                            <a href="usuarios.php?eliminar=<?php echo $u['id']; ?>" class="btn-delete" onclick="return confirm('¿Estás seguro de eliminar a este usuario?');">Eliminar</a>
                        <?php else: ?>
                            <span style="color: #888; font-size: 12px;">Protegido</span>
                        <?php endif; ?>
                    </td>
                </tr>
                <?php endforeach; ?>
            </tbody>
        </table>

        <div class="form-container">
            <h3>Registrar Nuevo Personal</h3>
            <form action="usuarios.php" method="POST">
                <input type="hidden" name="accion" value="crear">
                <div class="form-group">
                    <label>Nombre Completo</label>
                    <input type="text" name="nombre" required placeholder="Ej. Carlos Pérez">
                </div>
                <div class="form-group">
                    <label>Correo Electrónico (Usuario)</label>
                    <input type="email" name="correo" required placeholder="carlos@sigere.com">
                </div>
                <div class="form-group">
                    <label>Contraseña Temporal</label>
                    <input type="password" name="clave" required placeholder="••••••••">
                </div>
                <div class="form-group">
                    <label>Rol en el Sistema</label>
                    <select name="rol" required>
                        <option value="Mesero">Mesero</option>
                        <option value="Administrador">Administrador</option>
                    </select>
                </div>
                <button type="submit" class="btn">Guardar Personal</button>
            </form>
        </div>
    </div>
</body>
</html>