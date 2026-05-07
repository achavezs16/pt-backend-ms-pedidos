-- Base de datos para MS-Notificaciones - PymeTrack
-- Sistema de comunicaciones para pymes chilenas (ropa, electrónicos, belleza)

CREATE DATABASE IF NOT EXISTS ms_notificaciones CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ms_notificaciones;

-- Tabla de Plantillas de Notificación
CREATE TABLE plantilla_notificacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    tipo_evento ENUM('PEDIDO_CONFIRMADO', 'DESPACHO_INICIADO', 'ENTREGA_EN_CAMINO', 'ENTREGA_REALIZADA', 'ENTREGA_FALLIDA', 'AVISO_ENTREGA_PROXIMA') NOT NULL,
    canal_notificacion ENUM('CORREO_ELECTRONICO', 'WHATSAPP', 'MENSAJE_TEXTO') NOT NULL,
    asunto VARCHAR(200),
    contenido_formato_html TEXT,
    contenido_formato_texto TEXT,
    variables_personalizacion JSON, -- Ej: ["{{nombre_cliente}}", "{{estado_envio}}"]
    activa BOOLEAN DEFAULT TRUE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tipo_canal (tipo_evento, canal_notificacion),
    INDEX idx_activa (activa)
);

-- Tabla de Notificaciones
CREATE TABLE notificacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_envio BIGINT NOT NULL,
    id_pedido BIGINT NOT NULL,
    email_cliente VARCHAR(100) NOT NULL,
    telefono_cliente VARCHAR(20),
    tipo_evento ENUM('PEDIDO_CONFIRMADO', 'DESPACHO_INICIADO', 'ENTREGA_EN_CAMINO', 'ENTREGA_REALIZADA', 'ENTREGA_FALLIDA', 'AVISO_ENTREGA_PROXIMA') NOT NULL,
    canal_notificacion ENUM('CORREO_ELECTRONICO', 'WHATSAPP', 'MENSAJE_TEXTO') NOT NULL DEFAULT 'CORREO_ELECTRONICO',
    estado_proceso ENUM('PENDIENTE_ENVIO', 'EN_PROCESO', 'ENVIADO_EXITOSO', 'ERROR_ENVIO', 'PROGRAMADO_REINTENTO') DEFAULT 'PENDIENTE_ENVIO',
    reintentos_realizados INT DEFAULT 0,
    limite_maximo_reintentos INT DEFAULT 3,
    fecha_proximo_reintento TIMESTAMP NULL,
    fecha_envio_realizado TIMESTAMP NULL,
    respuesta_proveedor TEXT,
    descripcion_error TEXT,
    datos_contexto JSON, -- Datos para reemplazar en plantilla
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_envio_estado (id_envio, estado_proceso),
    INDEX idx_tipo_estado (tipo_evento, estado_proceso),
    INDEX idx_pendientes_reintentar (estado_proceso, fecha_proximo_reintento),
    INDEX idx_fecha_envio (fecha_envio_realizado)
);

-- Tabla de Configuración de Canales
CREATE TABLE configuracion_canal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    canal_notificacion ENUM('CORREO_ELECTRONICO', 'WHATSAPP', 'MENSAJE_TEXTO') NOT NULL,
    proveedor VARCHAR(50) NOT NULL, -- 'SMTP', 'TWILIO', 'WHATSAPP_API'
    configuracion JSON NOT NULL, -- Ej: {"host": "smtp.gmail.com", "port": 587, "username": "..."}
    activo BOOLEAN DEFAULT TRUE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_canal_activo (canal_notificacion, activo)
);

-- Tabla de Reglas de Anti-Saturación
CREATE TABLE regla_anti_saturacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo_evento ENUM('ENTREGA_EN_CAMINO', 'AVISO_ENTREGA_PROXIMA') NOT NULL,
    condicion_geografica ENUM('DENTRO_RADIO_KM', 'ENTREGAS_PREVIAS', 'TIEMPO_ULTIMA_NOTIFICACION') NOT NULL,
    valor_condicion DECIMAL(10,2) NOT NULL, -- Ej: 2.0 km, 3 entregas, 60 minutos
    activa BOOLEAN DEFAULT TRUE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tipo_activa (tipo_evento, activa)
);

