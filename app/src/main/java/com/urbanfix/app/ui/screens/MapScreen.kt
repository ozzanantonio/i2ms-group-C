package com.urbanfix.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.urbanfix.app.ui.components.*
import com.urbanfix.app.ui.i18n.Translations
import com.urbanfix.app.ui.theme.GreenPrimary
import com.urbanfix.app.viewmodel.UrbanFixViewModel
import com.urbanfix.app.viewmodel.searchTickets
import kotlinx.coroutines.delay

@Composable
fun MapScreen(vm: UrbanFixViewModel, s: Translations) {
    val tickets by vm.tickets.collectAsState()
    val results = searchTickets(tickets, vm.searchQuery, s::label)
    val cs = MaterialTheme.colorScheme

    // Show tapped pin immediately on the map, then open Quick Report dialog after 500ms
    var showQuickDialog by remember { mutableStateOf(false) }
    val pendingPin = vm.quickReportAt
    LaunchedEffect(pendingPin) {
        if (pendingPin != null) {
            showQuickDialog = false
            delay(500L)
            showQuickDialog = true
        } else {
            showQuickDialog = false
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = vm.searchQuery, onValueChange = vm::updateSearch,
            placeholder = { Text(s.searchMapHint) },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = if (vm.searchQuery.isNotEmpty()) {
                { IconButton(onClick = { vm.updateSearch("") }) { Icon(Icons.Filled.Close, s.clearSearch) } }
            } else null,
            singleLine = true,
            enabled = !vm.isPickingLocation,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(8.dp))

        if (vm.isPickingLocation) {
            InfoBanner(s.pickLocationBanner, icon = Icons.Outlined.TouchApp) {
                TextButton(onClick = vm::cancelPickingLocation) { Text(s.cancel) }
            }
        } else if (vm.searchQuery.isBlank()) {
            InfoBanner(s.tapMapHint, icon = Icons.Outlined.TouchApp)
        }
        Spacer(Modifier.height(8.dp))

        Box(Modifier.fillMaxWidth().weight(1f)) {
            BoxWithConstraints(
                Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { o -> vm.onMapTapped(o.x / size.width, o.y / size.height) }
                    }
            ) {
                SimulatedCityMap()
                tickets.forEach { t ->
                    val highlighted = vm.searchQuery.isNotBlank() && t in results
                    Box(
                        Modifier
                            .offset(x = maxWidth * t.mapX - 22.dp, y = maxHeight * t.mapY - 48.dp)
                            .size(44.dp, 48.dp)
                            .then(
                                if (vm.isPickingLocation) Modifier
                                else Modifier.clickable(role = Role.Button, onClickLabel = s.openDetails) { vm.openPin(t.id) }
                            )
                            .semantics { contentDescription = "${s.label(t.category)}: ${t.location}" },
                        contentAlignment = Alignment.BottomCenter
                    ) { PinIcon(highlighted) }
                }

                // Immediate marker feedback at the tapped coordinates before Quick Report opens
                pendingPin?.let { (px, py) ->
                    Box(
                        Modifier
                            .offset(x = maxWidth * px - 22.dp, y = maxHeight * py - 48.dp)
                            .size(44.dp, 48.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) { PinIcon(highlighted = true) }
                }
            }

            if (vm.searchQuery.isNotBlank()) {
                Card(
                    Modifier.fillMaxWidth().heightIn(max = 260.dp).padding(4.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(cs.surface)
                ) {
                    if (results.isEmpty()) {
                        Text(s.noResults, Modifier.padding(16.dp), color = cs.onSurfaceVariant)
                    } else {
                        LazyColumn {
                            items(results, key = { it.id }) { t ->
                                Row(
                                    Modifier.fillMaxWidth().clickable(role = Role.Button) { vm.openPin(t.id) }.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(t.category.emoji, fontSize = 20.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(t.location, fontWeight = FontWeight.SemiBold)
                                        Text("${s.label(t.category)} · ${t.id}", fontSize = 12.sp, color = cs.onSurfaceVariant)
                                    }
                                }
                                HorizontalDivider(color = cs.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    val focused = tickets.firstOrNull { it.id == vm.focusedPinId }
    if (focused != null) {
        Dialog(onDismissRequest = vm::dismissPin) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(cs.surface)) {
                Column(Modifier.padding(16.dp)) {
                    TicketImage(focused.photoUri, focused.category, s.photoPreview, Modifier.fillMaxWidth().height(120.dp))
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(s.label(focused.category), color = cs.primary, fontWeight = FontWeight.Bold)
                        if (focused.isOwnReport) {
                            SemanticBadge(s.yourReportBadge, BadgeKind.YOUR_REPORT)
                        } else if (focused.isTestData) {
                            SemanticBadge(s.testDataBadge, BadgeKind.TEST_DATA)
                        }
                    }
                    Text(focused.location, fontSize = 12.sp, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(focused.description.ifBlank { s.noDescription }, maxLines = 3)
                    Spacer(Modifier.height(16.dp))
                    if (focused.isOwnReport) {
                        Text(
                            if (focused.isConfirmed) "✅ ${s.yourReportConfirmed}" else "👤 ${s.yourReportWaiting}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = cs.secondary
                        )
                    } else {
                        ConfirmButton(focused.isConfirmed, s, { vm.confirm(focused.id) }, Modifier.fillMaxWidth())
                    }
                    TextButton(onClick = { vm.dismissPin(); vm.openTicket(focused.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text(s.openDetails)
                    }
                }
            }
        }
    }

    if (showQuickDialog && pendingPin != null) {
        Dialog(onDismissRequest = vm::dismissQuickReport) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(cs.surface)) {
                Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(s.quickReportTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = vm::dismissQuickReport) { Icon(Icons.Filled.Close, s.close) }
                    }
                    ReportForm(
                        draft = vm.quickDraft,
                        s = s,
                        showLocation = false,
                        onSubmit = vm::trySubmitQuickReport
                    )
                }
            }
        }
    }

    // Modal when user is about to submit quick report without photo
    if (vm.promptPhotoWarningForQuick) {
        AlertDialog(
            onDismissRequest = { vm.promptPhotoWarningForQuick = false },
            title = { Text(s.photoAdvisedTitle) },
            text = { Text(s.photoAdvisedBody) },
            confirmButton = {
                Button(
                    onClick = { vm.proceedQuickSubmitWithoutPhoto() },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) { Text(s.submitAnyway) }
            },
            dismissButton = {
                TextButton(onClick = { vm.promptPhotoWarningForQuick = false }) { Text(s.addPhotoNow) }
            }
        )
    }
}