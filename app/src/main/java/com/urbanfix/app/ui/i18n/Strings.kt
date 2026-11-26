package com.urbanfix.app.ui.i18n

import com.urbanfix.app.data.Category
import com.urbanfix.app.viewmodel.UiMessage
import java.util.Locale

data class Translations(
    val locale: Locale = Locale.ENGLISH,
    val appName: String = "UrbanFix",
    val mapTab: String = "Home",
    val reportTab: String = "Report",
    val trackerTab: String = "Tracker",
    val searchMapHint: String = "Search a street, issue or ID...",
    val clearSearch: String = "Clear search",
    val noResults: String = "No reports match your search.",
    val tapMapHint: String = "Tap anywhere on the map to report a problem there.",
    val pickLocationBanner: String = "Tap the map to choose the location of the problem",
    val cancel: String = "Cancel",
    val quickReportTitle: String = "Quick Report",
    val quickReportLocation: String = "Location: map pin",
    val location: String = "Location",
    val locationHint: String = "ul. Piotrkowska, 42",
    val locationRequired: String = "Please choose or type a location.",
    val category: String = "Category",
    val description: String = "Description (optional)",
    val descriptionHint: String = "What is the problem? e.g. deep pothole near the bus stop",
    val descriptionTooLong: String = "Description is too long.",
    val submit: String = "Submit Report",
    val photoEv: String = "Photo Evidence (Optional)",
    val tapToAttach: String = "Take a photo or pick from gallery (Optional)",
    val takePhoto: String = "Take photo",
    val chooseGallery: String = "Gallery",
    val changePhoto: String = "Change photo",
    val removePhoto: String = "Remove photo",
    val photoPreview: String = "Attached photo of the problem",
    val photoAdvisedTitle: String = "Photo Recommended",
    val photoAdvisedBody: String = "Adding a photo makes it much easier for the city to verify and fix the issue. Would you like to add one or submit anyway?",
    val submitAnyway: String = "Submit without photo",
    val addPhotoNow: String = "Add photo now",
    val fixErrors: String = "Please fix the highlighted fields.",
    val currentGps: String = "Current GPS",
    val gpsLoading: String = "Locating…",
    val mapBtn: String = "Pick on map",
    val successTitle: String = "Thank You, Citizen!",
    val successBody: String = "Saved locally in SQLite and synced with the test City API.",
    val successOfflineBody: String = "Offline fallback active: report saved locally in SQLite and will auto-sync when online.",
    val ticketId: String = "Ticket ID",
    val goToTracker: String = "Go to Tracker",
    val close: String = "Close",
    val iSawThisToo: String = "I saw this too / Confirm",
    val confirmed: String = "Confirmed",
    val yourReportBadge: String = "YOUR REPORT",
    val yourReportWaiting: String = "Your report · Awaiting confirmation",
    val yourReportConfirmed: String = "Your report · Confirmed by community",
    val autoSyncActive: String = "Auto-sync active",
    val cleanupPolicy: String = "Cleanup Policy: Unconfirmed issues older than 30 days are removed. Confirmations reset this timer.",
    val emptyTracker: String = "No reports yet. Be the first to report a problem!",
    val reportFirst: String = "Report a problem",
    val detailTitle: String = "Report details",
    val back: String = "Back",
    val status: String = "Status",
    val statusConfirmed: String = "Confirmed by the community",
    val statusWaiting: String = "Waiting for confirmation",
    val reportedOn: String = "Last activity / Reported",
    val noDescription: String = "No description",
    val noPhoto: String = "No photo attached",
    val showOnMap: String = "Show on map",
    val openDetails: String = "Open details",
    val syncStatusLocal: String = "Offline (Auto-sync queued)",
    val syncStatusSynced: String = "Synced (Cloud)",
    val testDataBadge: String = "TEST DATA",
    val syncButton: String = "Sync API",
    val syncing: String = "Auto-syncing…",
    val resetDemo: String = "Reset Demo",
    val catRoads: String = "Roads",
    val catWaste: String = "Waste",
    val catLights: String = "Lights",
    val catOther: String = "Other",
    val language: String = "Change language",
    val textSize: String = "Toggle large text",
    val theme: String = "Toggle dark mode",
    val msgConfirmed: String = "Confirmed! Reported date updated to today.",
    val msgLocationMap: String = "Location set from the map.",
    val msgLocationGps: String = "Location set from GPS.",
    val msgGpsDenied: String = "GPS permission denied. Type the street manually below or pick on the map.",
    val msgGpsUnavailable: String = "GPS signal unavailable on device. Type the street manually or pick on the map.",
    val msgGpsOutside: String = "You are outside the Łódź map area – the pin was placed at the map edge.",
    val msgSyncSuccess: String = "Connection restored: pending reports synced automatically.",
    val msgSyncOffline: String = "Offline: data safely preserved locally in database.",
    val msgCameraDeniedFallback: String = "Camera permission denied — opening Gallery fallback.",
    val msgDemoReset: String = "Demo data restored."
) {
    fun label(c: Category) = when (c) {
        Category.ROADS -> catRoads
        Category.WASTE -> catWaste
        Category.LIGHTS -> catLights
        Category.OTHER -> catOther
    }

    fun message(m: UiMessage) = when (m) {
        UiMessage.CONFIRMED -> msgConfirmed
        UiMessage.LOCATION_FROM_MAP -> msgLocationMap
        UiMessage.LOCATION_FROM_GPS -> msgLocationGps
        UiMessage.GPS_DENIED -> msgGpsDenied
        UiMessage.GPS_UNAVAILABLE -> msgGpsUnavailable
        UiMessage.GPS_OUTSIDE_MAP -> msgGpsOutside
        UiMessage.SYNC_SUCCESS -> msgSyncSuccess
        UiMessage.SYNC_OFFLINE -> msgSyncOffline
        UiMessage.CAMERA_DENIED_FALLBACK -> msgCameraDeniedFallback
        UiMessage.DEMO_RESET -> msgDemoReset
    }
}

