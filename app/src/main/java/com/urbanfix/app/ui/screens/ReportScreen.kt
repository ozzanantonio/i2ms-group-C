package com.urbanfix.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.urbanfix.app.data.DeviceLocationProvider
import com.urbanfix.app.ui.components.AmberWarningBg
import com.urbanfix.app.ui.components.AmberWarningBorder
import com.urbanfix.app.ui.components.AmberWarningText
import com.urbanfix.app.ui.components.CategoryGrid
import com.urbanfix.app.ui.components.ErrorText
import com.urbanfix.app.ui.components.PhotoPicker
import com.urbanfix.app.ui.components.SectionTitle
import com.urbanfix.app.ui.i18n.Translations
import com.urbanfix.app.ui.theme.GreenPrimary
import com.urbanfix.app.viewmodel.DraftError
import com.urbanfix.app.viewmodel.MAX_DESCRIPTION_LENGTH
import com.urbanfix.app.viewmodel.ReportDraft
import com.urbanfix.app.viewmodel.UiMessage
import com.urbanfix.app.viewmodel.UrbanFixViewModel
import java.io.File
import java.io.FileOutputStream

private fun saveBitmapToAppStorage(context: Context, bitmap: Bitmap): String? =
    runCatching {
        val file = File(context.filesDir, "urbanfix_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        Uri.fromFile(file).toString()
    }.getOrNull()

@Composable
fun ReportScreen(vm: UrbanFixViewModel, s: Translations) {
    val context = LocalContext.current
    val locationProvider = remember { DeviceLocationProvider(context) }

    val requestGps: () -> Unit = {
        vm.onGpsRequested()
        locationProvider.getCurrentLocation(vm::onGpsResult)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) requestGps() else vm.onGpsPermissionDenied()
    }
    val onGpsClick = {
        val granted = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        if (granted) requestGps()
        else permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        ReportForm(
            draft = vm.draft,
            s = s,
            showLocation = true,
            gpsLoading = vm.gpsLoading,
            gpsWarning = vm.gpsWarning,
            onClearGpsWarning = vm::clearGpsWarning,
            onCameraDenied = vm::onCameraPermissionDenied,
            onGps = onGpsClick,
            onPickOnMap = vm::startPickingLocation,
            onSubmit = vm::trySubmitReport
        )
    }

    // Modal when user is about to submit main report without photo
    if (vm.promptPhotoWarningForDraft) {
        AlertDialog(
            onDismissRequest = { vm.promptPhotoWarningForDraft = false },
            title = { Text(s.photoAdvisedTitle) },
            text = { Text(s.photoAdvisedBody) },
            confirmButton = {
                Button(
                    onClick = { vm.proceedSubmitWithoutPhoto() },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) { Text(s.submitAnyway) }
            },
            dismissButton = {
                TextButton(onClick = { vm.promptPhotoWarningForDraft = false }) { Text(s.addPhotoNow) }
            }
        )
    }
}

@Composable
fun ReportForm(
    draft: ReportDraft,
    s: Translations,
    showLocation: Boolean,
    gpsLoading: Boolean = false,
    gpsWarning: UiMessage? = null,
    onClearGpsWarning: () -> Unit = {},
    onCameraDenied: () -> Unit = {},
    onGps: () -> Unit = {},
    onPickOnMap: () -> Unit = {},
    onSubmit: () -> Set<DraftError>
) {
    val context = LocalContext.current
    val locationFocusRequester = remember { FocusRequester() }

    LaunchedEffect(gpsWarning) {
        if (gpsWarning != null && showLocation) {
            runCatching { locationFocusRequester.requestFocus() }
        }
    }

    val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) draft.updatePhoto(uri.toString())
    }
    val pickFromGallery = {
        galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            saveBitmapToAppStorage(context, bitmap)?.let { draft.updatePhoto(it) }
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            onCameraDenied()
            pickFromGallery()
        }
    }
    val takePhoto = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        if (granted) cameraLauncher.launch(null)
        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val errors = draft.errors

    var step = 1
    Column {
        SectionTitle("${step++}. ${s.photoEv}")
        PhotoPicker(
            photoUri = draft.photoUri,
            s = s,
            onPick = pickFromGallery,
            onRemove = { draft.updatePhoto(null) },
            onTakePhoto = takePhoto
        )

        SectionTitle("${step++}. ${s.category}", top = 12.dp)
        CategoryGrid(draft.category, draft::updateCategory, s)

        if (showLocation) {
            SectionTitle("${step++}. ${s.location}", top = 12.dp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onGps, enabled = !gpsLoading, modifier = Modifier.weight(1f)) {
                    if (gpsLoading) {
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                        Text(s.gpsLoading, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Filled.MyLocation, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(s.currentGps, fontSize = 12.sp)
                    }
                }
                OutlinedButton(onClick = onPickOnMap, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Map, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(s.mapBtn, fontSize = 12.sp)
                }
            }

            if (gpsWarning != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmberWarningBg)
                        .border(1.dp, AmberWarningBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, null, tint = AmberWarningText, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        s.message(gpsWarning),
                        fontSize = 12.sp,
                        color = AmberWarningText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            OutlinedTextField(
                value = draft.location,
                onValueChange = {
                    draft.updateLocationText(it)
                    if (it.isNotBlank()) onClearGpsWarning()
                },
                label = { Text(s.location) },
                placeholder = { Text(s.locationHint) },
                singleLine = true,
                isError = DraftError.LOCATION_MISSING in errors,
                supportingText = if (DraftError.LOCATION_MISSING in errors) {
                    { Text(s.locationRequired) }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .focusRequester(locationFocusRequester)
            )
        } else {
            Text(
                "📍 ${s.quickReportLocation}: ${draft.location}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        SectionTitle("${step}. ${s.description}", top = 12.dp)
        val tooLong = DraftError.DESCRIPTION_TOO_LONG in errors
        OutlinedTextField(
            value = draft.description, onValueChange = draft::updateDescription,
            label = { Text(s.description) },
            placeholder = { Text(s.descriptionHint) },
            isError = tooLong,
            supportingText = {
                Row(Modifier.fillMaxWidth()) {
                    if (tooLong) Text(s.descriptionTooLong, Modifier.weight(1f)) else Spacer(Modifier.weight(1f))
                    Text("${draft.description.length}/$MAX_DESCRIPTION_LENGTH")
                }
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)
        )

        if (errors.isNotEmpty()) ErrorText(s.fixErrors)

        Button(
            onClick = { onSubmit() },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).heightIn(min = 50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White)
        ) { Text(s.submit, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}