package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DashboardSummary
import com.example.data.DaySalesPoint
import com.example.data.KasuwanciDatabase
import com.example.data.KasuwanciRepository
import com.example.data.dao.TopCustomerSummary
import com.example.data.dao.TopProductSummary
import com.example.data.model.AchievementEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.UserEntity
import com.example.util.SoundUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    DASHBOARD,
    SALES,
    PRODUCTS,
    CUSTOMERS,
    REPORTS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = KasuwanciDatabase.getInstance(application)
    private val repository = KasuwanciRepository(db)

    // Current User
    private val _currentUser = MutableStateFlow<UserEntity?>(
        UserEntity(id = 1, username = "Admin", passwordHash = "", role = "admin")
    )
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Settings
    private val _language = MutableStateFlow("ha")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _theme = MutableStateFlow("neon")
    val theme: StateFlow<String> = _theme.asStateFlow()

    private val _currency = MutableStateFlow("₦")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _dailyGoal = MutableStateFlow(150000.0)
    val dailyGoal: StateFlow<Double> = _dailyGoal.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    // Data Flows
    val products: StateFlow<List<ProductEntity>> = repository.productsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.customersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSales: StateFlow<List<SaleEntity>> = repository.recentSalesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard & Analytics
    private val _dashboardSummary = MutableStateFlow(
        DashboardSummary(0, 0.0, 0.0, 0, 0.0, 0.0, 0, 0, 0)
    )
    val dashboardSummary: StateFlow<DashboardSummary> = _dashboardSummary.asStateFlow()

    private val _weeklySales = MutableStateFlow<List<DaySalesPoint>>(emptyList())
    val weeklySales: StateFlow<List<DaySalesPoint>> = _weeklySales.asStateFlow()

    private val _topProducts = MutableStateFlow<List<TopProductSummary>>(emptyList())
    val topProducts: StateFlow<List<TopProductSummary>> = _topProducts.asStateFlow()

    private val _topCustomers = MutableStateFlow<List<TopCustomerSummary>>(emptyList())
    val topCustomers: StateFlow<List<TopCustomerSummary>> = _topCustomers.asStateFlow()

    // Active Digital Invoice Dialog
    private val _activeInvoice = MutableStateFlow<SaleEntity?>(null)
    val activeInvoice: StateFlow<SaleEntity?> = _activeInvoice.asStateFlow()

    // Celebration modal/toast
    private val _celebrationNotice = MutableStateFlow<String?>(null)
    val celebrationNotice: StateFlow<String?> = _celebrationNotice.asStateFlow()

    // Error message snack
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadSettings()
        refreshAnalytics()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repository.getSetting("language")?.let { _language.value = it }
            repository.getSetting("theme")?.let { _theme.value = it }
            repository.getSetting("currency")?.let { _currency.value = it }
            repository.getSetting("daily_goal")?.toDoubleOrNull()?.let { _dailyGoal.value = it }
            repository.getSetting("sound")?.toBooleanStrictOrNull()?.let { _soundEnabled.value = it }
        }
    }

    fun refreshAnalytics() {
        viewModelScope.launch {
            _dashboardSummary.value = repository.getDashboardSummary()
            _weeklySales.value = repository.getWeeklySalesTrend()
            _topProducts.value = repository.getTopProducts()
            _topCustomers.value = repository.getTopCustomers()
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
        if (tab == AppTab.DASHBOARD || tab == AppTab.REPORTS) {
            refreshAnalytics()
        }
    }

    fun login(username: String, passwordRaw: String, context: Context): Boolean {
        if (username.equals("admin", ignoreCase = true) && passwordRaw == "admin123") {
            _currentUser.value = UserEntity(id = 1, username = "Admin", passwordHash = "", role = "admin")
            _isLoggedIn.value = true
            SoundUtil.playFeedback(context, "success", _soundEnabled.value)
            refreshAnalytics()
            return true
        }

        viewModelScope.launch {
            val user = repository.login(username, passwordRaw)
            if (user != null) {
                _currentUser.value = user
                _isLoggedIn.value = true
                SoundUtil.playFeedback(context, "success", _soundEnabled.value)
                refreshAnalytics()
            } else {
                SoundUtil.playFeedback(context, "error", _soundEnabled.value)
                _toastMessage.value = if (_language.value == "ha") "Sunan mai amfani ko kalmar sirri ba daidai ba!" else "Invalid username or password!"
            }
        }
        return false
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun makeSale(customerId: Long, productId: Long, quantity: Int, context: Context) {
        viewModelScope.launch {
            val sale = repository.recordSale(customerId, productId, quantity)
            if (sale != null) {
                _activeInvoice.value = sale
                SoundUtil.playFeedback(context, "celebrate", _soundEnabled.value)
                _celebrationNotice.value = if (_language.value == "ha") "CINIKI YA CIM MA! 🎉" else "SALE COMPLETED! 🎉"
                refreshAnalytics()
            } else {
                SoundUtil.playFeedback(context, "error", _soundEnabled.value)
                _toastMessage.value = if (_language.value == "ha") "Ajiya ba ta isa ba ko kuskure ya faru!" else "Insufficient stock or invalid input!"
            }
        }
    }

    fun closeInvoice() {
        _activeInvoice.value = null
    }

    fun dismissCelebration() {
        _celebrationNotice.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun addProduct(
        name: String,
        category: String,
        barcode: String,
        cost: Double,
        sell: Double,
        qty: Int,
        minStock: Int,
        context: Context
    ) {
        viewModelScope.launch {
            val finalBarcode = if (barcode.isBlank()) "BC${(100000..999999).random()}" else barcode
            val product = ProductEntity(
                name = name,
                category = if (category.isBlank()) "General" else category,
                barcode = finalBarcode,
                costPrice = cost,
                sellPrice = sell,
                quantity = qty,
                minStock = minStock
            )
            repository.addProduct(product)
            SoundUtil.playFeedback(context, "success", _soundEnabled.value)
            _toastMessage.value = if (_language.value == "ha") "An ƙara $name cikin nasara!" else "Added $name successfully!"
            refreshAnalytics()
        }
    }

    fun updateProduct(product: ProductEntity, context: Context) {
        viewModelScope.launch {
            repository.updateProduct(product)
            SoundUtil.playFeedback(context, "success", _soundEnabled.value)
            refreshAnalytics()
        }
    }

    fun restockProduct(id: Long, qty: Int, context: Context) {
        viewModelScope.launch {
            repository.restockProduct(id, qty)
            SoundUtil.playFeedback(context, "success", _soundEnabled.value)
            _toastMessage.value = if (_language.value == "ha") "An ƙara ajiya!" else "Restocked successfully!"
            refreshAnalytics()
        }
    }

    fun deleteProduct(product: ProductEntity, context: Context) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            SoundUtil.playFeedback(context, "click", _soundEnabled.value)
            refreshAnalytics()
        }
    }

    fun addCustomer(
        name: String,
        phone: String,
        address: String,
        rating: Int,
        context: Context
    ) {
        viewModelScope.launch {
            val customer = CustomerEntity(
                name = name,
                phone = phone,
                address = address,
                rating = rating.coerceIn(1, 5)
            )
            repository.addCustomer(customer)
            SoundUtil.playFeedback(context, "success", _soundEnabled.value)
            _toastMessage.value = if (_language.value == "ha") "An ƙara mai kaya $name!" else "Customer $name registered!"
            refreshAnalytics()
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            refreshAnalytics()
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            refreshAnalytics()
        }
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        viewModelScope.launch { repository.saveSetting("language", lang) }
    }

    fun setTheme(theme: String) {
        _theme.value = theme
        viewModelScope.launch { repository.saveSetting("theme", theme) }
    }

    fun setCurrency(currency: String) {
        _currency.value = currency
        viewModelScope.launch { repository.saveSetting("currency", currency) }
    }

    fun setDailyGoal(goal: Double) {
        _dailyGoal.value = goal
        viewModelScope.launch { repository.saveSetting("daily_goal", goal.toString()) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        viewModelScope.launch { repository.saveSetting("sound", enabled.toString()) }
    }
}
