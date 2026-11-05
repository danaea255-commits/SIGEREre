<?php
session_start();
require_once 'conexion.php';

$mensaje = '';
$ticketGenerado = null;

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['accion']) && $_POST['accion'] === 'cobrar') {
    $mesa_id = $_POST['mesa_id'] ?? 0;
    
    // 1. Registrar pago y ticket...

    // 2. AUTOMÁTICO: Liberar la mesa en la base de datos para que vuelva a estar verde
    $stmtLiberar = $pdo->prepare("UPDATE mesas SET estado = 'libre' WHERE id = :id");
    $stmtLiberar->execute(['id' => $mesa_id]);
}

    if (!empty($mesa) && $totalPagar > 0) {
        $cambio = $montoRecibido >= $totalPagar ? ($montoRecibido - $totalPagar) : 0;
        
        $ticketGenerado = [
            'folio' => rand(1000, 9999),
            'mesa' => $mesa,
            'metodo' => $metodoPago,
            'total' => $totalPagar,
            'recibido' => $montoRecibido,
            'cambio' => $cambio,
            'fecha' => date('d/m/Y H:i:s')
        ];
        $mensaje = "¡Cobro registrado con éxito para la Mesa $mesa!";
    } else {
        $mensaje = "Error al procesar el pago. Verifique los montos.";
    }


