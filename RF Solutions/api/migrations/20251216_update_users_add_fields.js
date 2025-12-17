/**
 * @param { import('knex').Knex } knex
 * @returns { Promise<void> }
 */
export async function up(knex) {
  const has = await knex.schema.hasTable('users');
  if (has) {
    await knex.schema.alterTable('users', (t) => {
      // Departamento al que pertenece el usuario (puede ser null)
      t.integer('id_departamento').unsigned().nullable()
        .references('id_departamento').inTable('departamentos').onDelete('SET NULL');

      // Estado del usuario
      t.enu('estado', ['ACTIVO','INACTIVO','BLOQUEADO']).notNullable().defaultTo('ACTIVO');

      // Otros datos (telefono, rut, etc.)
      t.text('otros_datos').nullable();

      // Rol: administrador del departamento o operador
      t.enu('role', ['ADMIN','OPERADOR']).notNullable().defaultTo('OPERADOR');
    });
  }
}

/**
 * @param { import('knex').Knex } knex
 * @returns { Promise<void> }
 */
export async function down(knex) {
  const has = await knex.schema.hasTable('users');
  if (has) {
    await knex.schema.alterTable('users', (t) => {
      t.dropColumn('id_departamento');
      t.dropColumn('estado');
      t.dropColumn('otros_datos');
      t.dropColumn('role');
    });
  }
}
