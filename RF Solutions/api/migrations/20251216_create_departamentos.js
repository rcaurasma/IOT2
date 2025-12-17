/**
 * @param { import('knex').Knex } knex
 * @returns { Promise<void> }
 */
export async function up(knex) {
  const has = await knex.schema.hasTable('departamentos');
  if (!has) {
    await knex.schema.createTable('departamentos', (t) => {
      t.increments('id_departamento').primary();
      t.string('numero', 60).notNullable();
      t.string('torre', 60).nullable();
      t.text('otros_datos').nullable();
      t.timestamp('created_at').defaultTo(knex.fn.now());
    });
  }
}

/**
 * @param { import('knex').Knex } knex
 * @returns { Promise<void> }
 */
export async function down(knex) {
  await knex.schema.dropTableIfExists('departamentos');
}
