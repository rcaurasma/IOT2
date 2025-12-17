/**
 * @param { import('knex').Knex } knex
 * @returns { Promise<void> }
 */
export async function up(knex) {
  const has = await knex.schema.hasTable('sensores');
  if (has) {
    await knex.schema.alterTable('sensores', (t) => {
      t.integer('id_usuario').unsigned().nullable()
        .references('id').inTable('users').onDelete('SET NULL');
    });
  }
}

/**
 * @param { import('knex').Knex } knex
 * @returns { Promise<void> }
 */
export async function down(knex) {
  const has = await knex.schema.hasTable('sensores');
  if (has) {
    await knex.schema.alterTable('sensores', (t) => {
      t.dropColumn('id_usuario');
    });
  }
}
