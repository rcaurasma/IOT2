import { Router } from 'express';
import db from '../db/knex.js';
import bcrypt from 'bcryptjs';

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

router.post('/', async (req, res) => {
	try {
		const { name, last_name: lastName, email, password } = req.body;

		if (!name || !lastName || !email || !password) {
			return res.status(400).json({ error: 'Faltan datos obligatorios' });
		}

		const normalizedEmail = normalizeEmail(email);
		const existingUser = await db('users').where({ email: normalizedEmail }).first();
		if (existingUser) {
			return res.status(409).json({ error: 'El correo ya está registrado' });
		}

		const passwordHash = await bcrypt.hash(password, SALT_ROUNDS);
		const [id] = await db('users').insert({
			name,
			last_name: lastName,
			email: normalizedEmail,
			password_hash: passwordHash,
		});

		const user = await db('users')
			.where({ id })
			.first('id', 'name', 'last_name', 'email', 'created_at');

		return res.status(201).json({
			message: 'Usuario creado correctamente',
			user,
		});
	} catch (error) {
		console.error('Error creating user:', error);
		return res.status(500).json({ error: 'Error interno del servidor' });
	}
});

router.put('/:id', async (req, res) => {
	try {
		const { id } = req.params;
		const { name, last_name: lastName, email } = req.body;

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

		await db('users')
			.where({ id })
			.update({ name, last_name: lastName, email: normalizedEmail });

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
