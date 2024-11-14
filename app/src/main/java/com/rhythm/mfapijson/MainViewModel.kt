package com.rhythm.mfapijson

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


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

    private val _chartData = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val chartData: StateFlow<List<Pair<String, Double>>> = _chartData

    private val _searchResults = MutableStateFlow<List<Scheme>>(emptyList())
    val searchResults: StateFlow<List<Scheme>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val searchQuery = MutableStateFlow("")

    private var searchJob: Job? = null

    init {
        fetchAllSchemes()
        setupSearchFlow()
    }
    @OptIn(FlowPreview::class)
    private fun setupSearchFlow() {
        viewModelScope.launch {
            searchQuery
                .debounce(300.milliseconds) // Debounce typing events
                .filter { it.length >= 3 } // Only process queries with 3 or more chars
                .distinctUntilChanged()
                .onEach { _isSearching.value = true }
                .mapLatest { query ->
                    performSearch(query)
                }
                .catch { e ->
                    _error.value = "Search failed: ${e.message}"
                }
                .collect { results ->
                    _searchResults.value = results
                    _isSearching.value = false
                }
        }
    }

    private suspend fun performSearch(query: String): List<Scheme> {
        return withContext(Dispatchers.Default) {
            allSchemes
                .asSequence()
                .filter {
                    it.schemeName.contains(query, ignoreCase = true) ||
                            it.schemeCode.toString().contains(query)
                }
                .take(50) // Limit results for better performance
                .toList()
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
        if (query.length < 3) {
            _searchResults.value = emptyList()
        }
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
                val schemeDetail = apiService.getSchemeDetails(schemeCode)
                _selectedSchemeDetail.value = schemeDetail
                updateChartData(schemeDetail.data)
                Log.d("MainViewModel", "Fetched scheme details successfully for code: $schemeCode")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching scheme details: ${e.message}")
                _error.value = "Failed to load scheme details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateChartData(data: List<NAVData>) {
        val chartData = data.mapIndexed { index, navData ->
            navData.date to navData.nav.toDouble()
        }
        _chartData.value = chartData
    }
}

