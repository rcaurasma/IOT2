// routes/iot.js
import express from 'express';
import db from '../db/knex.js';
import auth from '../middleware/auth.js';
import { loadCurrentUser } from '../middleware/roles.js';

const router = express.Router();

/**
 * GET /api/iot/data
 *
 * Devuelve un payload simulado para la parte de IoT (sin temperatura/humedad).
 * Útil para pruebas de la app que simula el dispositivo.
 */
router.get('/data', (req, res) => {
    const payload = {
        status: 'IDLE',
        message: 'Simulación de datos IoT (sin sensores físicos)',
        timestamp: new Date().toISOString()
    };
    return res.json(payload);
});

// Registrar un sensor (solo ADMIN del departamento)
router.post('/sensors', auth, loadCurrentUser, async (req, res) => {
    try {
        const user = req.currentUser;
        if (user.role !== 'ADMIN') return res.status(403).json({ error: 'Requiere ser admin' });
        const { codigo_sensor, tipo, id_usuario } = req.body;
        if (!codigo_sensor || !tipo) return res.status(400).json({ error: 'Faltan datos' });
        const exists = await db('sensores').where({ codigo_sensor }).first();
        if (exists) return res.status(409).json({ error: 'Sensor ya registrado' });
        const [id] = await db('sensores').insert({ codigo_sensor, tipo, id_departamento: user.id_departamento, id_usuario: id_usuario || null });
        const sensor = await db('sensores').where({ id_sensor: id }).first();
        return res.status(201).json({ message: 'Sensor registrado', sensor });
    } catch (e) {
        console.error('Error registering sensor', e);
        return res.status(500).json({ error: 'Error interno' });
    }
});

// Activar/Desactivar sensor (admin del departamento)
router.patch('/sensors/:id/estado', auth, loadCurrentUser, async (req, res) => {
    try {
        const user = req.currentUser;
        const { id } = req.params;
        const { estado } = req.body;
        if (user.role !== 'ADMIN') return res.status(403).json({ error: 'Requiere ser admin' });
        const sensor = await db('sensores').where({ id_sensor: id }).first();
        if (!sensor) return res.status(404).json({ error: 'Sensor no encontrado' });
        if (String(sensor.id_departamento) !== String(user.id_departamento)) return res.status(403).json({ error: 'Sensor no pertenece a su departamento' });
        if (!['ACTIVO','INACTIVO','PERDIDO','BLOQUEADO'].includes(estado)) return res.status(400).json({ error: 'Estado inválido' });
        await db('sensores').where({ id_sensor: id }).update({ estado });
        return res.json({ message: 'Estado de sensor actualizado' });
    } catch (e) {
        console.error('Error updating sensor estado', e);
        return res.status(500).json({ error: 'Error interno' });
    }
});

// Simular lectura de sensor / registrar evento de acceso
// Body opcional: { codigo_sensor, user_id, tipo_evento }
// tipo_evento puede ser: 'ACCESO_VALIDO','ACCESO_RECHAZADO','APERTURA_MANUAL','CIERRE_MANUAL'
router.post('/access', async (req, res) => {
    try {
        const { codigo_sensor, user_id, tipo_evento } = req.body;
        if (!codigo_sensor) return res.status(400).json({ error: 'Codigo sensor requerido' });
        const sensor = await db('sensores').where({ codigo_sensor }).first();
        if (!sensor) return res.status(404).json({ error: 'Sensor no encontrado' });

        let usuario = null;
        if (user_id) usuario = await db('users').where({ id: user_id }).first();
        else if (sensor.id_usuario) usuario = await db('users').where({ id: sensor.id_usuario }).first();

        const now = new Date();
        // Si se especifica un tipo de evento manual desde la app, respetarlo
        const tipo = tipo_evento || 'ACCESO_VALIDO';

        // Determinar resultado por defecto
        let resultado = 'PERMITIDO';
        if (tipo === 'APERTURA_MANUAL' || tipo === 'CIERRE_MANUAL') {
            // acciones manuales por app siempre se registran como PERMITIDO
            resultado = 'PERMITIDO';
        } else {
            if (sensor.estado !== 'ACTIVO') resultado = 'DENEGADO';
            if (usuario && usuario.estado !== 'ACTIVO') resultado = 'DENEGADO';
        }

        await db('eventos_acceso').insert({ id_sensor: sensor.id_sensor, id_usuario: usuario ? usuario.id : null, tipo_evento: tipo, fecha_hora: now, resultado });
        return res.json({ message: 'Evento registrado', resultado, tipo_evento: tipo });
    } catch (e) {
        console.error('Error registering access event', e);
        return res.status(500).json({ error: 'Error interno' });
    }
});

// Obtener historial de eventos por departamento (admin y operadores del depto)
router.get('/departamento/:id/eventos', auth, loadCurrentUser, async (req, res) => {
    try {
        const { id } = req.params;
        const user = req.currentUser;
        if (String(user.id_departamento) !== String(id)) return res.status(403).json({ error: 'Acceso denegado' });
        const events = await db('eventos_acceso as e')
            .leftJoin('sensores as s', 'e.id_sensor', 's.id_sensor')
            .leftJoin('users as u', 'e.id_usuario', 'u.id')
            .where('s.id_departamento', id)
            .select('e.*', 's.codigo_sensor', 'u.name as user_name', 'u.last_name as user_last_name')
            .orderBy('e.fecha_hora', 'desc')
            .limit(500);
        return res.json(events);
    } catch (e) {
        console.error('Error fetching events', e);
        return res.status(500).json({ error: 'Error interno' });
    }
});

export default router;
