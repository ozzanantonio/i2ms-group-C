package com.urbanfix.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.urbanfix.app.data.Category
import com.urbanfix.app.data.Ticket
import com.urbanfix.app.ui.i18n.Translations
import com.urbanfix.app.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.Date

val AmberWarningBg = Color(0xFFFFFBEB)
val AmberWarningBorder = Color(0xFFF59E0B)
val AmberWarningText = Color(0xFFB45309)

enum class BadgeKind { TEST_DATA, YOUR_REPORT, SYNCED, OFFLINE }

@Composable
fun SectionTitle(text: String, top: Dp = 0.dp) {
    Text(
        text, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = top, bottom = 8.dp).semantics { heading() }
    )
}

@Composable
fun ErrorText(text: String) {
    Text(text, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
}

@Composable
fun SemanticBadge(text: String, kind: BadgeKind, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminanceIsLow()
    val (bg, fg, borderCol) = when (kind) {
        BadgeKind.TEST_DATA -> if (isDark) {
            Triple(Color(0xFF451A03), Color(0xFFFCD34D), Color(0xFF92400E))
        } else {
            Triple(Color(0xFFFFFBEB), Color(0xFFB45309), Color(0xFFFDE68A))
        }
        BadgeKind.YOUR_REPORT -> if (isDark) {
            Triple(Color(0xFF0C4A6E), Color(0xFF7DD3FC), Color(0xFF0284C7))
        } else {
            Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), Color(0xFFBAE6FD))
        }
        BadgeKind.SYNCED -> if (isDark) {
            Triple(Color(0xFF14532D), Color(0xFF86EFAC), Color(0xFF166534))
        } else {
            Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Color(0xFFBBF7D0))
        }
        BadgeKind.OFFLINE -> if (isDark) {
            Triple(Color(0xFF451A03), Color(0xFFFBBF24), Color(0xFF92400E))
        } else {
            Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFFDE68A))
        }
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
fun InfoBanner(text: String, modifier: Modifier = Modifier, icon: ImageVector = Icons.Outlined.Info,
               trailing: @Composable (() -> Unit)? = null) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .background(cs.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, cs.outlineVariant, RoundedCornerShape(8.dp))
            .drawBehind { drawRect(cs.primary, Offset.Zero, Size(3.dp.toPx(), size.height)) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = cs.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
fun TicketImage(photoUri: String?, category: Category, contentDescription: String?,
                modifier: Modifier = Modifier, emojiSize: Int = 40) {
    Box(
        modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrBlank()) {
            AsyncImage(
                model = photoUri, contentDescription = contentDescription,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(category.emoji, fontSize = emojiSize.sp)
        }
    }
}

@Composable
fun PhotoPicker(
    photoUri: String?,
    s: Translations,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    onTakePhoto: (() -> Unit)? = null
) {
    val cs = MaterialTheme.colorScheme
    if (photoUri == null) {
        if (onTakePhoto != null) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .dashedBorder(1.5.dp, cs.onSurfaceVariant, 8.dp)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(s.tapToAttach, fontSize = 12.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onTakePhoto, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.PhotoCamera, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(s.takePhoto, fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = onPick, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.PhotoLibrary, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(s.chooseGallery, fontSize = 12.sp)
                    }
                }
            }
        } else {
            Box(
                Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(8.dp))
                    .dashedBorder(1.5.dp, cs.onSurfaceVariant, 8.dp)
                    .clickable(role = Role.Button, onClickLabel = s.tapToAttach, onClick = onPick)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) { Text("📷  ${s.tapToAttach}", fontSize = 13.sp, color = cs.onSurfaceVariant) }
        }
    } else {
        Column {
            AsyncImage(
                model = photoUri, contentDescription = s.photoPreview, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(8.dp))
                    .border(2.dp, cs.primary, RoundedCornerShape(8.dp))
            )
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.End) {
                if (onTakePhoto != null) {
                    TextButton(onClick = onTakePhoto) { Text(s.takePhoto) }
                }
                TextButton(onClick = onPick) { Text(s.chooseGallery) }
                TextButton(onClick = onRemove,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text(s.removePhoto)
                }
            }
        }
    }
}