// Lista de cuentas pendientes de cobro (simuladas desde las mesas activas)
$cuentas_pendientes = [
    ['id_mesa' => 'Mesa VIP 2', 'cliente' => 'Roberto Gómez', 'detalle' => '2x Bistec a la Mexicana, 1x Flan Napolitano', 'total' => 290.00],
    ['id_mesa' => 'Mesa G-3', 'cliente' => 'Carlos Mendoza', 'detalle' => '2x Tacos al Pastor, 1x Sopa de Res', 'total' => 245.00]
];
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Caja y Cobros - SIGERE</title>
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
        
        .header-caja { 
            background: var(--card-bg); padding: 20px 25px; border-radius: 8px; 
            display: flex; justify-content: space-between; align-items: center; 
            border-bottom: 3px solid var(--accent-orange); border: 1px solid var(--border-color);
            margin-bottom: 25px;
        }
        .header-caja h2 { margin: 0; color: var(--accent-orange); }
        .back-btn { color: var(--text-gray); text-decoration: none; font-weight: bold; }
        .back-btn:hover { color: var(--accent-orange); }

        .grid-caja { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        @media(max-width: 768px) { .grid-caja { grid-template-columns: 1fr; } }

        .panel-box {
            background: var(--card-bg); padding: 25px; border-radius: 8px;
            border: 1px solid var(--border-color); box-shadow: 0 2px 6px rgba(0,0,0,0.02);
        }
        .panel-box h3 { margin-top: 0; color: var(--text-dark); border-bottom: 1px solid #eee; padding-bottom: 10px; }

        .cuenta-card {
            background: #fafafa; border: 1px solid #e5e7eb; padding: 15px; border-radius: 6px; margin-bottom: 15px;
            display: flex; justify-content: space-between; align-items: center;
        }
        .cuenta-info .mesa { font-weight: bold; font-size: 15px; color: var(--text-dark); }
        .cuenta-info .cliente { font-size: 13px; color: var(--text-gray); margin-top: 2px; }
        .cuenta-info .monto { font-size: 16px; font-weight: bold; color: var(--accent-orange); margin-top: 5px; }
        
        .btn-cobrar {
            background: var(--success-green); color: white; border: none; padding: 8px 14px;
            border-radius: 4px; font-weight: bold; cursor: pointer; font-size: 13px;
        }
        .btn-cobrar:hover { background: #059669; }

        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px; }
        .form-group input, .form-group select { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        
        .btn-submit { background: var(--accent-orange); color: white; border: none; padding: 12px; width: 100%; font-weight: bold; border-radius: 4px; cursor: pointer; font-size: 15px; }
        .btn-submit:hover { background: #e65c00; }

        /* Estilo del Ticket Generado */
        .ticket-box { background: #fffbeb; border: 1px dashed #f59e0b; padding: 20px; border-radius: 6px; margin-top: 20px; }
        .ticket-box h4 { margin-top: 0; color: #b45309; text-align: center; }
        .ticket-line { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 6px; }
        .ticket-total { font-size: 16px; font-weight: bold; border-top: 1px solid #fcd34d; padding-top: 8px; margin-top: 8px; }
        .msg { background: #e2f0d9; color: #385723; padding: 10px; border-radius: 4px; margin-bottom: 15px; font-weight: bold; text-align: center; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header-caja">
            <h2>💵 Estación de Caja y Cobros - SIGERE</h2>
            <a href="index.php" class="back-btn">← Volver al Panel</a>
        </div>

        <?php if (!empty($mensaje)): ?>
            <div class="msg"><?php echo $mensaje; ?></div>
        <?php endif; ?>

        <div class="grid-caja">
            <!-- Izquierda: Cuentas Pendientes por Cobrar -->
            <div class="panel-box">
                <h3>📋 Cuentas Pendientes de Mesas</h3>
                <p style="font-size: 13px; color: var(--text-gray);">Selecciona una cuenta para cargarla al sistema de cobro:</p>

                <?php foreach ($cuentas_pendientes as $cuenta): ?>
                    <div class="cuenta-card">
                        <div class="cuenta-info">
                            <div class="mesa">📍 <?php echo htmlspecialchars($cuenta['id_mesa']); ?> (<?php echo htmlspecialchars($cuenta['cliente']); ?>)</div>
                            <div class="cliente"><?php echo htmlspecialchars($cuenta['detalle']); ?></div>
                            <div class="monto">$<?php echo number_format($cuenta['total'], 2); ?> MXN</div>
                        </div>
                        <button class="btn-cobrar" onclick="cargarCuenta('<?php echo $cuenta['id_mesa']; ?>', <?php echo $cuenta['total']; ?>)">Cobrar</button>
                    </div>
                <?php endforeach; ?>
            </div>

            <!-- Derecha: Procesar Pago y Generar Ticket -->
            <div class="panel-box">
                <h3>💳 Procesar Pago y Ticket</h3>
                <form action="caja.php" method="POST">
                    <input type="hidden" name="accion" value="cobrar">

                    <div class="form-group">
                        <label for="mesa">Mesa / Cuenta:</label>
                        <input type="text" id="mesa" name="mesa" required placeholder="Ej. Mesa VIP 2" readonly>
                    </div>

                    <div class="form-group">
                        <label for="total_pagar">Total a Pagar ($ MXN):</label>
                        <input type="number" step="0.01" id="total_pagar" name="total_pagar" required placeholder="0.00" readonly>
                    </div>

                    <div class="form-group">
                        <label for="metodo_pago">Método de Pago:</label>
                        <select name="metodo_pago" id="metodo_pago" required>
                            <option value="Efectivo">Efectivo</option>
                            <option value="Tarjeta de Crédito/Débito">Tarjeta de Crédito / Débito</option>
                            <option value="Transferencia / Spei">Transferencia / SPEI</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="monto_recibido">Monto Recibido del Cliente ($ MXN):</label>
                        <input type="number" step="0.01" id="monto_recibido" name="monto_recibido" required placeholder="0.00">
                    </div>

                    <button type="submit" class="btn-submit">Registrar Cobro y Imprimir Ticket</button>
                </form>

                <?php if ($ticketGenerado): ?>
                    <div class="ticket-box">
                        <h4>🎫 TICKET DE VENTA #<?php echo $ticketGenerado['folio']; ?></h4>
                        <div class="ticket-line"><span>Mesa:</span> <strong><?php echo $ticketGenerado['mesa']; ?></strong></div>
                        <div class="ticket-line"><span>Fecha:</span> <span><?php echo $ticketGenerado['fecha']; ?></span></div>
                        <div class="ticket-line"><span>Método:</span> <span><?php echo $ticketGenerado['metodo']; ?></span></div>
                        <div class="ticket-line ticket-total"><span>Total Cobrado:</span> <span>$<?php echo number_format($ticketGenerado['total'], 2); ?></span></div>
                        <div class="ticket-line"><span>Efectivo Recibido:</span> <span>$<?php echo number_format($ticketGenerado['recibido'], 2); ?></span></div>
                        <div class="ticket-line" style="color: #059669; font-weight: bold;"><span>Cambio / Vuelto:</span> <span>$<?php echo number_format($ticketGenerado['cambio'], 2); ?></span></div>
                    </div>
                <?php endif; ?>
            </div>
        </div>
    </div>

    <script>
        function cargarCuenta(mesa, total) {
            document.getElementById('mesa').value = mesa;
            document.getElementById('total_pagar').value = total.toFixed(2);
            document.getElementById('monto_recibido').focus();
        }
    </script>
</body>
</html>