<?php
session_start();

if (!isset($_SESSION['usuario'])) {
    header("Location: login.php");
    exit;
}

$nombreUsuario = $_SESSION['usuario'];
$rolUsuario = $_SESSION['rol'] ?? 'Administrador';
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel de Administración - SIGERE</title>
    <style>
        :root {
            --bg-main: #f4f6f9;
            --card-bg: #ffffff;
            --accent-orange: #ff6600;
            --text-dark: #2c3e50;
            --text-gray: #7f8c8d;
            --border-color: #e2e8f0;
        }

        body { 
            font-family: 'Segoe UI', Arial, sans-serif; 
            background-color: var(--bg-main); 
            color: var(--text-dark);
            margin: 0; 
            padding: 20px; 
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
        }

        /* Header Estilo SIGERE */
        .header { 
            background: var(--card-bg); 
            padding: 15px 25px; 
            border-radius: 10px; 
            display: flex; 
            justify-content: space-between; 
            align-items: center; 
            box-shadow: 0 2px 10px rgba(0,0,0,0.03);
            border: 1px solid var(--border-color);
            margin-bottom: 25px;
        }

        .brand {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .brand-icon {
            background: var(--accent-orange);
            color: white;
            padding: 10px;
            border-radius: 8px;
            font-size: 20px;
        }

        .brand h1 {
            margin: 0;
            font-size: 20px;
            color: var(--text-dark);
        }

        .brand span {
            font-size: 12px;
            color: var(--text-gray);
        }

        .role-indicator {
            display: flex;
            align-items: center;
            gap: 10px;
            background: #fffaf5;
            padding: 8px 15px;
            border-radius: 20px;
            border: 1px solid #ffe0cc;
            font-size: 13px;
        }

        .role-badge {
            background: var(--accent-orange);
            color: white;
            padding: 4px 10px;
            border-radius: 12px;
            font-weight: bold;
        }

        .btn-logout { 
            color: #e74c3c; 
            text-decoration: none; 
            font-weight: bold;
            font-size: 13px;
            margin-left: 15px;
            padding: 6px 12px;
            border: 1px solid #fadbd8;
            border-radius: 6px;
            transition: all 0.2s;
        }
        .btn-logout:hover { 
            background: #e74c3c; 
            color: #fff; 
        }

        /* Título del Panel */
        .dashboard-title {
            margin-bottom: 20px;
        }
        .dashboard-title h2 {
            margin: 0 0 5px 0;
            font-size: 22px;
            color: var(--text-dark);
        }
        .dashboard-title p {
            margin: 0;
            color: var(--text-gray);
            font-size: 14px;
        }

        /* Métricas Grid */
        .metrics-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }

        .metric-card {
            background: var(--card-bg);
            padding: 20px;
            border-radius: 10px;
            border: 1px solid var(--border-color);
            box-shadow: 0 2px 6px rgba(0,0,0,0.02);
            position: relative;
        }

        .metric-card .title {
            font-size: 11px;
            font-weight: bold;
            color: var(--text-gray);
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        .metric-card .value {
            font-size: 24px;
            font-weight: bold;
            color: var(--text-dark);
        }

        /* Secciones de Contenido */
        .sections-grid {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 20px;
        }

        @media(max-width: 768px) {
            .sections-grid {
                grid-template-columns: 1fr;
            }
        }

        .panel-box {
            background: var(--card-bg);
            padding: 20px;
            border-radius: 10px;
            border: 1px solid var(--border-color);
            box-shadow: 0 2px 6px rgba(0,0,0,0.02);
        }

        .panel-box h3 {
            margin-top: 0;
            font-size: 16px;
            color: var(--text-dark);
            border-bottom: 1px solid var(--border-color);
            padding-bottom: 10px;
            margin-bottom: 15px;
        }

        /* Acceso a Módulos */
        .modules-list {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 15px;
        }

        .module-item {
            background: #fafbfc;
            border: 1px solid var(--border-color);
            padding: 15px;
            border-radius: 8px;
            text-decoration: none;
            color: var(--text-dark);
            display: flex;
            flex-direction: column;
            gap: 5px;
            transition: all 0.2s;
        }

        .module-item:hover {
            border-color: var(--accent-orange);
            background: #fffaf5;
            transform: translateY(-2px);
        }

        .module-item strong {
            font-size: 14px;
            color: var(--accent-orange);
        }

        .module-item span {
            font-size: 12px;
            color: var(--text-gray);
        }

        .stock-alert {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 10px 0;
            border-bottom: 1px solid #f1f3f5;
            font-size: 13px;
        }

        .stock-badge {
            background: #fff5f5;
            color: #e74c3c;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header Superior -->
        <div class="header">
            <div class="brand">
                <div class="brand-icon">🍽️</div>
                <div>
                    <h1>SIGERE</h1>
                    <span>Sistema de Control Operativo</span>
                </div>
            </div>
            
            <div style="display: flex; align-items: center;">
                <div class="role-indicator">
                    <span>ROL ACTUAL:</span>
                    <span class="role-badge"><?php echo htmlspecialchars($rolUsuario); ?></span>
                </div>
                <a href="login.php" class="btn-logout">Cerrar Sesión</a>
            </div>
        </div>

        <!-- Título de la Sección -->
        <div class="dashboard-title">
            <h2>Panel de Administración General</h2>
            <p>Métricas operativas del restaurante e inventario en tiempo real (Bienvenido, <strong><?php echo htmlspecialchars($nombreUsuario); ?></strong>).</p>
        </div>

        <!-- Tarjetas de Métricas -->
        <div class="metrics-grid">
            <div class="metric-card">
                <div class="title">VENTAS DE HOY</div>
                <div class="value">$12,480.00</div>
            </div>
            <div class="metric-card">
                <div class="title">ÓRDENES SERVIDAS</div>
                <div class="value">48</div>
            </div>
            <div class="metric-card">
                <div class="title">TIEMPO PROM. COCINA</div>
                <div class="value">14 min</div>
            </div>
            <div class="metric-card">
                <div class="title">PERSONAL ACTIVO</div>
                <div class="value">6 Personas</div>
            </div>
        </div>

        <!-- Paneles Inferiores -->
        <div class="sections-grid">
            <!-- Ajustes del Sistema y Módulos -->
            <div class="panel-box">
                <h3>Ajustes y Módulos del Sistema</h3>
                <div class="modules-list">
                    <a href="menu.php" class="module-item">
                        <strong>🍽️ Gestionar Menú</strong>
                        <span>Precios y platillos</span>
                    </a>
                    <a href="mesas.php" class="module-item">
                        <strong>🪑 Mapa de Mesas</strong>
                        <span>Zonas VIP, General y Terraza</span>
                    </a>
                    <a href="usuarios.php" class="module-item">
                        <strong>👥 Control de Personal</strong>
                        <span>Altas, bajas y credenciales</span>
                    </a>
                    <a href="pedidos.php" class="module-item">
                        <strong>📝 Órdenes y Comandas</strong>
                        <span>Captura con notas especiales</span>
                    </a>
                    <a href="cocina.php" class="module-item">
                        <strong>🔥 Pantalla de Cocina</strong>
                        <span>Estación de preparación</span>
                    </a>
                    <a href="caja.php" class="module-item">
                        <strong>💵 Caja y Cobros</strong>
                        <span>Tickets y pagos</span>
                    </a>
                </div>
            </div>

            <!-- Alertas de Stock Bajo -->
            <div class="panel-box">
                <h3>⚠️ Alertas de Stock Bajo</h3>
                <div class="stock-alert">
                    <span>Carne para Hamburguesa (Sirloin)</span>
                    <span class="stock-badge">Quedan 2.5 kg</span>
                </div>
                <div class="stock-alert">
                    <span>Queso Mozzarella</span>
                    <span class="stock-badge">Quedan 1.2 kg</span>
                </div>
                <div class="stock-alert" style="border-bottom: none;">
                    <span>Tomates / Verduras</span>
                    <span class="stock-badge">Quedan 3.0 kg</span>
                </div>
            </div>
        </div>
    </div>
</body>
</html>