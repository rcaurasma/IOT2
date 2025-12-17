import db from '../db/knex.js';

// Carga el usuario actual a partir de req.userId (establecido por auth middleware)
export async function loadCurrentUser(req, res, next) {
  try {
    if (!req.userId) return res.status(401).json({ error: 'No autenticado' });
    const user = await db('users').where({ id: req.userId }).first();
    if (!user) return res.status(401).json({ error: 'Usuario no encontrado' });
    req.currentUser = user;
    return next();
  } catch (e) {
    console.error('Error loading current user', e);
    return res.status(500).json({ error: 'Error interno' });
  }
}

// Requiere que el usuario actual sea ADMIN del departamento indicado
// departmentId puede ser valor numérico o nombre de parámetro en req.params
export function requireAdminOfDepartment(departmentIdParam) {
  return (req, res, next) => {
    const user = req.currentUser;
    if (!user) return res.status(401).json({ error: 'No autenticado' });
    const deptId = typeof departmentIdParam === 'string' && departmentIdParam.startsWith(':')
      ? req.params[departmentIdParam.slice(1)]
      : departmentIdParam;
    if (!deptId) return res.status(400).json({ error: 'Falta id de departamento' });
    if (user.role !== 'ADMIN' || String(user.id_departamento) !== String(deptId)) {
      return res.status(403).json({ error: 'Requiere ser administrador del departamento' });
    }
    return next();
  };
}

// Requiere que el usuario sea ADMIN del mismo departamento que targetId param or sea el mismo usuario
export function requireAdminOrSelf() {
  return (req, res, next) => {
    const user = req.currentUser;
    if (!user) return res.status(401).json({ error: 'No autenticado' });
    const targetId = req.params.id;
    if (!targetId) return res.status(400).json({ error: 'Falta id objetivo' });
    if (String(user.id) === String(targetId)) return next();
    // si no es self, check admin of same department
    if (user.role === 'ADMIN') return next();
    return res.status(403).json({ error: 'Acceso denegado' });
  };
}
