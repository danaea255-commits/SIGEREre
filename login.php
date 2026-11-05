<?php
session_start();
require_once 'conexion.php';

$error = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $correo = trim($_POST['correo'] ?? '');
    $clave = trim($_POST['clave'] ?? '');

    if (!empty($correo) && !empty($clave)) {
        $stmt = $pdo->prepare("SELECT * FROM usuario WHERE correo = :correo");
        $stmt->execute(['correo' => $correo]);
        $usuario = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($usuario && $clave === $usuario['clave']) {
            $_SESSION['usuario'] = $usuario['nombre'];
            $_SESSION['rol'] = $usuario['rol'];
            
            // REDIRECCIÓN CORREGIDA SEGÚN EL ROL
            if ($usuario['rol'] === 'Administrador') {
                header("Location: index.php");
            } else {
                header("Location: mesas.php"); // Redirige al mesero directo a su vista exclusiva
            }
            exit;
        } else {
            $error = "Correo o contraseña incorrectos.";
        }
    } else {
        $error = "Por favor, completa todos los campos.";
    }
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Acceso - SIGERE</title>
    <style>
        body { 
            font-family: Arial, sans-serif; 
            background-color: #f8f9fa; 
            color: #333; 
            display: flex; 
            justify-content: center; 
            align-items: center; 
            height: 100vh; 
            margin: 0; 
        }
        .login-container { 
            background: #ffffff; 
            padding: 40px; 
            border-radius: 10px; 
            box-shadow: 0 4px 15px rgba(0,0,0,0.08); 
            width: 100%; 
            max-width: 420px; 
            border: 1px solid #e0e0e0; 
        }
        .login-container h2 { 
            text-align: center; 
            margin-bottom: 5px; 
            color: #ff6600; 
        }
        .login-container p.subtitle { 
            text-align: center; 
            color: #666; 
            font-size: 14px; 
            margin-bottom: 25px; 
        }
        .form-group { 
            margin-bottom: 20px; 
        }
        .form-group label { 
            display: block; 
            margin-bottom: 8px; 
            color: #444; 
            font-weight: bold; 
            font-size: 14px; 
        }
        .form-group input { 
            width: 100%; 
            padding: 12px; 
            border: 1px solid #ccc; 
            border-radius: 6px; 
            box-sizing: border-box; 
            font-size: 14px; 
        }
        .form-group input:focus { 
            border-color: #ff6600; 
            outline: none; 
        }
        .btn-submit { 
            width: 100%; 
            padding: 12px; 
            background: #ff6600; 
            border: none; 
            color: white; 
            border-radius: 6px; 
            font-size: 16px; 
            font-weight: bold; 
            cursor: pointer; 
            transition: background 0.2s; 
        }
        .btn-submit:hover { 
            background: #e65c00; 
        }
        .error-msg { 
            background: #f8d7da; 
            color: #721c24; 
            padding: 10px; 
            border-radius: 6px; 
            text-align: center; 
            margin-bottom: 20px; 
            font-size: 14px; 
        }
        .stations-section { 
            margin-top: 30px; 
            border-top: 1px solid #eee; 
            padding-top: 20px; 
            text-align: center; 
        }
        .stations-section p { 
            font-size: 13px; 
            color: #666; 
            margin-bottom: 15px; 
        }
        .btn-station { 
            display: inline-block; 
            padding: 10px 15px; 
            margin: 5px; 
            background: #333; 
            color: white; 
            text-decoration: none; 
            border-radius: 5px; 
            font-size: 13px; 
            font-weight: bold; 
            transition: background 0.2s; 
        }
        .btn-station:hover { 
            background: #ff6600; 
            color: #fff; 
        }
    </style>
</head>
<body>
    <div class="login-container">
        <h2>SIGERE</h2>
        <p class="subtitle">Sistema de Gestión de Restaurante</p>

        <?php if (!empty($error)): ?>
            <div class="error-msg"><?php echo $error; ?></div>
        <?php endif; ?>

        <!-- Formulario con anti-autorrelleno -->
        <form action="login.php" method="POST" autocomplete="off">
            <input type="text" style="display:none" name="fakeusernameremembered">
            <input type="password" style="display:none" name="fakepasswordremembered">

            <div class="form-group">
                <label for="correo">Correo Electrónico</label>
                <input type="email" id="correo" name="correo" placeholder="admin@sigere.com" autocomplete="off" readonly onfocus="this.removeAttribute('readonly');" required>
            </div>
            <div class="form-group">
                <label for="clave">Contraseña</label>
                <input type="password" id="clave" name="clave" placeholder="••••••••" autocomplete="new-password" readonly onfocus="this.removeAttribute('readonly');" required>
            </div>
            <button type="submit" class="btn-submit">Iniciar Sesión</button>
        </form>

        <div class="stations-section">
            <p>Acceso directo a estaciones:</p>
            <a href="cocina.php" class="btn-station">🔥 Pantalla Cocina</a>
            <a href="caja.php" class="btn-station">💵 Caja y Cobros</a>
        </div>
    </div>
</body>
</html>