val EnStrings = Translations()

val PlStrings = Translations(
    locale = Locale.forLanguageTag("pl-PL"),
    mapTab = "Start", reportTab = "Zgłoś", trackerTab = "Zgłoszenia",
    searchMapHint = "Szukaj ulicy, problemu lub numeru...",
    clearSearch = "Wyczyść wyszukiwanie",
    noResults = "Brak zgłoszeń pasujących do wyszukiwania.",
    tapMapHint = "Dotknij mapy, aby zgłosić problem w tym miejscu.",
    pickLocationBanner = "Dotknij mapy, aby wybrać miejsce problemu",
    cancel = "Anuluj",
    quickReportTitle = "Szybkie zgłoszenie", quickReportLocation = "Lokalizacja: pinezka na mapie",
    location = "Lokalizacja", locationRequired = "Wybierz lub wpisz lokalizację.",
    category = "Kategoria", description = "Opis (opcjonalnie)",
    descriptionHint = "Na czym polega problem? np. głęboka dziura przy przystanku",
    descriptionTooLong = "Opis jest za długi.",
    submit = "Wyślij zgłoszenie",
    photoEv = "Zdjęcie (opcjonalne)",
    tapToAttach = "Zrób zdjęcie lub wybierz z galerii (opcjonalne)",
    takePhoto = "Zrób zdjęcie",
    chooseGallery = "Galeria",
    changePhoto = "Zmień zdjęcie", removePhoto = "Usuń zdjęcie",
    photoPreview = "Załączone zdjęcie problemu",
    photoAdvisedTitle = "Zalecane zdjęcie",
    photoAdvisedBody = "Dodanie zdjęcia ułatwia miastu weryfikację zgłoszenia. Czy chcesz dodać zdjęcie, czy wysłać bez niego?",
    submitAnyway = "Wyślij bez zdjęcia", addPhotoNow = "Dodaj zdjęcie",
    fixErrors = "Popraw zaznaczone pola.",
    currentGps = "Obecny GPS", gpsLoading = "Lokalizowanie…", mapBtn = "Wybierz na mapie",
    successTitle = "Dziękujemy!",
    successBody = "Zapisano lokalnie w SQLite i zsynchronizowano z serwerem testowym.",
    successOfflineBody = "Tryb offline: zgłoszenie zapisano lokalnie w SQLite i zsynchronizuje się automatycznie po odzyskaniu sieci.",
    ticketId = "Numer zgłoszenia", goToTracker = "Przejdź do listy", close = "Zamknij",
    iSawThisToo = "Też to widziałem", confirmed = "Potwierdzone",
    yourReportBadge = "TWOJE ZGŁOSZENIE",
    yourReportWaiting = "Twoje zgłoszenie · Oczekuje na potwierdzenie",
    yourReportConfirmed = "Twoje zgłoszenie · Potwierdzone przez mieszkańców",
    autoSyncActive = "Auto-synchronizacja aktywna",
    cleanupPolicy = "Polityka czyszczenia: niepotwierdzone zgłoszenia po 30 dniach są usuwane. Potwierdzenie resetuje licznik.",
    emptyTracker = "Brak zgłoszeń. Zgłoś problem jako pierwszy!", reportFirst = "Zgłoś problem",
    detailTitle = "Szczegóły zgłoszenia", back = "Wstecz", status = "Status",
    statusConfirmed = "Potwierdzone przez mieszkańców", statusWaiting = "Oczekuje na potwierdzenie",
    reportedOn = "Ostatnia aktywność / Zgłoszono", noDescription = "Brak opisu", noPhoto = "Brak zdjęcia",
    showOnMap = "Pokaż na mapie", openDetails = "Otwórz szczegóły",
    syncStatusLocal = "Offline (W kolejce)",
    syncStatusSynced = "Zsynchronizowano (API)",
    testDataBadge = "DANE TESTOWE",
    syncButton = "Synchronizuj", syncing = "Synchronizacja…",
    resetDemo = "Resetuj Demo",
    catRoads = "Drogi", catWaste = "Odpady", catLights = "Oświetlenie", catOther = "Inne",
    language = "Zmień język", textSize = "Duży tekst", theme = "Tryb ciemny",
    msgConfirmed = "Potwierdzono! Data zgłoszenia została zaktualizowana na dzisiaj.",
    msgLocationMap = "Lokalizacja ustawiona z mapy.",
    msgLocationGps = "Lokalizacja ustawiona z GPS.",
    msgGpsDenied = "Brak zgody na GPS. Wpisz ulicę ręcznie poniżej lub wybierz na mapie.",
    msgGpsUnavailable = "Sygnał GPS niedostępny. Wpisz ulicę ręcznie lub wybierz na mapie.",
    msgGpsOutside = "Jesteś poza obszarem mapy Łodzi – pinezka została umieszczona na krawędzi mapy.",
    msgSyncSuccess = "Połączenie przywrócone: zgłoszenia zsynchronizowane automatycznie.",
    msgSyncOffline = "Tryb offline: dane bezpiecznie zapisane w lokalnej bazie.",
    msgCameraDeniedFallback = "Brak dostępu do aparatu — otwieram Galerię.",
    msgDemoReset = "Przywrócono dane demonstracyjne."
)