@Composable
fun CategoryGrid(selected: Category, onSelect: (Category) -> Unit, s: Translations) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Category.entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { c -> CategoryButton(c, s.label(c), c == selected, { onSelect(c) }, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CategoryButton(cat: Category, label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(8.dp)
    val cs = MaterialTheme.colorScheme
    Row(
        modifier.heightIn(min = 48.dp).clip(shape)
            .background(if (selected) cs.primaryContainer else cs.surface)
            .border(1.dp, if (selected) cs.primary else cs.outlineVariant, shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(cat.emoji, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) cs.onPrimaryContainer else cs.onSurface)
    }
}

@Composable
fun ConfirmButton(confirmed: Boolean, s: Translations, onConfirm: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onConfirm, enabled = !confirmed,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (confirmed) cs.primary else cs.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = cs.onSurface,
            disabledContentColor = cs.onPrimaryContainer,
            disabledContainerColor = cs.primaryContainer
        ),
        modifier = modifier.heightIn(min = 40.dp)
    ) {
        Icon(if (confirmed) Icons.Filled.Check else Icons.Outlined.CheckCircle, null, Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            if (confirmed) s.confirmed else s.iSawThisToo,
            fontSize = 12.sp,
            fontWeight = if (confirmed) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
fun SuccessDialog(ticket: Ticket, s: Translations, onDismiss: () -> Unit, onGoToTracker: () -> Unit) {
    val synced = ticket.isSyncedWithApi
    val cs = MaterialTheme.colorScheme
    val isDark = cs.background.luminanceIsLow()
    val offlineBg = if (isDark) Color(0xFF451A03) else AmberWarningBg
    val offlineFg = if (isDark) Color(0xFFFBBF24) else AmberWarningText

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(cs.surface)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(50.dp).clip(CircleShape)
                        .background(if (synced) cs.primaryContainer else offlineBg)
                        .border(
                            2.dp,
                            if (synced) cs.primary.copy(alpha = 0.4f) else AmberWarningBorder,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (synced) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                        null,
                        tint = if (synced) cs.onPrimaryContainer else offlineFg,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(s.successTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (synced) s.successBody else s.successOfflineBody,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text("${s.ticketId}: ${ticket.id}", fontWeight = FontWeight.SemiBold, color = cs.primary)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onGoToTracker, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White)) {
                    Text(s.goToTracker, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(s.close) }
            }
        }
    }
}

@Composable
fun PinIcon(highlighted: Boolean = false) {
    Canvas(Modifier.size(26.dp, 32.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w / 2f, h)
            cubicTo(w * 0.1f, h * 0.68f, 0f, h * 0.48f, 0f, w / 2f)
            cubicTo(0f, w * 0.22f, w * 0.22f, 0f, w / 2f, 0f)
            cubicTo(w * 0.78f, 0f, w, w * 0.22f, w, w / 2f)
            cubicTo(w, h * 0.48f, w * 0.9f, h * 0.68f, w / 2f, h)
            close()
        }
        drawPath(path, if (highlighted) Color(0xFFF59E0B) else GreenPrimary)
        drawCircle(Color.White, w * 0.17f, Offset(w / 2f, h * 0.38f))
    }
}

@Composable
fun SimulatedCityMap() {
    val dark = MaterialTheme.colorScheme.background.luminanceIsLow()
    val roadColor = if (dark) Color(0xFF4A4A4A) else Color(0xFFEDF2ED)
    val blockColor = if (dark) Color(0xFF333333) else Color(0xFFD2DDD2)
    Canvas(Modifier.fillMaxSize()) {
        drawRect(roadColor)
        val xs = listOf(0f, 0.25f, 0.52f, 0.8f, 1f)
        val ys = listOf(0f, 0.16f, 0.35f, 0.54f, 0.72f, 0.88f, 1f)
        val gap = 6.dp.toPx()
        for (i in 0 until xs.size - 1) {
            for (j in 0 until ys.size - 1) {
                val l = xs[i] * size.width + gap
                val t = ys[j] * size.height + gap
                val r = xs[i + 1] * size.width - gap
                val b = ys[j + 1] * size.height - gap
                drawRoundRect(blockColor, Offset(l, t), Size(r - l, b - t), CornerRadius(4.dp.toPx()))
            }
        }
    }
}

fun Color.luminanceIsLow() = (red * 0.299f + green * 0.587f + blue * 0.114f) < 0.5f

fun Modifier.dashedBorder(width: Dp, color: Color, cornerRadius: Dp) = drawBehind {
    drawRoundRect(
        color,
        style = Stroke(width.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)),
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

fun formatDate(millis: Long, s: Translations): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", s.locale).format(Date(millis))