-- Tabla de Historial de Envíos (para control de anti-saturación)
CREATE TABLE historial_envio_cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email_cliente VARCHAR(100) NOT NULL,
    id_envio BIGINT NOT NULL,
    tipo_evento ENUM('ENTREGA_EN_CAMINO', 'AVISO_ENTREGA_PROXIMA') NOT NULL,
    coordenadas_envio POINT,
    fecha_notificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    canal_notificacion ENUM('CORREO_ELECTRONICO', 'WHATSAPP', 'MENSAJE_TEXTO') NOT NULL,
    INDEX idx_cliente_fecha (email_cliente, fecha_notificacion),
    INDEX idx_tipo_fecha (tipo_evento, fecha_notificacion)
);

-- Tabla de Estadísticas de Notificaciones
CREATE TABLE estadistica_notificacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    tipo_evento ENUM('PEDIDO_CONFIRMADO', 'DESPACHO_INICIADO', 'ENTREGA_EN_CAMINO', 'ENTREGA_REALIZADA', 'ENTREGA_FALLIDA', 'AVISO_ENTREGA_PROXIMA') NOT NULL,
    canal_notificacion ENUM('CORREO_ELECTRONICO', 'WHATSAPP', 'MENSAJE_TEXTO') NOT NULL,
    total_enviados INT DEFAULT 0,
    total_exitosos INT DEFAULT 0,
    total_fallidos INT DEFAULT 0,
    tasa_exito DECIMAL(5,2) GENERATED ALWAYS AS (CASE WHEN total_enviados > 0 THEN (total_exitosos * 100.0 / total_enviados) ELSE 0 END) STORED,
    tiempo_promedio_envio INT, -- segundos
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_fecha_tipo_canal (fecha, tipo_evento, canal_notificacion),
    INDEX idx_fecha (fecha),
    INDEX idx_tipo_fecha (tipo_evento, fecha)
);

-- Trigger para actualizar timestamps
DELIMITER //
CREATE TRIGGER before_notificacion_update 
BEFORE UPDATE ON notificacion
FOR EACH ROW
BEGIN
    NEW.actualizado_en = CURRENT_TIMESTAMP;
    
    -- Actualizar fecha de envío cuando se envía exitosamente
    IF OLD.estado_proceso != 'ENVIADO_EXITOSO' AND NEW.estado_proceso = 'ENVIADO_EXITOSO' THEN
        NEW.fecha_envio_realizado = CURRENT_TIMESTAMP;
    END IF;
    
    -- Incrementar intentos
    IF OLD.estado_proceso != NEW.estado_proceso AND NEW.estado_proceso IN ('ERROR_ENVIO', 'PROGRAMADO_REINTENTO') THEN
        NEW.reintentos_realizados = NEW.reintentos_realizados + 1;
        
        -- Programar próximo reintento
        IF NEW.reintentos_realizados < NEW.limite_maximo_reintentos THEN
            SET NEW.fecha_proximo_reintento = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 
                CASE NEW.reintentos_realizados
                    WHEN 1 THEN 5 -- 5 minutos
                    WHEN 2 THEN 15 -- 15 minutos
                    ELSE 60 -- 1 hora
                END MINUTE);
            SET NEW.estado_proceso = 'PROGRAMADO_REINTENTO';
        ELSE
            SET NEW.estado_proceso = 'ERROR_ENVIO';
        END IF;
    END IF;
END//

CREATE TRIGGER before_plantilla_update 
BEFORE UPDATE ON plantilla_notificacion
FOR EACH ROW
BEGIN
    NEW.actualizado_en = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER before_configuracion_update 
BEFORE UPDATE ON configuracion_canal
FOR EACH ROW
BEGIN
    NEW.actualizado_en = CURRENT_TIMESTAMP;
END//
DELIMITER ;

