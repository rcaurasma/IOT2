import bcrypt from 'bcryptjs';

/**
 * @param { import('knex').Knex } knex
 * @returns { Promise<void> }
 */
export async function seed(knex) {
  // Limpia tablas relevantes (orden cuidadoso por FK)
  await knex('eventos_acceso').del().catch(()=>{});
  await knex('sensores').del().catch(()=>{});
  await knex('password_reset_codes').del().catch(()=>{});
  await knex('users').del();
  await knex('departamentos').del();

  const hash = await bcrypt.hash('123456', 10);

  // Crear un departamento de ejemplo
  const [deptId] = await knex('departamentos').insert({ numero: '101', torre: 'A', otros_datos: 'Condominio Demo' });

  // Crear un usuario admin del departamento
  const [userId] = await knex('users').insert({
    name: 'Demo',
    last_name: 'Apellido',
    email: 'demo@example.com',
    password_hash: hash,
    id_departamento: deptId,
    estado: 'ACTIVO',
    role: 'ADMIN'
  });

  // Crear un sensor asociado al departamento y al usuario
  await knex('sensores').insert({ codigo_sensor: 'RFID-0001', estado: 'ACTIVO', id_departamento: deptId, id_usuario: userId, tipo: 'Llavero' });
}
