package com.urbanfix.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.urbanfix.app.data.Ticket
import com.urbanfix.app.ui.components.*
import com.urbanfix.app.ui.i18n.Translations
import com.urbanfix.app.ui.theme.GreenPrimary
import com.urbanfix.app.viewmodel.AppTab
import com.urbanfix.app.viewmodel.UrbanFixViewModel

@Composable
fun TicketsScreen(vm: UrbanFixViewModel, s: Translations) {
    val tickets by vm.tickets.collectAsState()
    val open = tickets.firstOrNull { it.id == vm.openTicketId }
    val cs = MaterialTheme.colorScheme

    if (open != null) {
        BackHandler(onBack = vm::closeTicket)
        TicketDetailScreen(open, s,
            onBack = vm::closeTicket,
            onConfirm = { vm.confirm(open.id) },
            onShowOnMap = { vm.showOnMap(open.id) })
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        InfoBanner(s.cleanupPolicy, Modifier.padding(bottom = 8.dp))

        // Automatic Sync status indicator + Reset Demo Data
        Row(
            Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (vm.isSyncing) {
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = cs.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(s.syncing, fontSize = 12.sp, color = cs.primary, fontWeight = FontWeight.Medium)
                } else {
                    Icon(Icons.Filled.Sync, null, Modifier.size(15.dp), tint = cs.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(s.autoSyncActive, fontSize = 12.sp, color = cs.onSurfaceVariant)
                }
            }
            OutlinedButton(
                onClick = vm::resetDemoData,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Filled.Refresh, null, Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(s.resetDemo, fontSize = 12.sp)
            }
        }

        if (tickets.isEmpty()) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text("📭", fontSize = 48.sp)
                Text(s.emptyTracker, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
                Button(onClick = { vm.selectTab(AppTab.REPORT) },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) { Text(s.reportFirst) }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(tickets, key = { it.id }) { t ->
                    TicketCard(t, s, onOpen = { vm.openTicket(t.id) }, onConfirm = { vm.confirm(t.id) })
                }
            }
        }
    }
}

@Composable
private fun TicketCard(t: Ticket, s: Translations, onOpen: () -> Unit, onConfirm: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val isDark = cs.background.luminanceIsLow()
    val cardBg = if (t.isOwnReport) {
        cs.secondaryContainer.copy(alpha = if (isDark) 0.35f else 0.45f)
    } else {
        cs.surface
    }
    val cardBorder = if (t.isOwnReport) {
        BorderStroke(1.5.dp, cs.secondary)
    } else {
        BorderStroke(1.dp, cs.outlineVariant)
    }

    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = cardBorder,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (t.isOwnReport) {
                        Modifier.drawBehind {
                            drawRect(cs.secondary, Offset.Zero, Size(4.dp.toPx(), size.height))
                        }
                    } else Modifier
                )
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            TicketImage(t.photoUri, t.category, null, Modifier.size(56.dp), emojiSize = 22)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(s.label(t.category), fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (t.isSyncedWithApi) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                            contentDescription = if (t.isSyncedWithApi) s.syncStatusSynced else s.syncStatusLocal,
                            tint = if (t.isSyncedWithApi) cs.primary else AmberWarningBorder,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(t.id, fontSize = 11.sp, color = cs.onSurfaceVariant)
                    }
                }
                Row(
                    Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (t.isOwnReport) {
                        SemanticBadge(s.yourReportBadge, BadgeKind.YOUR_REPORT)
                    }
                    if (t.isTestData) {
                        SemanticBadge(s.testDataBadge, BadgeKind.TEST_DATA)
                    } else if (t.isSyncedWithApi) {
                        SemanticBadge(s.syncStatusSynced, BadgeKind.SYNCED)
                    } else {
                        SemanticBadge(s.syncStatusLocal, BadgeKind.OFFLINE)
                    }
                }
                Text(t.location, fontSize = 12.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                Text(formatDate(t.createdAt, s), fontSize = 11.sp, color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp))

                if (t.isOwnReport) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Filled.Person, null, tint = cs.secondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (t.isConfirmed) s.yourReportConfirmed else s.yourReportWaiting,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = cs.secondary
                        )
                    }
                } else {
                    ConfirmButton(t.isConfirmed, s, onConfirm)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, s.openDetails, tint = cs.onSurfaceVariant)
        }
    }
}

@Composable
fun TicketDetailScreen(t: Ticket, s: Translations, onBack: () -> Unit, onConfirm: () -> Unit, onShowOnMap: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back) }
            Text(s.detailTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).semantics { heading() })
            Text(t.id, color = cs.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(end = 12.dp))
        }
        Column(Modifier.padding(horizontal = 16.dp)) {
            TicketImage(t.photoUri, t.category, s.photoPreview, Modifier.fillMaxWidth().height(220.dp), emojiSize = 64)
            if (t.photoUri == null) {
                Text(s.noPhoto, fontSize = 11.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${t.category.emoji}  ${s.label(t.category)}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                AssistChip(
                    onClick = {},
                    label = { Text(if (t.isSyncedWithApi) s.syncStatusSynced else s.syncStatusLocal, fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            if (t.isSyncedWithApi) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                            null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
            }
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (t.isOwnReport) {
                    SemanticBadge(s.yourReportBadge, BadgeKind.YOUR_REPORT)
                }
                if (t.isTestData) {
                    SemanticBadge(s.testDataBadge, BadgeKind.TEST_DATA)
                }
            }

            Column(Modifier.padding(top = 12.dp)) {
                Text(s.location, fontSize = 12.sp, color = cs.onSurfaceVariant)
                Text(t.location)
            }
            Column(Modifier.padding(top = 12.dp)) {
                Text(s.reportedOn, fontSize = 12.sp, color = cs.onSurfaceVariant)
                Text(formatDate(t.createdAt, s))
            }
            Column(Modifier.padding(top = 12.dp)) {
                Text(s.status, fontSize = 12.sp, color = cs.onSurfaceVariant)
                Text(if (t.isConfirmed) "✅ ${s.statusConfirmed}" else "⏳ ${s.statusWaiting}")
            }
            Column(Modifier.padding(top = 12.dp)) {
                Text(s.description, fontSize = 12.sp, color = cs.onSurfaceVariant)
                Text(t.description.ifBlank { s.noDescription })
            }

            Spacer(Modifier.height(16.dp))
            if (!t.isOwnReport) {
                ConfirmButton(t.isConfirmed, s, onConfirm, Modifier.fillMaxWidth())
            }
            OutlinedButton(onClick = onShowOnMap, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 40.dp)) {
                Icon(Icons.Outlined.Map, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(s.showOnMap)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}