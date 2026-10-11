package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

@Database(
    entities = [
        UserEntity::class,
        CustomerEntity::class,
        ProductEntity::class,
        SaleEntity::class,
        AchievementEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KasuwanciDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun achievementDao(): AchievementDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: KasuwanciDatabase? = null

        fun sha256(text: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(text.toByteArray())
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }

        fun getInstance(context: Context): KasuwanciDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KasuwanciDatabase::class.java,
                    "kasuwanci.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default data on first creation
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedInitialData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: KasuwanciDatabase) {
            // Seed Admin User
            val adminPasswordHash = sha256("admin123")
            db.userDao().insert(
                UserEntity(
                    username = "admin",
                    passwordHash = adminPasswordHash,
                    role = "admin"
                )
            )

            // Seed Settings
            db.settingDao().set(SettingEntity("language", "ha"))
            db.settingDao().set(SettingEntity("theme", "neon"))
            db.settingDao().set(SettingEntity("currency", "₦"))
            db.settingDao().set(SettingEntity("daily_goal", "150000"))
            db.settingDao().set(SettingEntity("user_name", "Admin"))
            db.settingDao().set(SettingEntity("sound", "true"))

            // Seed Customers
            val c1 = db.customerDao().insert(
                CustomerEntity(name = "Alhaji Musa Kano", phone = "08031234567", address = "Kasuwar Kurmi, Kano", rating = 5)
            )
            val c2 = db.customerDao().insert(
                CustomerEntity(name = "Hajiya Fatima Bello", phone = "08029876543", address = "Wuse Zone 4, Abuja", rating = 5)
            )
            val c3 = db.customerDao().insert(
                CustomerEntity(name = "Malam Sani Danladi", phone = "08145556677", address = "Central Market, Kaduna", rating = 4)
            )
            val c4 = db.customerDao().insert(
                CustomerEntity(name = "Zainab Umar", phone = "07061122334", address = "Sabon Gari, Zaria", rating = 4)
            )
            val c5 = db.customerDao().insert(
                CustomerEntity(name = "Ibrahim Garba", phone = "09088776655", address = "Kasuwar Dawanau", rating = 5)
            )

            // Seed Products
            val p1 = db.productDao().insert(
                ProductEntity(
                    name = "Shinkafa (Rice) 50kg",
                    category = "Abinci",
                    barcode = "BC881029",
                    costPrice = 62000.0,
                    sellPrice = 72000.0,
                    quantity = 28,
                    minStock = 5
                )
            )
            val p2 = db.productDao().insert(
                ProductEntity(
                    name = "Sukari (Sugar) 50kg",
                    category = "Abinci",
                    barcode = "BC449012",
                    costPrice = 75000.0,
                    sellPrice = 85000.0,
                    quantity = 15,
                    minStock = 4
                )
            )
            val p3 = db.productDao().insert(
                ProductEntity(
                    name = "Mai na Girki (Vegetable Oil) 25L",
                    category = "Abinci",
                    barcode = "BC902184",
                    costPrice = 48000.0,
                    sellPrice = 55000.0,
                    quantity = 20,
                    minStock = 5
                )
            )
            val p4 = db.productDao().insert(
                ProductEntity(
                    name = "Taliya Carton (Pasta x20)",
                    category = "Abinci",
                    barcode = "BC123987",
                    costPrice = 11000.0,
                    sellPrice = 13500.0,
                    quantity = 45,
                    minStock = 10
                )
            )
            val p5 = db.productDao().insert(
                ProductEntity(
                    name = "Sabulun Wanki (Detergent 2kg)",
                    category = "Kayan Gida",
                    barcode = "BC665544",
                    costPrice = 2800.0,
                    sellPrice = 3500.0,
                    quantity = 3, // Low stock on purpose to show alert!
                    minStock = 6
                )
            )
            val p6 = db.productDao().insert(
                ProductEntity(
                    name = "Madara (Powdered Milk 1kg)",
                    category = "Abinci",
                    barcode = "BC334411",
                    costPrice = 6500.0,
                    sellPrice = 8000.0,
                    quantity = 2, // Low stock on purpose
                    minStock = 5
                )
            )
            val p7 = db.productDao().insert(
                ProductEntity(
                    name = "Siminti (Dangote Cement 50kg)",
                    category = "Kayan Gine-gine",
                    barcode = "BC778899",
                    costPrice = 8500.0,
                    sellPrice = 9800.0,
                    quantity = 120,
                    minStock = 20
                )
            )

            // Seed Initial Sales
            val now = System.currentTimeMillis()
            val oneDay = 24 * 60 * 60 * 1000L

            db.saleDao().insert(
                SaleEntity(
                    customerId = c1,
                    productId = p1,
                    customerName = "Alhaji Musa Kano",
                    productName = "Shinkafa (Rice) 50kg",
                    quantity = 2,
                    unitPrice = 72000.0,
                    total = 144000.0,
                    profit = 20000.0,
                    saleDate = now - (oneDay * 0)
                )
            )
            db.saleDao().insert(
                SaleEntity(
                    customerId = c2,
                    productId = p3,
                    customerName = "Hajiya Fatima Bello",
                    productName = "Mai na Girki (Vegetable Oil) 25L",
                    quantity = 1,
                    unitPrice = 55000.0,
                    total = 55000.0,
                    profit = 7000.0,
                    saleDate = now - (oneDay * 1)
                )
            )
            db.saleDao().insert(
                SaleEntity(
                    customerId = c3,
                    productId = p4,
                    customerName = "Malam Sani Danladi",
                    productName = "Taliya Carton (Pasta x20)",
                    quantity = 3,
                    unitPrice = 13500.0,
                    total = 40500.0,
                    profit = 7500.0,
                    saleDate = now - (oneDay * 2)
                )
            )
            db.saleDao().insert(
                SaleEntity(
                    customerId = c4,
                    productId = p2,
                    customerName = "Zainab Umar",
                    productName = "Sukari (Sugar) 50kg",
                    quantity = 1,
                    unitPrice = 85000.0,
                    total = 85000.0,
                    profit = 10000.0,
                    saleDate = now - (oneDay * 3)
                )
            )

            // Unlock initial achievement
            db.achievementDao().insert(AchievementEntity(key = "first_sale"))
            db.achievementDao().insert(AchievementEntity(key = "customer_5"))
            db.achievementDao().insert(AchievementEntity(key = "product_10"))
        }
    }
}
