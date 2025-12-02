package com.ev.iot2.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.ev.iot2.data.model.User
import java.security.MessageDigest
import java.security.SecureRandom

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "iotemp.db"
        private const val DATABASE_VERSION = 2

        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD_HASH = "password_hash"
        private const val COLUMN_SALT = "salt"
        private const val COLUMN_CREATED_AT = "created_at"

        // Recovery codes table
        private const val TABLE_RECOVERY_CODES = "recovery_codes"
        private const val COLUMN_CODE = "code"
        private const val COLUMN_USED = "used"
        
        private const val SALT_LENGTH = 16
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD_HASH TEXT NOT NULL,
                $COLUMN_SALT TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        val createRecoveryCodesTable = """
            CREATE TABLE $TABLE_RECOVERY_CODES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EMAIL TEXT NOT NULL,
                $COLUMN_CODE TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_USED INTEGER DEFAULT 0
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createRecoveryCodesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECOVERY_CODES")
        onCreate(db)
    }
    
    // Generate a random salt
    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    // Hash password using SHA-256 with salt for better security
    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val saltedPassword = salt + password
        val hashBytes = digest.digest(saltedPassword.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // User CRUD operations
    fun insertUser(name: String, email: String, password: String): Long {
        val db = writableDatabase
        val salt = generateSalt()
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_EMAIL, email.lowercase())
            put(COLUMN_PASSWORD_HASH, hashPassword(password, salt))
            put(COLUMN_SALT, salt)
            put(COLUMN_CREATED_AT, System.currentTimeMillis())
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_EMAIL = ?",
            arrayOf(email.lowercase()),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                    email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    passwordHash = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)),
                    salt = it.getString(it.getColumnIndexOrThrow(COLUMN_SALT)),
                    createdAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_CREATED_AT))
                )
            } else null
        }
    }

    fun getUserById(id: Long): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                    email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    passwordHash = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)),
                    salt = it.getString(it.getColumnIndexOrThrow(COLUMN_SALT)),
                    createdAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_CREATED_AT))
                )
            } else null
        }
    }

    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = readableDatabase
        val cursor = db.query(TABLE_USERS, null, null, null, null, null, "$COLUMN_NAME ASC")
        cursor.use {
            while (it.moveToNext()) {
                users.add(
                    User(
                        id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME)),
                        email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        passwordHash = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)),
                        salt = it.getString(it.getColumnIndexOrThrow(COLUMN_SALT)),
                        createdAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_CREATED_AT))
                    )
                )
            }
        }
        return users
    }

    fun updateUser(id: Long, name: String, email: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_EMAIL, email.lowercase())
        }
        return db.update(TABLE_USERS, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun updateUserPassword(email: String, newPassword: String): Int {
        val db = writableDatabase
        val salt = generateSalt()
        val values = ContentValues().apply {
            put(COLUMN_PASSWORD_HASH, hashPassword(newPassword, salt))
            put(COLUMN_SALT, salt)
        }
        return db.update(TABLE_USERS, values, "$COLUMN_EMAIL = ?", arrayOf(email.lowercase()))
    }

    fun deleteUser(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_USERS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun isEmailExists(email: String, excludeId: Long? = null): Boolean {
        val db = readableDatabase
        val selection = if (excludeId != null) {
            "$COLUMN_EMAIL = ? AND $COLUMN_ID != ?"
        } else {
            "$COLUMN_EMAIL = ?"
        }
        val selectionArgs = if (excludeId != null) {
            arrayOf(email.lowercase(), excludeId.toString())
        } else {
            arrayOf(email.lowercase())
        }
        val cursor = db.query(TABLE_USERS, arrayOf(COLUMN_ID), selection, selectionArgs, null, null, null)
        return cursor.use { it.count > 0 }
    }

    fun validateLogin(email: String, password: String): User? {
        val user = getUserByEmail(email) ?: return null
        return if (user.passwordHash == hashPassword(password, user.salt)) user else null
    }

    // Recovery code operations
    fun insertRecoveryCode(email: String, code: String): Long {
        val db = writableDatabase
        // Invalidate old codes for this email
        val updateValues = ContentValues().apply {
            put(COLUMN_USED, 1)
        }
        db.update(TABLE_RECOVERY_CODES, updateValues, "$COLUMN_EMAIL = ? AND $COLUMN_USED = 0", arrayOf(email.lowercase()))
        
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, email.lowercase())
            put(COLUMN_CODE, code)
            put(COLUMN_CREATED_AT, System.currentTimeMillis())
            put(COLUMN_USED, 0)
        }
        return db.insert(TABLE_RECOVERY_CODES, null, values)
    }

    fun validateRecoveryCode(email: String, code: String): Boolean {
        val db = readableDatabase
        val oneMinuteAgo = System.currentTimeMillis() - 60000 // 1 minute validity
        val cursor = db.query(
            TABLE_RECOVERY_CODES,
            null,
            "$COLUMN_EMAIL = ? AND $COLUMN_CODE = ? AND $COLUMN_USED = 0 AND $COLUMN_CREATED_AT > ?",
            arrayOf(email.lowercase(), code, oneMinuteAgo.toString()),
            null, null, null
        )
        return cursor.use { it.count > 0 }
    }

    fun markRecoveryCodeAsUsed(email: String, code: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USED, 1)
        }
        return db.update(
            TABLE_RECOVERY_CODES,
            values,
            "$COLUMN_EMAIL = ? AND $COLUMN_CODE = ?",
            arrayOf(email.lowercase(), code)
        )
    }
}
