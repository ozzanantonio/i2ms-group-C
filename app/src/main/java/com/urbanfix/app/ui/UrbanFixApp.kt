package com.urbanfix.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.urbanfix.app.ui.components.SuccessDialog
import com.urbanfix.app.ui.i18n.EnStrings
import com.urbanfix.app.ui.i18n.PlStrings
import com.urbanfix.app.ui.screens.MapScreen
import com.urbanfix.app.ui.screens.ReportScreen
import com.urbanfix.app.ui.screens.TicketsScreen
import com.urbanfix.app.viewmodel.AppTab
import com.urbanfix.app.viewmodel.UrbanFixViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrbanFixApp(vm: UrbanFixViewModel, isDark: Boolean) {
    val s = if (vm.isPolish) PlStrings else EnStrings
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cs = MaterialTheme.colorScheme

    val message = vm.message
    LaunchedEffect(message) {
        if (message != null) {
            val text = s.message(message)
            vm.consumeMessage()
            scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar(text)
            }
        }
    }

    BackHandler(enabled = vm.isPickingLocation) { vm.cancelPickingLocation() }
    BackHandler(enabled = !vm.isPickingLocation && vm.tab != AppTab.MAP && vm.openTicketId == null) {
        vm.selectTab(AppTab.MAP)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, tint = cs.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(s.appName, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    TextButton(onClick = { vm.isPolish = !vm.isPolish },
                        modifier = Modifier.semantics { contentDescription = s.language }) {
                        Text(if (vm.isPolish) "PL" else "EN", color = cs.onSurface, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { vm.largeText = !vm.largeText },
                        modifier = Modifier.semantics { contentDescription = s.textSize }) {
                        Text("A+", fontWeight = FontWeight.Bold,
                            color = if (vm.largeText) cs.primary else cs.onSurface)
                    }
                    IconButton(onClick = { vm.darkOverride = !isDark }) {
                        Icon(if (isDark) Icons.Filled.WbSunny else Icons.Filled.NightsStay, s.theme)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = cs.surface) {
                val items = listOf(
                    Triple(AppTab.MAP, s.mapTab, Icons.Outlined.Map),
                    Triple(AppTab.REPORT, s.reportTab, Icons.Outlined.AddCircleOutline),
                    Triple(AppTab.TICKETS, s.trackerTab, Icons.AutoMirrored.Filled.FormatListBulleted)
                )
                items.forEach { (t, label, icon) ->
                    NavigationBarItem(
                        selected = vm.tab == t, onClick = { vm.selectTab(t) },
                        icon = { Icon(icon, null) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = cs.onPrimaryContainer,
                            selectedTextColor = cs.primary,
                            indicatorColor = cs.primaryContainer
                        )
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (vm.tab) {
                AppTab.MAP -> MapScreen(vm, s)
                AppTab.REPORT -> ReportScreen(vm, s)
                AppTab.TICKETS -> TicketsScreen(vm, s)
            }
        }
    }

    vm.lastSubmitted?.let { ticket ->
        SuccessDialog(ticket, s, onDismiss = vm::dismissSuccess, onGoToTracker = vm::goToTrackerFromSuccess)
    }
}