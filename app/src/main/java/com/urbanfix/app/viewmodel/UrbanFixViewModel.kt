package com.urbanfix.app.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.urbanfix.app.data.ApiResult
import com.urbanfix.app.data.Category
import com.urbanfix.app.data.MapProjection
import com.urbanfix.app.data.PersistentTicketRepository
import com.urbanfix.app.data.Ticket
import com.urbanfix.app.data.TicketRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class AppTab { MAP, REPORT, TICKETS }

enum class UiMessage {
    CONFIRMED, LOCATION_FROM_MAP, LOCATION_FROM_GPS, GPS_DENIED, GPS_UNAVAILABLE, GPS_OUTSIDE_MAP,
    SYNC_SUCCESS, SYNC_OFFLINE, CAMERA_DENIED_FALLBACK, DEMO_RESET
}

fun searchTickets(tickets: List<Ticket>, query: String, categoryLabel: (Category) -> String): List<Ticket> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    return tickets.filter {
        it.location.contains(q, ignoreCase = true) ||
                it.description.contains(q, ignoreCase = true) ||
                categoryLabel(it.category).contains(q, ignoreCase = true) ||
                it.id.contains(q, ignoreCase = true)
    }
}

class UrbanFixViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: TicketRepository = PersistentTicketRepository(application)
) : AndroidViewModel(application) {

    var darkOverride by mutableStateOf<Boolean?>(null)
    var largeText by mutableStateOf(false)
    var isPolish by mutableStateOf(false)

    var tab by mutableStateOf(AppTab.MAP); private set
    var openTicketId by mutableStateOf<String?>(null); private set
    var focusedPinId by mutableStateOf<String?>(null); private set
    var isPickingLocation by mutableStateOf(false); private set

    val tickets: StateFlow<List<Ticket>> = repository.tickets

    val draft = ReportDraft()
    var gpsLoading by mutableStateOf(false); private set
    var gpsWarning by mutableStateOf<UiMessage?>(null); private set

    var quickReportAt by mutableStateOf<Pair<Float, Float>?>(null); private set
    val quickDraft = ReportDraft()

    // Dialog asking user if they want to proceed without photo
    var promptPhotoWarningForDraft by mutableStateOf(false)
    var promptPhotoWarningForQuick by mutableStateOf(false)

    var lastSubmitted by mutableStateOf<Ticket?>(null); private set
    var message by mutableStateOf<UiMessage?>(null); private set
    var isSyncing by mutableStateOf(false); private set

    var searchQuery by mutableStateOf(""); private set

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        registerAutomaticNetworkSync(application)
    }

    private fun registerAutomaticNetworkSync(context: Context) {
        runCatching {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    viewModelScope.launch {
                        // Brief delay so DNS/routing stabilizes right after leaving Airplane Mode
                        delay(800L)
                        if (!isSyncing && tickets.value.any { !it.isSyncedWithApi }) {
                            isSyncing = true
                            val res = repository.syncPendingWithApi()
                            isSyncing = false
                            if (res is ApiResult.Success) {
                                message = UiMessage.SYNC_SUCCESS
                            }
                        }
                    }
                }
            }
            cm.registerNetworkCallback(request, callback)
            connectivityManager = cm
            networkCallback = callback
        }
    }

    override fun onCleared() {
        super.onCleared()
        runCatching {
            val cm = connectivityManager
            val cb = networkCallback
            if (cm != null && cb != null) {
                cm.unregisterNetworkCallback(cb)
            }
        }
    }

    fun selectTab(t: AppTab) {
        tab = t
        isPickingLocation = false
        if (t != AppTab.TICKETS) openTicketId = null
    }

    fun openTicket(id: String) { openTicketId = id; tab = AppTab.TICKETS }
    fun closeTicket() { openTicketId = null }

    fun showOnMap(id: String) {
        openTicketId = null
        searchQuery = ""
        focusedPinId = id
        tab = AppTab.MAP
    }

    fun openPin(id: String) { focusedPinId = id; searchQuery = "" }
    fun dismissPin() { focusedPinId = null }

    fun updateSearch(q: String) { searchQuery = q }

    fun startPickingLocation() { isPickingLocation = true; tab = AppTab.MAP }
    fun cancelPickingLocation() { isPickingLocation = false; tab = AppTab.REPORT }

    fun onMapTapped(x: Float, y: Float) {
        val (lat, lng) = MapProjection.toLatLng(x, y)
        val label = "📍 " + MapProjection.format(lat, lng)
        if (isPickingLocation) {
            draft.setLocationWithPosition(label, x, y)
            gpsWarning = null
            isPickingLocation = false
            tab = AppTab.REPORT
            message = UiMessage.LOCATION_FROM_MAP
        } else {
            quickDraft.reset()
            quickDraft.setLocationWithPosition(label, x, y)
            quickReportAt = x to y
        }
    }

    fun dismissQuickReport() { quickReportAt = null }

    fun onGpsRequested() {
        gpsLoading = true
        gpsWarning = null
    }

    fun onGpsPermissionDenied() {
        gpsLoading = false
        gpsWarning = UiMessage.GPS_DENIED
        message = UiMessage.GPS_DENIED
    }

    fun onGpsResult(latLng: Pair<Double, Double>?) {
        gpsLoading = false
        if (latLng == null) {
            gpsWarning = UiMessage.GPS_UNAVAILABLE
            message = UiMessage.GPS_UNAVAILABLE
            return
        }
        gpsWarning = null
        val (lat, lng) = latLng
        val (x, y) = MapProjection.toMap(lat, lng)
        draft.setLocationWithPosition("GPS: " + MapProjection.format(lat, lng), x, y)
        message = if (MapProjection.isInsideMap(lat, lng)) UiMessage.LOCATION_FROM_GPS else UiMessage.GPS_OUTSIDE_MAP
    }

    fun clearGpsWarning() { gpsWarning = null }

    fun onCameraPermissionDenied() {
        message = UiMessage.CAMERA_DENIED_FALLBACK
    }

    // Submit with photo suggestion pop-up check
    fun trySubmitReport(): Set<DraftError> {
        val errors = draft.validate()
        if (errors.isEmpty()) {
            if (draft.photoUri == null) {
                promptPhotoWarningForDraft = true
            } else {
                doSubmit(draft)
            }
        }
        return errors
    }

    fun proceedSubmitWithoutPhoto() {
        promptPhotoWarningForDraft = false
        doSubmit(draft)
    }

    fun trySubmitQuickReport(): Set<DraftError> {
        val errors = quickDraft.validate()
        if (errors.isEmpty()) {
            if (quickDraft.photoUri == null) {
                promptPhotoWarningForQuick = true
            } else {
                doSubmit(quickDraft)
                quickReportAt = null
            }
        }
        return errors
    }

    fun proceedQuickSubmitWithoutPhoto() {
        promptPhotoWarningForQuick = false
        doSubmit(quickDraft)
        quickReportAt = null
    }

    private fun doSubmit(d: ReportDraft) {
        viewModelScope.launch {
            lastSubmitted = repository.add(d.toNewReport())
            gpsWarning = null
            d.reset()
        }
    }

    fun dismissSuccess() { lastSubmitted = null }

    fun goToTrackerFromSuccess() {
        lastSubmitted = null
        openTicketId = null
        tab = AppTab.TICKETS
    }

    fun confirm(id: String) {
        val target = repository.find(id)
        if (target == null || target.isOwnReport) return
        viewModelScope.launch {
            repository.confirm(id, System.currentTimeMillis())
            message = UiMessage.CONFIRMED
        }
    }

    fun triggerApiSync() {
        viewModelScope.launch {
            isSyncing = true
            val res = repository.syncPendingWithApi()
            isSyncing = false
            message = if (res is ApiResult.Success) UiMessage.SYNC_SUCCESS else UiMessage.SYNC_OFFLINE
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
            openTicketId = null
            focusedPinId = null
            message = UiMessage.DEMO_RESET
        }
    }

    fun consumeMessage() { message = null }
}