-- Trigger para actualizar estadísticas
DELIMITER //
CREATE TRIGGER after_notificacion_update
AFTER UPDATE ON notificacion
FOR EACH ROW
BEGIN
    IF OLD.estado_proceso != 'ENVIADO_EXITOSO' AND NEW.estado_proceso = 'ENVIADO_EXITOSO' THEN
        INSERT INTO estadistica_notificacion (
            fecha, 
            tipo_evento, 
            canal_notificacion, 
            total_enviados, 
            total_exitosos
        ) VALUES (
            CURRENT_DATE,
            NEW.tipo_evento,
            NEW.canal_notificacion,
            1,
            1
        ) ON DUPLICATE KEY UPDATE 
            total_enviados = total_enviados + 1,
            total_exitosos = total_exitosos + 1,
            actualizado_en = CURRENT_TIMESTAMP;
            
    ELSEIF OLD.estado_proceso NOT IN ('ERROR_ENVIO', 'ENVIADO_EXITOSO') AND NEW.estado_proceso = 'ERROR_ENVIO' THEN
        INSERT INTO estadistica_notificacion (
            fecha, 
            tipo_evento, 
            canal_notificacion, 
            total_enviados, 
            total_fallidos
        ) VALUES (
            CURRENT_DATE,
            NEW.tipo_evento,
            NEW.canal_notificacion,
            1,
            1
        ) ON DUPLICATE KEY UPDATE 
            total_enviados = total_enviados + 1,
            total_fallidos = total_fallidos + 1,
            actualizado_en = CURRENT_TIMESTAMP;
    END IF;
END//
DELIMITER ;

-- Datos de ejemplo (plantillas y configuración) - Personalizado para pymes chilenas
INSERT INTO plantilla_notificacion (nombre, tipo_evento, canal_notificacion, asunto, contenido_formato_texto, variables_personalizacion) VALUES
('Correo Pedido Confirmado', 'PEDIDO_CONFIRMADO', 'CORREO_ELECTRONICO', '¡Tu pedido {{numero_pedido}} está confirmado!', 
 '¡Hola {{nombre_cliente}}! 🎉\n\n¡Buenas noticias! Tu pedido {{numero_pedido}} ya está confirmado y nuestro equipo está preparándolo con mucho cariño.\n\n📦 Productos: {{detalle_productos}}\n💰 Total: ${{total_pedido}}\n📍 Dirección de entrega: {{direccion_entrega}}\n\nTe mantendremos informado de cada paso. ¡Gracias por confiar en PymeTrack!\n\nSaludos cordiales,\nEl equipo de {{nombre_pyme}}', 
 '["{{nombre_cliente}}", "{{numero_pedido}}", "{{detalle_productos}}", "{{total_pedido}}", "{{direccion_entrega}}", "{{nombre_pyme}}"]'),

('WhatsApp Despacho Iniciado', 'DESPACHO_INICIADO', 'WHATSAPP', '🚚 ¡Tu pedido va en camino!', 
 '¡Hola {{nombre_cliente}}! 🛵\n\n¡Excelente noticia! Tu pedido {{numero_pedido}} ya está en camino.\n\n📦 Etiqueta de seguimiento: {{etiqueta_despacho}}\n👤 Repartidor: {{nombre_repartidor}}\n📱 Teléfono del repartidor: {{telefono_repartidor}}\n\n¡Prepárate para recibirlo! 🎯\n\nPuedes seguir tu pedido aquí: {{link_seguimiento}}', 
 '["{{nombre_cliente}}", "{{numero_pedido}}", "{{etiqueta_despacho}}", "{{nombre_repartidor}}", "{{telefono_repartidor}}", "{{link_seguimiento}}"]'),

