package com.pomplarg.spe95.map.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButtonToggleGroup
import com.pomplarg.spe95.R
import com.pomplarg.spe95.databinding.FragmentMapBinding
import com.pomplarg.spe95.speoperations.data.SpeOperation
import com.pomplarg.spe95.speoperations.ui.SpeOperationFragmentArgs
import com.pomplarg.spe95.utils.Constants
import com.pomplarg.spe95.utils.dateTimestampToString
import com.pomplarg.spe95.utils.geopointToString
import org.koin.androidx.viewmodel.ext.android.viewModel


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var specialtyDocument: String

    private val args: SpeOperationFragmentArgs by navArgs()
    private val mapViewModel: MapViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentMapBinding.inflate(inflater, container, false)
        context ?: return binding.root

        specialtyDocument = args.specialty

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment

        binding.btnMapYearSelection.check(R.id.btn_map_year_2022)

        mapViewModel.fetchLocations(specialtyDocument, Constants.YEAR_2022.toInt())

        binding.btnMapYearSelection.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val yearChecked = when (checkedId) {
                    R.id.btn_map_year_2020 -> Constants.YEAR_2020.toInt()
                    R.id.btn_map_year_2021 -> Constants.YEAR_2021.toInt()
                    R.id.btn_map_year_2022 -> Constants.YEAR_2022.toInt()
                    else                   -> Constants.YEAR_2022.toInt()
                }
                mapViewModel.fetchLocations(specialtyDocument, yearChecked)
            }
        }

        mapFragment?.getMapAsync(this)
        return binding.root
    }

    override fun onMapReady(map: GoogleMap) {
        map.apply {
            mapViewModel.locationsLd.observe(viewLifecycleOwner, Observer { speOperations ->
                map.clear()
                val customPopupAdapter = CustomInfoWindowAdapter()
                map.setInfoWindowAdapter(customPopupAdapter)
                map.setOnInfoWindowClickListener(this@MapFragment)

                speOperations.forEach { speOperation ->
                    speOperation.address?.let { geoPoint ->
                        val marquerOptions = MarkerOptions()
                            .position(LatLng(geoPoint.latitude, geoPoint.longitude))
                        val marker = map.addMarker(marquerOptions)
                        marker?.tag = speOperation
                    }
                }
            })
        }
    }

    internal inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {
        override fun getInfoWindow(p0: Marker): View? {
            return null
        }

        override fun getInfoContents(marker: Marker): View {
            val popupContent: View = layoutInflater.inflate(R.layout.popup_map, null)
            val operation = marker.tag as SpeOperation
            val title = popupContent.findViewById<TextView>(R.id.tv_popup_title)
            val date = popupContent.findViewById<TextView>(R.id.tv_popup_date)
            val address = popupContent.findViewById<TextView>(R.id.tv_popup_address)

            title.text = getString(R.string.intervention_number, operation.idIntervention.toString())
            date.text = dateTimestampToString(operation.startDate)
            address.text = context?.let { geopointToString(it, operation.address) }

            return popupContent
        }

    }

    override fun onInfoWindowClick(marker: Marker) {
        val speOperation = marker.tag as SpeOperation
        val direction =
            MapFragmentDirections.actionMapFragmentToSpeOperationtDetailsFragment()
                .setSpeOperationId(speOperation.id)
                .setSpecialtyDetails(speOperation.specialty)
        findNavController().navigate(direction)
    }
}

