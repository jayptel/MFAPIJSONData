package com.rhythm.mfapijson

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _schemeList = MutableStateFlow<List<Scheme>>(emptyList())
    val schemeList: StateFlow<List<Scheme>> = _schemeList

    private val _selectedSchemeDetail = MutableStateFlow<SchemeDetail?>(null)
    val selectedSchemeDetail: StateFlow<SchemeDetail?> = _selectedSchemeDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var allSchemes: List<Scheme> = emptyList()

    init {
        fetchAllSchemes()
    }

    private fun fetchAllSchemes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                allSchemes = apiService.getAllSchemes()
                _schemeList.value = allSchemes
                Log.d("MainViewModel", "Fetched all schemes successfully")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching schemes: ${e.message}")
                _error.value = "Failed to load schemes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchResults(query: String) {
        if (query.isEmpty()) {
            clearSearch()
            return
        }
        _schemeList.value = allSchemes.filter {
            it.schemeName.contains(query, ignoreCase = true)
        }
    }

    fun clearSearch() {
        _schemeList.value = allSchemes
        _selectedSchemeDetail.value = null
    }


    fun fetchSchemeDetails(schemeCode: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _selectedSchemeDetail.value = apiService.getSchemeDetails(schemeCode)
                Log.d("MainViewModel", "Fetched scheme details successfully for code: $schemeCode")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching scheme details: ${e.message}")
                _error.value = "Failed to load scheme details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}