('Correo Entrega en Camino', 'ENTREGA_EN_CAMINO', 'CORREO_ELECTRONICO', '🏠 Tu repartidor está cerca', 
 '¡Hola {{nombre_cliente}}! 🏠\n\n¡Buenas noticias! Tu repartidor {{nombre_repartidor}} está muy cerca de tu dirección.\n\n📍 Dirección: {{direccion_entrega}}\n⏰ Tiempo estimado: {{tiempo_entrega}} minutos\n📱 Teléfono del repartidor: {{telefono_repartidor}}\n\n¡Prepárate para recibir tu pedido! 📦✨\n\nSi no estarás, avísanos lo antes posible.\n\n¡Gracias por tu paciencia!\nEl equipo de PymeTrack', 
 '["{{nombre_cliente}}", "{{nombre_repartidor}}", "{{direccion_entrega}}", "{{tiempo_entrega}}", "{{telefono_repartidor}}"]'),

('SMS Entrega Realizada', 'ENTREGA_REALIZADA', 'MENSAJE_TEXTO', '✅ ¡Pedido entregado!', 
 '¡Hola {{nombre_cliente}}! ✅ Tu pedido {{numero_pedido}} fue entregado exitosamente. ¡Que lo disfrutes! 🎉 Gracias por comprar en {{nombre_pyme}}', 
 '["{{nombre_cliente}}", "{{numero_pedido}}", "{{nombre_pyme}}"]'),

('Correo Problema Entrega', 'ENTREGA_FALLIDA', 'CORREO_ELECTRONICO', '📍 Necesitamos coordinar tu entrega', 
 '¡Hola {{nombre_cliente}}! 📍\n\nLamentamos informarte que no pudimos realizar la entrega de tu pedido {{numero_pedido}}.\n\n🔍 Motivo: {{motivo_falla}}\n📦 Tu pedido está seguro y lo intentaremos nuevamente.\n\n📞 ¿Podrías contactarnos al {{telefono_pyme}} para coordinar?\n\nAgradecemos tu comprensión y paciencia.\n\nSaludos cordiales,\nEl equipo de {{nombre_pyme}}', 
 '["{{nombre_cliente}}", "{{numero_pedido}}", "{{motivo_falla}}", "{{telefono_pyme}}", "{{nombre_pyme}}"]'),

('WhatsApp Aviso Entrega Próxima', 'AVISO_ENTREGA_PROXIMA', 'WHATSAPP', '⏰ ¡Tu pedido llega hoy!', 
 '¡Hola {{nombre_cliente}}! ⏰\n\n¡Atención! Tu pedido {{numero_pedido}} será entregado hoy.\n\n🕐 Horario estimado: {{horario_entrega}}\n📍 Dirección: {{direccion_entrega}}\n\n¡Asegúrate de estar disponible! 🏠\n\nSi necesitas cambiar algo, avísanos rápido.\n\n¡Gracias! 🎉', 
 '["{{nombre_cliente}}", "{{numero_pedido}}", "{{horario_entrega}}", "{{direccion_entrega}}"]');

INSERT INTO configuracion_canal (canal_notificacion, proveedor, configuracion, activo) VALUES
('CORREO_ELECTRONICO', 'SMTP', '{"host": "smtp.gmail.com", "port": 587, "username": "contacto@pymetrack.cl", "password": "app_password_here", "from": "PymeTrack Chile <contacto@pymetrack.cl>", "use_tls": true, "use_ssl": false}', TRUE),
('MENSAJE_TEXTO', 'TWILIO', '{"account_sid": "ACxxxxxxxx", "auth_token": "xxxxxxxx", "from_number": "+569XXXXXXXX"}', FALSE),
('WHATSAPP', 'WHATSAPP_API', '{"phone_number_id": "xxxxxxxx", "access_token": "xxxxxxxx", "version": "v16.0"}', FALSE);

INSERT INTO regla_anti_saturacion (nombre, tipo_evento, condicion_geografica, valor_condicion, activa) VALUES
('Radio 2km para entrega en camino', 'ENTREGA_EN_CAMINO', 'DENTRO_RADIO_KM', 2.0, TRUE),
('Máximo 3 notificaciones por hora', 'AVISO_ENTREGA_PROXIMA', 'TIEMPO_ULTIMA_NOTIFICACION', 60.0, TRUE),
('Máximo 2 entregas previas', 'ENTREGA_EN_CAMINO', 'ENTREGAS_PREVIAS', 2.0, TRUE);
