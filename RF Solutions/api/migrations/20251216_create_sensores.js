/**
 * @param { import('knex').Knex } knex
 * @returns { Promise<void> }
 */
export async function up(knex) {
  const has = await knex.schema.hasTable('sensores');
  if (!has) {
    await knex.schema.createTable('sensores', (t) => {
      t.increments('id_sensor').primary();
      t.string('codigo_sensor', 120).notNullable().unique();
      t.enu('estado', ['ACTIVO','INACTIVO','PERDIDO','BLOQUEADO']).notNullable().defaultTo('ACTIVO');
      t.integer('id_departamento').unsigned().nullable()
        .references('id_departamento').inTable('departamentos').onDelete('SET NULL');
      t.string('tipo', 60).notNullable();
      t.timestamp('fecha_alta').defaultTo(knex.fn.now());
      t.timestamp('fecha_baja').nullable();
    });
  }
}

/**
 * @param { import('knex').Knex } knex
 * @returns { Promise<void> }
 */
export async function down(knex) {
  await knex.schema.dropTableIfExists('sensores');
}
