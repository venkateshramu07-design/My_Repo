package com.batchmates.app.ui.finder

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.batchmates.app.data.School
import com.batchmates.app.ui.components.GlassCard
import com.batchmates.app.ui.components.GradientButton
import com.batchmates.app.ui.components.SearchField
import com.batchmates.app.ui.theme.Brand
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * Free OpenStreetMap map (no Google Maps billing account needed).
 * Pan/zoom to see schools, tap a pin to pick it, or long-press anywhere to add a missing school.
 */
@Composable
fun MapStep(s: FinderState, vm: FinderViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var query by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf<School?>(null) }
    var longPress by remember { mutableStateOf<GeoPoint?>(null) }

    val pin = remember { pinBitmap(context) }
    val map = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            isTilesScaledToDpi = true
            controller.setZoom(4.5)
            controller.setCenter(GeoPoint(21.0, 78.0))
            minZoomLevel = 3.0
        }
    }
    val myLocation = remember { MyLocationNewOverlay(GpsMyLocationProvider(context), map) }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.values.any { it }) enableMyLocation(map, myLocation)
    }

    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_RESUME -> map.onResume()
                Lifecycle.Event.ON_PAUSE -> map.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(obs)
        map.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean { selected = null; longPress = null; return true }
            override fun longPressHelper(p: GeoPoint?): Boolean { longPress = p; selected = null; return true }
        }))
        map.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?) = report()
            override fun onZoom(event: ZoomEvent?) = report()
            fun report(): Boolean {
                val b = map.boundingBox
                vm.onMapMoved(b.latSouth, b.lonWest, b.latNorth, b.lonEast, map.zoomLevelDouble)
                return false
            }
        })
        onDispose { lifecycle.removeObserver(obs); myLocation.disableMyLocation(); map.onDetach() }
    }

    // Start near what the user already chose, else on their location if permitted.
    LaunchedEffect(Unit) {
        val firstSchool = (s.schools as? Load.Ready)?.value?.firstOrNull { it.lat != null }
        if (firstSchool != null) {
            map.controller.setZoom(14.0); map.controller.setCenter(GeoPoint(firstSchool.lat!!, firstSchool.lon!!))
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation(map, myLocation)
        }
    }

    // Keep school pins in sync with state.
    LaunchedEffect(s.mapSchools) {
        map.overlays.removeAll { it is Marker }
        s.mapSchools.filter { it.lat != null && it.lon != null }.forEach { school ->
            map.overlays.add(Marker(map).apply {
                position = GeoPoint(school.lat!!, school.lon!!)
                title = school.name
                icon = BitmapDrawable(context.resources, pin)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ -> selected = school; longPress = null; true }
            })
        }
        map.invalidate()
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView({ map }, Modifier.fillMaxSize())

        // Top: back + search + results
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Brand.Night.copy(alpha = 0.85f)), contentAlignment = Alignment.Center) {
                    IconButton(onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                }
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(Brand.Night.copy(alpha = 0.88f))) {
                    SearchField(query, { query = it; if (it.isBlank()) vm.clearSearch() }, "Search school name, town…", onSearch = { vm.searchOnMap(query) })
                }
            }
            when (val r = s.searchResults) {
                Load.Loading -> Hint("Searching…", loading = true)
                is Load.Failed -> Hint(r.message)
                is Load.Ready -> if (r.value.isEmpty()) Hint("No match. Long-press on the map to add your school.") else {
                    Box(Modifier.padding(top = 8.dp).clip(RoundedCornerShape(18.dp)).background(Brand.Night.copy(alpha = 0.94f))) {
                        LazyColumn(Modifier.heightIn(max = 280.dp), contentPadding = PaddingValues(8.dp)) {
                            items(r.value, key = { it.id }) { school ->
                                Column(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                        .clickableNoRipple {
                                            vm.clearSearch()
                                            if (school.lat != null && school.lon != null) {
                                                map.controller.animateTo(GeoPoint(school.lat, school.lon), 16.0, 900L)
                                            }
                                            selected = school
                                        }
                                        .padding(10.dp),
                                ) {
                                    Text(school.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(school.region.label, color = Brand.TextDim, fontSize = 12.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
                Load.Idle -> {
                    if (s.mapLoading) Hint("Finding schools around here…", loading = true)
                    else s.mapHint?.let { Hint(it) }
                }
            }
        }

        // My location FAB
        Box(
            Modifier.align(Alignment.CenterEnd).padding(12.dp).size(48.dp).clip(CircleShape).background(Brand.Night.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center,
        ) {
            IconButton({
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation(map, myLocation)
                } else {
                    permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }
            }) { Icon(Icons.Rounded.MyLocation, "My location", tint = Brand.Sun) }
        }

        // Bottom sheet-ish card
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(12.dp)) {
            AnimatedVisibility(selected != null, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
                val school = selected
                if (school != null) GlassCard(Modifier.fillMaxWidth().background(Brand.Night.copy(alpha = 0.7f), RoundedCornerShape(24.dp))) {
                    Text("🏫  ${school.name}", style = MaterialTheme.typography.titleLarge)
                    if (school.region.label.isNotBlank()) Text(school.region.label, color = Brand.TextDim)
                    Spacer(Modifier.height(12.dp))
                    GradientButton("This is my school", { vm.pickSchool(school) }, Modifier.fillMaxWidth())
                }
            }
            AnimatedVisibility(longPress != null && selected == null, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
                val p = longPress
                if (p != null) GlassCard(Modifier.fillMaxWidth().background(Brand.Night.copy(alpha = 0.7f), RoundedCornerShape(24.dp))) {
                    Text("📍 Add a school here?", style = MaterialTheme.typography.titleLarge)
                    Text("Your school isn't on the map? Pin it and create its group.", color = Brand.TextDim)
                    Spacer(Modifier.height(12.dp))
                    GradientButton("Create school at this spot", { vm.openCreate(p.latitude, p.longitude) }, Modifier.fillMaxWidth(), brush = Brand.Warm)
                }
            }
            if (selected == null && longPress == null) {
                Text(
                    "Tap a 🎓 pin to choose · Long-press to add a missing school",
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Brand.Night.copy(alpha = 0.8f)).padding(10.dp),
                    fontSize = 12.sp, color = Brand.TextDim, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            Text("© OpenStreetMap contributors", fontSize = 10.sp, color = Color.DarkGray, modifier = Modifier.align(Alignment.End).padding(top = 4.dp))
        }
    }
}

@Composable
private fun Hint(text: String, loading: Boolean = false) {
    Row(
        Modifier.padding(top = 8.dp).clip(RoundedCornerShape(14.dp)).background(Brand.Night.copy(alpha = 0.85f)).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) { CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp); Spacer(Modifier.width(10.dp)) }
        Text(text, fontSize = 13.sp)
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit) = this.then(
    Modifier.clickable(onClick = onClick),
)

private fun enableMyLocation(map: MapView, overlay: MyLocationNewOverlay) {
    if (!map.overlays.contains(overlay)) map.overlays.add(overlay)
    overlay.enableMyLocation()
    overlay.runOnFirstFix {
        overlay.myLocation?.let { p -> map.post { map.controller.animateTo(p, 15.0, 1000L) } }
    }
}

/** Gradient map pin with a cap emoji, drawn once. */
private fun pinBitmap(context: android.content.Context): Bitmap {
    val d = context.resources.displayMetrics.density
    val w = (36 * d).toInt(); val h = (46 * d).toInt()
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.shader = android.graphics.LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), 0xFF7C4DFF.toInt(), 0xFFFF6B6B.toInt(), android.graphics.Shader.TileMode.CLAMP)
    val r = w / 2f
    val path = android.graphics.Path().apply {
        addCircle(r, r, r, android.graphics.Path.Direction.CW)
        moveTo(r * 0.45f, r * 1.6f); lineTo(r, h.toFloat()); lineTo(r * 1.55f, r * 1.6f); close()
    }
    c.drawPath(path, p)
    p.shader = null; p.color = 0xFFFFFFFF.toInt()
    c.drawCircle(r, r, r * 0.72f, p)
    p.textSize = r * 0.95f; p.textAlign = Paint.Align.CENTER
    c.drawText("🎓", r, r + p.textSize * 0.35f, p)
    return bmp
}
