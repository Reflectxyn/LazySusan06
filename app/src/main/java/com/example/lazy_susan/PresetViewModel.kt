package com.example.lazy_susan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class PresetViewModel(private val userId: String) : ViewModel() {
    private val database =
        FirebaseDatabase.getInstance().getReference("users").child(userId).child("presets")
    private val _presets = MutableLiveData<List<Preset>>(emptyList())
    val presets: LiveData<List<Preset>> = _presets

    init {
        fetchPresets()
    }

     fun fetchPresets() {
        database.get().addOnSuccessListener { snapshot ->
            val list = snapshot.children.mapNotNull { it.getValue(Preset::class.java) }
            _presets.value = list
        }
    }
    fun addPreset(
        cuisines: List<Boolean>,
        rating: Int,
        distance: Int
    ) {
        val newPreset = Preset(
            name = "Preset ${_presets.value!!.size + 1}",
            filters = Filters(cuisines, rating, distance)
        )
        val updatedList = _presets.value!!.toMutableList().apply { add(newPreset) }
        _presets.value = updatedList
        savePreset(newPreset)
    }

    fun deletePreset(id: String) {
        database.child(id).removeValue()
        _presets.value = _presets.value?.filterNot { it.id == id }
    }
    fun updatePreset(preset: Preset) {
        database.child(preset.id).setValue(preset)
        _presets.value = _presets.value?.map {
            if (it.id == preset.id) preset else it
        }
    }

    fun copyPreset(id: String) {
        val original = _presets.value?.find { it.id == id } ?: return
        val copy = original.copy(id = UUID.randomUUID().toString(), name = "${original.name} (Copy)")
        _presets.value = _presets.value!! + copy
        savePreset(copy)
    }

    fun updatePresetName(id: String, newName: String) {
        val updatedList = _presets.value!!.map {
            if (it.id == id) it.copy(name = newName) else it
        }
        _presets.value = updatedList
        database.child(id).child("name").setValue(newName)
    }

    private fun savePreset(preset: Preset) {
        database.child(preset.id).setValue(preset)
            .addOnSuccessListener {
                fetchPresets()}
    }
}

class PresetViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PresetViewModel(userId) as T
    }
}