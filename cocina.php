<?php
session_start();
require_once 'conexion.php';

$mensaje = '';

// Procesar cambio de estado de la comanda desde la pantalla de cocina
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['accion'])) {
    $id_pedido = $_POST['id_pedido'] ?? '';
    $nuevo_estado = $_POST['nuevo_estado'] ?? '';

    if (!empty($id_pedido) && !empty($nuevo_estado)) {
        // Aquí se actualiza el estado en la base de datos (o simulación)
        $mensaje = "Pedido #$id_pedido actualizado a: $nuevo_estado";
    }
}

// Lista de comandas de ejemplo en cocina (conectada a los datos de las comandas)
$pedidos_cocina = [
    [
        'id' => 101,
        'mesa' => 'Mesa G-3 (General)',
        'hora' => '18:42 PM',
        'estado' => 'Pendiente',
        'platillos' => [
            '2x Tacos al Pastor',
            '1x Sopa de Res'
        ],
        'notas' => 'Tacos SIN cebolla, sopa muy caliente.'
    ],
    [
        'id' => 102,
        'mesa' => 'Mesa VIP 2',
        'hora' => '18:35 PM',
        'estado' => 'En Preparación',
        'platillos' => [
            '2x Bistec a la Mexicana',
            '1x Flan Napolitano'
        ],
        'notas' => 'Servir el postre al final.'
    ],
    [
        'id' => 103,
        'mesa' => 'Terraza 1',
        'hora' => '18:20 PM',
        'estado' => 'Listo',
        'platillos' => [
            '1x Agua de Horchata 1L'
        ],
        'notas' => 'Con bastante hielo.'
    ]
];
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pantalla de Cocina - SIGERE</title>
    <style>
        :root {
            --bg-dark: #1e293b;
            --card-bg: #ffffff;
            --accent-orange: #ff6600;
            --status-pendiente: #ef4444;
            --status-preparacion: #f59e0b;
            --status-listo: #10b981;
            --text-dark: #0f172a;
        }

        body { 
            font-family: 'Segoe UI', Arial, sans-serif; 
            background-color: var(--bg-dark); 
            color: #f8fafc;
            margin: 0; 
            padding: 20px; 
        }

        .header-cocina { 
            display: flex; 
            justify-content: space-between; 
            align-items: center; 
            background: #0f172a; 
            padding: 15px 25px; 
            border-radius: 8px; 
            border-bottom: 3px solid var(--accent-orange);
            margin-bottom: 25px;
        }

        .header-cocina h1 { 
            margin: 0; 
            font-size: 22px; 
            color: var(--accent-orange);
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .btn-volver { 
            background: #334155; 
            color: white; 
            text-decoration: none; 
            padding: 8px 16px; 
            border-radius: 6px; 
            font-size: 13px; 
            font-weight: bold;
            transition: background 0.2s;
        }

        .btn-volver:hover { 
            background: var(--accent-orange); 
        }

        .orders-grid { 
            display: grid; 
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); 
            gap: 20px; 
        }

        .order-card { 
            background: var(--card-bg); 
            color: var(--text-dark); 
            border-radius: 8px; 
            box-shadow: 0 4px 12px rgba(0,0,0,0.2); 
            overflow: hidden; 
            display: flex; 
            flex-direction: column; 
            justify-content: space-between;
        }

        .order-header { 
            padding: 12px 15px; 
            background: #f1f5f9; 
            display: flex; 
            justify-content: space-between; 
            align-items: center; 
            border-bottom: 2px solid #e2e8f0;
        }

        .order-header .mesa { 
            font-weight: bold; 
            font-size: 16px; 
            color: var(--text-dark);
        }

        .order-header .hora { 
            font-size: 12px; 
            color: #64748b; 
        }

        .order-body { 
            padding: 15px; 
            flex-grow: 1;
        }

        .items-list { 
            list-style: none; 
            padding: 0; 
            margin: 0 0 15px 0; 
        }

        .items-list li { 
            font-size: 15px; 
            font-weight: 600; 
            padding: 6px 0; 
            border-bottom: 1px dashed #cbd5e1; 
        }

        .notes-box { 
            background: #fef2f2; 
            border-left: 4px solid var(--status-pendiente); 
            padding: 10px; 
            border-radius: 4px; 
            font-size: 13px; 
            color: #991b1b; 
            font-weight: bold; 
            margin-top: 10px; 
        }

        .order-footer { 
            padding: 12px 15px; 
            background: #fafafa; 
            border-top: 1px solid #e2e8f0; 
            display: flex; 
            gap: 8px; 
        }

        .btn-status { 
            flex: 1; 
            padding: 10px; 
            border: none; 
            border-radius: 6px; 
            font-weight: bold; 
            cursor: pointer; 
            color: white; 
            font-size: 13px; 
            transition: opacity 0.2s;
        }

        .btn-status:hover { 
            opacity: 0.9; 
        }

        .btn-preparar { background: var(--status-preparacion); }
        .btn-listo { background: var(--status-listo); }

        .badge-status { 
            padding: 4px 10px; 
            border-radius: 12px; 
            font-size: 11px; 
            font-weight: bold; 
            color: white; 
        }

        .badge-pendiente { background: var(--status-pendiente); }
        .badge-preparacion { background: var(--status-preparacion); }
        .badge-listo { background: var(--status-listo); }

        .msg-bar { 
            background: #10b981; 
            color: white; 
            padding: 10px; 
            border-radius: 6px; 
            margin-bottom: 20px; 
            text-align: center; 
            font-weight: bold; 
        }
    </style>
