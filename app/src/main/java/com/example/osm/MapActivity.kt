package com.example.osm

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Picture
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.osm.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MinimapOverlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.InputStream


class MapActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private var currentLat = 0.0
    private var currentLong = 0.0
    private lateinit var binding: ActivityMapBinding
    private lateinit var map: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var targetMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // configuracao OSM - Antes de inflar o layout
        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        binding = ActivityMapBinding.inflate(layoutInflater)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContentView(binding.root)

        // remover barra de status
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        // criar mapa
        map = binding.map
        map.setTileSource(TileSourceFactory.MAPNIK)

        // movimentacao do mapa
        val mapController = map.controller
        mapController.setZoom(16)


        val mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(baseContext), map)
        mLocationOverlay.enableMyLocation()
        map.overlays.add(mLocationOverlay)

        currentLat = -20.5258752
        currentLong = -43.6862976
        val startPoint = GeoPoint(currentLat, currentLong)
        mapController.setCenter(startPoint)

        // rotation gestures
        val rotationGestureOverlay = RotationGestureOverlay(map)
        rotationGestureOverlay.isEnabled
        map.setMultiTouchControls(true)
        map.overlays.add(rotationGestureOverlay)

        // escala
        val dm : DisplayMetrics = this.resources.displayMetrics
        val scaleBarOverlay = ScaleBarOverlay(map)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
        scaleBarOverlay.textPaint.color = Color.RED // cor do texto da escala.
        scaleBarOverlay.setTextSize(22.00F)
        map.overlays.add(scaleBarOverlay)

        // minimap
        val minimapOverlay = MinimapOverlay(this, map.tileRequestCompleteHandler)
        minimapOverlay.setWidth(dm.widthPixels / 5)
        minimapOverlay.setHeight(dm.heightPixels / 5)
        map.overlays.add(minimapOverlay)


        // add icones ao mapa
        addMarker(startPoint)
        var latTarget = -20.
        var longTarget = -43.

        GlobalScope.launch {
            while (true){
                latTarget += 0.00005
                longTarget += 0.00005
                addMarkerForTarget(GeoPoint(latTarget, longTarget))
                Thread.sleep(1500)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()

        map.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsToRequest = ArrayList<String>()
        var i = 0
        while (i < grantResults.size){
            permissionsToRequest.add(permissions[i])
            i++
        }
        if(permissionsToRequest.size > 0){
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun addMarker(center: GeoPoint){
        val marker = Marker(map)
        marker.position = center
        val svgDrawable = loadSvgDrawable(R.raw.home)
        marker.icon = svgDrawable
        marker.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
        marker.title = "Casa"
        map.overlays.add(marker)
        map.invalidate()
    }

    private fun addMarkerForTarget(center: GeoPoint) {
        if (targetMarker == null) {
            // Se não houver um marcador alvo, cria um novo
            targetMarker = Marker(map)
            targetMarker!!.title = "Alvo"
            val svgDrawable = loadSvgDrawable(R.raw.dog)
            targetMarker!!.icon = svgDrawable
            targetMarker!!.setAnchor(Marker.ANCHOR_BOTTOM, Marker.ANCHOR_CENTER)
            targetMarker!!.setOnMarkerClickListener(object : Marker.OnMarkerClickListener {
                override fun onMarkerClick(marker: Marker?, mapView: MapView?): Boolean {
                    return true
                }
            })
            map.overlays.add(targetMarker)
        }

        // Atualiza a posição do marcador
        targetMarker!!.position = center
        map.invalidate()
    }

    private fun loadSvgDrawable(resourceId: Int): PictureDrawable? {
        val inputStream: InputStream = resources.openRawResource(resourceId)
        return try {
            val svg = SVG.getFromInputStream(inputStream)
            svg.setDocumentWidth("100%")
            svg.setDocumentHeight("100%")
            val picture = svg.renderToPicture(50, 50)
            PictureDrawable(picture)
        } catch (e: SVGParseException) {
            e.printStackTrace()
            null
        }
    }

}