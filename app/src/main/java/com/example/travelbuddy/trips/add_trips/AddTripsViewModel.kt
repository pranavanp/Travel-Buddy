package com.example.travelbuddy.trips.add_trips

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelbuddy.NavWrapper
import com.example.travelbuddy.Screen
import com.example.travelbuddy.data.model.DestinationModel
import com.example.travelbuddy.repository.DestinationRepository
import com.example.travelbuddy.repository.TripRepository
import com.example.travelbuddy.trips.add_trips.model.AddTripsPageModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTripsViewModel @Inject constructor(
    private val destinationRepository: DestinationRepository,
    private val tripRepository: TripRepository,
    private val navWrapper: NavWrapper
) : ViewModel() {
    private val _state = MutableStateFlow(AddTripsPageModel.AddTripViewState())
    val state: StateFlow<AddTripsPageModel.AddTripViewState>
        get() = _state

    private val destinationList: MutableStateFlow<List<DestinationModel.Destination>> = MutableStateFlow(listOf())
    private val tripName: MutableStateFlow<String> = MutableStateFlow(_state.value.tripName)
    private val tripId: MutableStateFlow<String?> = MutableStateFlow(_state.value.tripId)

    init {
        viewModelScope.launch {
            combine(destinationList, tripName, tripId) {
                    destinationList: List<DestinationModel.Destination>,
                    tripName: String,
                    tripId: String? ->
                AddTripsPageModel.AddTripViewState(
                    destinationList = destinationList,
                    tripName = tripName,
                    tripId = tripId
                )
            }.collect {
                _state.value = it
            }
        }
    }

    fun getData(){
        viewModelScope.launch {
            destinationRepository.getDestinations(tripId.value).collect{destination ->
                destination.data?.let {
                    destinationList.value = it
                }?: run {
                    Log.d("Error", "Error getting destination data")
                }
            }
        }
    }

    init {
        getData()
    }

    fun addDestination(destination: DestinationModel.Destination) {
        destinationList.value += destination
    }

    fun deleteDestination(destination: DestinationModel.Destination){
        destinationList.value -= destination
    }

    fun setTripName(name: String){
        tripName.value = name
    }

    fun setTripId(Id: String?){
        tripId.value = Id.toString()
        getData()
    }

    fun submitDestination(){
        viewModelScope.launch {
            val destIdList = mutableListOf<String>()
            destinationList.value.forEach { destination ->
                val response = destinationRepository.addDestination(destination)
                destIdList.add(response?.data.toString())
            }
            val tripId = tripRepository.addTrip(
                tripName= tripName.value,
                destIdList = destIdList,
            )
            tripRepository.addTripIdToUser(tripId.data.toString())
        }
    }

    fun updateDestination(){
        viewModelScope.launch {
            // Clear Existing Data
            val destinationListOrig = tripRepository.getDestinationIds(tripId.value)
            destinationListOrig.data?.forEach { destinationId ->
                destinationRepository.deleteDestination(destinationId, tripId.value)
            }

            // Update With New Data Values
            val destIdList = mutableListOf<String>()
            destinationList.value.forEach { destination ->
                val response = destinationRepository.addDestination(destination)
                destIdList.add(response?.data.toString())
            }
            tripRepository.updateDestinationIds(tripId.value, destIdList)
        }
    }

    fun navigateToTrips() {
        navWrapper.getNavController().navigate(Screen.Trips.route)
    }
}