package com.example.data

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class DashboardSummary(
    val todaySalesCount: Int,
    val todayRevenue: Double,
    val todayProfit: Double,
    val monthSalesCount: Int,
    val monthRevenue: Double,
    val monthProfit: Double,
    val totalCustomers: Int,
    val totalProducts: Int,
    val totalStockUnits: Int
)

data class DaySalesPoint(
    val dayLabel: String,
    val amount: Double
)

class KasuwanciRepository(private val db: KasuwanciDatabase) {
    val customersFlow: Flow<List<CustomerEntity>> = db.customerDao().getAllFlow()
    val productsFlow: Flow<List<ProductEntity>> = db.productDao().getAllFlow()
    val lowStockFlow: Flow<List<ProductEntity>> = db.productDao().getLowStockFlow()
    val recentSalesFlow: Flow<List<SaleEntity>> = db.saleDao().getRecentFlow(50)
    val achievementsFlow: Flow<List<AchievementEntity>> = db.achievementDao().getAllFlow()
    val settingsFlow: Flow<List<SettingEntity>> = db.settingDao().getAllFlow()

    suspend fun login(username: String, passwordRaw: String): UserEntity? {
        val hash = KasuwanciDatabase.sha256(passwordRaw)
        val user = db.userDao().getByUsername(username)
        return if (user != null && user.passwordHash == hash) user else null
    }

    suspend fun addCustomer(customer: CustomerEntity): Long {
        val id = db.customerDao().insert(customer)
        checkAchievements()
        return id
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        db.customerDao().update(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        db.customerDao().delete(customer)
    }

    suspend fun addProduct(product: ProductEntity): Long {
        val id = db.productDao().insert(product)
        checkAchievements()
        return id
    }

    suspend fun updateProduct(product: ProductEntity) {
        db.productDao().update(product)
    }

    suspend fun restockProduct(id: Long, qty: Int) {
        db.productDao().addStock(id, qty)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        db.productDao().delete(product)
    }

    suspend fun recordSale(
        customerId: Long,
        productId: Long,
        quantity: Int
    ): SaleEntity? {
        val product = db.productDao().getById(productId) ?: return null
        val customer = db.customerDao().getById(customerId) ?: return null

        if (product.quantity < quantity || quantity <= 0) {
            return null
        }

        // Deduct stock
        val affected = db.productDao().deductStock(productId, quantity)
        if (affected <= 0) return null

        val total = quantity * product.sellPrice
        val profit = quantity * (product.sellPrice - product.costPrice)
        val sale = SaleEntity(
            customerId = customerId,
            productId = productId,
            customerName = customer.name,
            productName = product.name,
            quantity = quantity,
            unitPrice = product.sellPrice,
            total = total,
            profit = profit,
            saleDate = System.currentTimeMillis()
        )
        val saleId = db.saleDao().insert(sale)
        val savedSale = sale.copy(id = saleId)

        checkAchievements()
        return savedSale
    }

    suspend fun getDashboardSummary(): DashboardSummary {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = cal.timeInMillis

        val todaySales = db.saleDao().getSalesSince(startOfToday)
        val monthSales = db.saleDao().getSalesSince(startOfMonth)

        val todayTotal = todaySales.sumOf { it.total }
        val todayProfit = todaySales.sumOf { it.profit }

        val monthTotal = monthSales.sumOf { it.total }
        val monthProfit = monthSales.sumOf { it.profit }

        val customers = db.customerDao().getAll().size
        val products = db.productDao().getAll()
        val totalUnits = products.sumOf { it.quantity }

        return DashboardSummary(
            todaySalesCount = todaySales.size,
            todayRevenue = todayTotal,
            todayProfit = todayProfit,
            monthSalesCount = monthSales.size,
            monthRevenue = monthTotal,
            monthProfit = monthProfit,
            totalCustomers = customers,
            totalProducts = products.size,
            totalStockUnits = totalUnits
        )
    }

    suspend fun getWeeklySalesTrend(): List<DaySalesPoint> {
        val result = mutableListOf<DaySalesPoint>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val dayNamesHa = arrayOf("Lah", "Lit", "Tal", "Lar", "Alh", "Jum", "Asa")

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.timeInMillis = cal.timeInMillis - (i * 24 * 60 * 60 * 1000L)
            val startDay = dayCal.timeInMillis
            val endDay = startDay + 24 * 60 * 60 * 1000L

            val sales = db.saleDao().getSalesSince(startDay).filter { it.saleDate < endDay }
            val sum = sales.sumOf { it.total }
            val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK) - 1
            result.add(DaySalesPoint(dayNamesHa[dayOfWeek], sum))
        }
        return result
    }

    suspend fun getTopProducts(): List<TopProductSummary> = db.saleDao().getTopProducts(10)
    suspend fun getTopCustomers(): List<TopCustomerSummary> = db.saleDao().getTopCustomers(10)

    suspend fun getSetting(key: String): String? = db.settingDao().get(key)
    suspend fun saveSetting(key: String, value: String) = db.settingDao().set(SettingEntity(key, value))

    suspend fun checkAchievements(): List<String> {
        val newlyUnlocked = mutableListOf<String>()
        val sales = db.saleDao().getAllFlow() // or count
        // Check simple metrics
        val summary = getDashboardSummary()
        val allProducts = db.productDao().getAll()
        val allCustomers = db.customerDao().getAll()

        suspend fun tryUnlock(key: String) {
            if (db.achievementDao().hasKey(key) == 0) {
                db.achievementDao().insert(AchievementEntity(key = key))
                newlyUnlocked.add(key)
            }
        }

        if (summary.monthSalesCount >= 1 || summary.todaySalesCount >= 1) tryUnlock("first_sale")
        if (summary.monthSalesCount >= 10) tryUnlock("sale_10")
        if (summary.monthSalesCount >= 100) tryUnlock("sale_100")
        if (allCustomers.size >= 5) tryUnlock("customer_5")
        if (allProducts.size >= 10) tryUnlock("product_10")
        if (summary.monthProfit >= 1_000_000.0) tryUnlock("profit_1m")

        return newlyUnlocked
    }
}
