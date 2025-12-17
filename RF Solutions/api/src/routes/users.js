import { Router } from 'express';
import db from '../db/knex.js';
import bcrypt from 'bcryptjs';
import auth from '../middleware/auth.js';
import { loadCurrentUser, requireAdminOfDepartment } from '../middleware/roles.js';

const router = Router();
const SALT_ROUNDS = 10;

function normalizeEmail(email) {
	return email?.trim().toLowerCase();
}

function pickUserFields(user) {
	if (!user) return null;
	const { id, name, last_name: lastName, email, created_at: createdAt } = user;
	return {
		id,
		name,
		last_name: lastName,
		email,
		created_at: createdAt,
	};
}

router.get('/', async (req, res) => {
	try {
		const users = await db('users')
			.select('id', 'name', 'last_name', 'email', 'created_at')
			.orderBy('name', 'asc');
		return res.json(users);
	} catch (error) {
		console.error('Error fetching users:', error);
		return res.status(500).json({ error: 'Error interno del servidor' });
	}
});

router.get('/:id', async (req, res) => {
	try {
		const { id } = req.params;
		const user = await db('users')
			.where({ id })
			.first('id', 'name', 'last_name', 'email', 'created_at');
		if (!user) {
			return res.status(404).json({ error: 'Usuario no encontrado' });
		}
		return res.json(user);
	} catch (error) {
		console.error('Error fetching user:', error);
		return res.status(500).json({ error: 'Error interno del servidor' });
	}
});

router.post('/', auth, loadCurrentUser, async (req, res) => {
	try {
		const { name, last_name: lastName, email, password, id_departamento, role } = req.body;

		if (!name || !lastName || !email || !password) {
			return res.status(400).json({ error: 'Faltan datos obligatorios' });
		}

		const normalizedEmail = normalizeEmail(email);
		const existingUser = await db('users').where({ email: normalizedEmail }).first();
		if (existingUser) {
			return res.status(409).json({ error: 'El correo ya está registrado' });
		}

		// Si se intenta crear con role=ADMIN o asignar departamento, solo admins del departamento pueden hacerlo
		if ((role === 'ADMIN' || id_departamento) && req.currentUser.role !== 'ADMIN') {
			return res.status(403).json({ error: 'Solo administradores pueden asignar roles o departamentos' });
		}

		// Si se intenta crear ADMIN, comprobar que no exista otro admin en ese departamento
		if (role === 'ADMIN' && id_departamento) {
			const other = await db('users').where({ id_departamento, role: 'ADMIN' }).first();
			if (other) return res.status(409).json({ error: 'Ya existe un administrador para ese departamento' });
		}

		const passwordHash = await bcrypt.hash(password, SALT_ROUNDS);
		const [id] = await db('users').insert({
			name,
			last_name: lastName,
			email: normalizedEmail,
			password_hash: passwordHash,
			id_departamento: id_departamento || null,
			role: role || 'OPERADOR'
		});

		const user = await db('users')
			.where({ id })
			.first('id', 'name', 'last_name', 'email', 'created_at', 'id_departamento', 'role');

		return res.status(201).json({ message: 'Usuario creado correctamente', user });
	} catch (error) {
		console.error('Error creating user:', error);
		return res.status(500).json({ error: 'Error interno del servidor' });
	}
});

router.put('/:id', auth, loadCurrentUser, async (req, res) => {
	try {
		const { id } = req.params;
		const { name, last_name: lastName, email, role, id_departamento } = req.body;

		if (!name || !lastName || !email) {
			return res.status(400).json({ error: 'Faltan datos obligatorios' });
		}

		const normalizedEmail = normalizeEmail(email);
		const user = await db('users').where({ id }).first();
		if (!user) {
			return res.status(404).json({ error: 'Usuario no encontrado' });
		}

		const emailOwner = await db('users')
			.where({ email: normalizedEmail })
			.whereNot({ id })
			.first();
		if (emailOwner) {
			return res.status(409).json({ error: 'El correo ya está registrado por otro usuario' });
		}

		// Si se intenta cambiar role o departamento, debe hacerlo un admin del departamento
		if ((role && role !== user.role) || (id_departamento && String(id_departamento) !== String(user.id_departamento))) {
			if (req.currentUser.role !== 'ADMIN') return res.status(403).json({ error: 'Solo administradores pueden cambiar role o departamento' });
			// Si promoviendo a ADMIN, verificar unicidad
			if (role === 'ADMIN') {
				const other = await db('users').where({ id_departamento: id_departamento || user.id_departamento, role: 'ADMIN' }).whereNot({ id }).first();
				if (other) return res.status(409).json({ error: 'Ya existe un administrador para ese departamento' });
			}
		}

		await db('users')
			.where({ id })
			.update({ name, last_name: lastName, email: normalizedEmail, role: role || user.role, id_departamento: id_departamento || user.id_departamento });

		const updated = await db('users')
			.where({ id })
			.first('id', 'name', 'last_name', 'email', 'created_at');

		return res.json({
			message: 'Usuario actualizado correctamente',
			user: updated,
		});
	} catch (error) {
		console.error('Error updating user:', error);
		return res.status(500).json({ error: 'Error interno del servidor' });
	}
});

// Admin: activar / desactivar usuario
router.patch('/:id/estado', auth, loadCurrentUser, async (req, res) => {
	try {
		const { id } = req.params;
		const { estado } = req.body;
		if (!['ACTIVO', 'INACTIVO', 'BLOQUEADO'].includes(estado)) return res.status(400).json({ error: 'Estado inválido' });
		const target = await db('users').where({ id }).first();
		if (!target) return res.status(404).json({ error: 'Usuario no encontrado' });
		// Solo admin del mismo departamento puede cambiar estado
		if (req.currentUser.role !== 'ADMIN' || String(req.currentUser.id_departamento) !== String(target.id_departamento)) {
			return res.status(403).json({ error: 'Acceso denegado' });
		}
		await db('users').where({ id }).update({ estado });
		return res.json({ message: 'Estado actualizado' });
	} catch (e) {
		console.error('Error updating estado', e);
		return res.status(500).json({ error: 'Error interno' });
	}
});

router.delete('/:id', async (req, res) => {
	try {
		const { id } = req.params;
		const deleted = await db('users').where({ id }).del();
		if (!deleted) {
			return res.status(404).json({ error: 'Usuario no encontrado' });
		}
		return res.json({ message: 'Usuario eliminado correctamente' });
	} catch (error) {
		console.error('Error deleting user:', error);
		return res.status(500).json({ error: 'Error interno del servidor' });
	}
});

export default router;
