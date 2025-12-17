// routes/iot.js
import express from 'express';
import db from '../db/knex.js';
import auth from '../middleware/auth.js';
import { loadCurrentUser } from '../middleware/roles.js';

const router = express.Router();

/**
 * GET /api/iot/data
 *
 * Devuelve datos de ejemplo:
 * {
 *   temperature: 23.5,
 *   humidity: 56.7,
 *   timestamp: "2025-11-20T12:34:56.789Z"
 * }
 */
router.get('/data', (req, res) => {
    // Temperatura entre 18 y 30 grados
    const temperature = (18 + Math.random() * 12).toFixed(1);

    // Humedad entre 30% y 80%
    const humidity = (30 + Math.random() * 50).toFixed(1);

    const payload = {
        temperature: Number(temperature),
        humidity: Number(humidity),
        timestamp: new Date().toLocaleString('es-CL', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
        })
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
router.post('/access', async (req, res) => {
    try {
        const { codigo_sensor, user_id } = req.body;
        if (!codigo_sensor) return res.status(400).json({ error: 'Codigo sensor requerido' });
        const sensor = await db('sensores').where({ codigo_sensor }).first();
        if (!sensor) return res.status(404).json({ error: 'Sensor no encontrado' });

        let usuario = null;
        if (user_id) usuario = await db('users').where({ id: user_id }).first();
        else if (sensor.id_usuario) usuario = await db('users').where({ id: sensor.id_usuario }).first();

        const now = new Date();
        // Determinar resultado
        let resultado = 'PERMITIDO';
        if (sensor.estado !== 'ACTIVO') resultado = 'DENEGADO';
        if (usuario && usuario.estado !== 'ACTIVO') resultado = 'DENEGADO';

        await db('eventos_acceso').insert({ id_sensor: sensor.id_sensor, id_usuario: usuario ? usuario.id : null, tipo_evento: 'ACCESO_VALIDO', fecha_hora: now, resultado });
        return res.json({ message: 'Evento registrado', resultado });
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
