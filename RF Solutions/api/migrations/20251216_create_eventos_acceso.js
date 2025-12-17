/**
 * @param { import('knex').Knex } knex
 * @returns { Promise<void> }
 */
export async function up(knex) {
  const has = await knex.schema.hasTable('eventos_acceso');
  if (!has) {
    await knex.schema.createTable('eventos_acceso', (t) => {
      t.increments('id_evento').primary();
      t.integer('id_sensor').unsigned().notNullable()
        .references('id_sensor').inTable('sensores').onDelete('CASCADE');
      // Referencia a la tabla de usuarios existentes (tabla `users`)
      t.integer('id_usuario').unsigned().nullable()
        .references('id').inTable('users').onDelete('SET NULL');
      t.string('tipo_evento', 120).notNullable();
      t.timestamp('fecha_hora').defaultTo(knex.fn.now());
      t.enu('resultado', ['PERMITIDO','DENEGADO']).notNullable();
    });
  }
}

/**
 * @param { import('knex').Knex } knex
 * @returns { Promise<void> }
 */
export async function down(knex) {
  await knex.schema.dropTableIfExists('eventos_acceso');
}