</head>
<body>

    <div class="header-cocina">
        <h1>🔥 Estación de Cocina (KDS) - SIGERE</h1>
        <div>
            <span style="margin-right: 15px; font-size: 14px; color: #94a3b8;">Monitoreo en tiempo real</span>
            <a href="index.php" class="btn-volver">← Volver al Panel</a>
        </div>
    </div>

    <?php if (!empty($mensaje)): ?>
        <div class="msg-bar"><?php echo $mensaje; ?></div>
    <?php endif; ?>

    <div class="orders-grid">
        <?php foreach ($pedidos_cocina as $pedido): ?>
            <div class="order-card">
                <div>
                    <div class="order-header">
                        <div>
                            <div class="mesa"><?php echo htmlspecialchars($pedido['mesa']); ?></div>
                            <div class="hora">Pedido #<?php echo $pedido['id']; ?> | <?php echo $pedido['hora']; ?></div>
                        </div>
                        <span class="badge-status 
                            <?php 
                                echo $pedido['estado'] === 'Pendiente' ? 'badge-pendiente' : 
                                    ($pedido['estado'] === 'En Preparación' ? 'badge-preparacion' : 'badge-listo'); 
                            ?>">
                            <?php echo $pedido['estado']; ?>
                        </span>
                    </div>

                    <div class="order-body">
                        <ul class="items-list">
                            <?php foreach ($pedido['platillos'] as $platillo): ?>
                                <li><?php echo htmlspecialchars($platillo); ?></li>
                            <?php endforeach; ?>
                        </ul>

                        <?php if (!empty($pedido['notas'])): ?>
                            <div class="notes-box">
                                ⚠️ Nota: <?php echo htmlspecialchars($pedido['notas']); ?>
                            </div>
                        <?php endif; ?>
                    </div>
                </div>

                <div class="order-footer">
                    <form action="cocina.php" method="POST" style="width: 100%; display: flex; gap: 8px;">
                        <input type="hidden" name="id_pedido" value="<?php echo $pedido['id']; ?>">
                        <input type="hidden" name="accion" value="cambiar_estado">
                        
                        <?php if ($pedido['estado'] === 'Pendiente'): ?>
                            <button type="submit" name="nuevo_estado" value="En Preparación" class="btn-status btn-preparar">👨‍🍳 En Preparación</button>
                        <?php elseif ($pedido['estado'] === 'En Preparación'): ?>
                            <button type="submit" name="nuevo_estado" value="Listo" class="btn-status btn-listo">✅ Marcar Listo</button>
                        <?php else: ?>
                            <div style="text-align: center; width: 100%; font-size: 13px; color: #10b981; font-weight: bold;">
                                Comanda Servida / Lista
                            </div>
                        <?php endif; ?>
                    </form>
                </div>
            </div>
        <?php endforeach; ?>
    </div>

</body>
</html>