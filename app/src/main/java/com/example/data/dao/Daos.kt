package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

data class TopProductSummary(
    val productId: Long,
    val productName: String,
    val totalQty: Int,
    val totalSales: Double,
    val totalProfit: Double
)

data class TopCustomerSummary(
    val customerId: Long,
    val customerName: String,
    val salesCount: Int,
    val totalSpent: Double
)

data class CategorySalesSummary(
    val category: String,
    val totalAmount: Double
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllFlow(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAll(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CustomerEntity?

    @Query("SELECT COUNT(*) FROM customers")
    fun getCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    @Delete
    suspend fun delete(customer: CustomerEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAll(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE quantity <= minStock ORDER BY quantity ASC")
    fun getLowStockFlow(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    fun getCountFlow(): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM products")
    fun getTotalUnitsFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("UPDATE products SET quantity = quantity - :qty WHERE id = :id AND quantity >= :qty")
    suspend fun deductStock(id: Long, qty: Int): Int

    @Query("UPDATE products SET quantity = quantity + :qty WHERE id = :id")
    suspend fun addStock(id: Long, qty: Int): Int
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    fun getAllFlow(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales ORDER BY saleDate DESC LIMIT :limit")
    fun getRecentFlow(limit: Int = 30): Flow<List<SaleEntity>>

    @Query("SELECT COUNT(*) FROM sales")
    fun getCountFlow(): Flow<Int>

    @Query("SELECT COALESCE(SUM(profit), 0.0) FROM sales")
    fun getTotalProfitFlow(): Flow<Double>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM sales")
    fun getTotalRevenueFlow(): Flow<Double>

    @Query("SELECT * FROM sales WHERE saleDate >= :startTime ORDER BY saleDate DESC")
    suspend fun getSalesSince(startTime: Long): List<SaleEntity>

    @Query("""
        SELECT productId, productName, 
               SUM(quantity) AS totalQty, 
               SUM(total) AS totalSales, 
               SUM(profit) AS totalProfit 
        FROM sales 
        GROUP BY productId 
        ORDER BY totalQty DESC 
        LIMIT :limit
    """)
    suspend fun getTopProducts(limit: Int = 10): List<TopProductSummary>

    @Query("""
        SELECT customerId, customerName, 
               COUNT(id) AS salesCount, 
               SUM(total) AS totalSpent 
        FROM sales 
        GROUP BY customerId 
        ORDER BY totalSpent DESC 
        LIMIT :limit
    """)
    suspend fun getTopCustomers(limit: Int = 10): List<TopCustomerSummary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sale: SaleEntity): Long

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllFlow(): Flow<List<AchievementEntity>>

    @Query("SELECT COUNT(*) FROM achievements WHERE `key` = :key")
    suspend fun hasKey(key: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(achievement: AchievementEntity): Long
}

@Dao
interface SettingDao {
    @Query("SELECT value FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): String?

    @Query("SELECT * FROM settings")
    fun getAllFlow(): Flow<List<SettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(setting: SettingEntity)
}
