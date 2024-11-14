package com.rhythm.mfapijson

import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.text.TextComponent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val selectedSchemeDetail by viewModel.selectedSchemeDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Searchable dropdown at the top
        SearchableDropdown(
            viewModel = viewModel,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onSchemeSelected = { scheme ->
                viewModel.fetchSchemeDetails(scheme.schemeCode)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main content area with fixed height
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> LoadingIndicator()
                error != null -> ErrorMessage(error = error!!)
                selectedSchemeDetail != null -> SchemeDetailsScreen(detail = selectedSchemeDetail!!)
                else -> InitialStateMessage()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdown(
    viewModel: MainViewModel,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSchemeSelected: (Scheme) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    onSearchQueryChange(query)
                    isExpanded = query.length >= 3
                    viewModel.onSearchQueryChanged(query)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text("Enter at least 3 characters to search...") },
                leadingIcon = {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = {
                                onSearchQueryChange("")
                                isExpanded = false
                                viewModel.clearSearch()
                            }
                        ) {
                            Icon(Icons.Default.Clear, "Clear search")
                        }
                    }
                } else null,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                )
            )

            AnimatedVisibility(
                visible = isExpanded && searchQuery.length >= 3,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .shadow(elevation = 4.dp),
                    shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        state = rememberLazyListState()
                    ) {
                        items(
                            items = searchResults,
                            key = { "${it.schemeCode}-${it.schemeName}" } // Composite key to ensure uniqueness
                        ) { scheme ->
                            DropdownSchemeItem(
                                scheme = scheme,
                                onClick = {
                                    onSchemeSelected(scheme)
                                    onSearchQueryChange(scheme.schemeName)
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun DropdownSchemeItem(
    scheme: Scheme,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = scheme.schemeName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Scheme Code: ${scheme.schemeCode}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Divider(
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
private fun InitialStateMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            modifier = Modifier
                .size(48.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Search for mutual fund schemes",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Enter scheme name in the search bar above",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ErrorMessage(error: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = error,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
fun SchemeDetailsScreen(detail: SchemeDetail) {
    var isChartExpanded by remember { mutableStateOf(true) }
    var isTableExpanded by remember { mutableStateOf(true) }
    var selectedStartDate by remember { mutableStateOf(detail.data.lastOrNull()?.date ?: "") }
    var selectedEndDate by remember { mutableStateOf(detail.data.firstOrNull()?.date ?: "") }
    var showDateRangePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }
    val currentStartDate = remember(selectedStartDate) { dateFormat.parse(selectedStartDate)?.time ?: System.currentTimeMillis() }
    val currentEndDate = remember(selectedEndDate) { dateFormat.parse(selectedEndDate)?.time ?: System.currentTimeMillis() }

    // Pre-calculate filtered data
    val filteredData = remember(selectedStartDate, selectedEndDate, detail.data) {
        detail.data.filter { isDateInRange(it.date, selectedStartDate, selectedEndDate) }
    }

    // Use LaunchedEffect for smooth animation handling
    val animatedChartHeight by animateDpAsState(
        targetValue = if (isChartExpanded) 250.dp else 0.dp,
        label = "chartHeight"
    )
    val animatedTableHeight by animateDpAsState(
        targetValue = if (isTableExpanded) 300.dp else 0.dp,
        label = "tableHeight"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Basic scheme info card
        SchemeInfoCard(detail.meta)

        // NAV chart card with optimized rendering
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                ExpandableSection(
                    title = "NAV Chart",
                    isExpanded = isChartExpanded,
                    onExpandClick = { isChartExpanded = !isChartExpanded }
                ) {
                    ChartContent(
                        isExpanded = isChartExpanded,
                        animatedHeight = animatedChartHeight,
                        selectedStartDate = selectedStartDate,
                        selectedEndDate = selectedEndDate,
                        filteredData = filteredData,
                        onDatePickerClick = { showDateRangePicker = true }
                    )
                }
            }
        }

        // NAV history table card with optimized rendering
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ExpandableSection(
                    title = "NAV History",
                    isExpanded = isTableExpanded,
                    onExpandClick = { isTableExpanded = !isTableExpanded }
                ) {
                    TableContent(
                        isExpanded = isTableExpanded,
                        animatedHeight = animatedTableHeight,
                        navDataList = detail.data
                    )
                }
            }
        }
    }

    // Date Range Picker Dialog
    if (showDateRangePicker) {
        DateRangePickerDialog(
            startDate = currentStartDate,
            endDate = currentEndDate,
            onDateRangeSelected = { start, end ->
                selectedStartDate = dateFormat.format(Date(start))
                selectedEndDate = dateFormat.format(Date(end))
                showDateRangePicker = false
            },
            onDismiss = { showDateRangePicker = false }
        )
    }
}

@Composable
private fun SchemeInfoCard(meta: Meta) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = meta.scheme_name,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fund House: ${meta.fund_house}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChartContent(
    isExpanded: Boolean,
    animatedHeight: Dp,
    selectedStartDate: String,
    selectedEndDate: String,
    filteredData: List<NAVData>,
    onDatePickerClick: () -> Unit
) {
    Column {
        OutlinedButton(
            onClick = onDatePickerClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = "Date Range")
            Spacer(modifier = Modifier.width(8.dp))
            Text("${formatDate(selectedStartDate)} - ${formatDate(selectedEndDate)}")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(animatedHeight)
        ) {
            if (isExpanded) {
                key(filteredData) {  // Key helps with recomposition optimization
                    NAVLineChart(navDataList = filteredData)
                }
            }
        }
    }
}

@Composable
private fun TableContent(
    isExpanded: Boolean,
    animatedHeight: Dp,
    navDataList: List<NAVData>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(animatedHeight)
    ) {
        if (isExpanded) {
            key(navDataList) {  // Key helps with recomposition optimization
                NAVDataTable(navDataList = navDataList)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NAVDataTable(navDataList: List<NAVData>) {
    LazyColumn {
        stickyHeader {  // Make header sticky for better UX
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "NAV",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }

        items(
            items = navDataList,
            key = { it.date }  // Add key for better list performance
        ) { navData ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = navData.date,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "₹${navData.nav}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    startDate: Long,
    endDate: Long,
    onDateRangeSelected: (Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    // Define and remember DatePickerState objects for start and end dates
    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
    val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date Range") },
        text = {
            // Make the content scrollable
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Start Date Picker
                DatePicker(
                    state = startDatePickerState,
                    showModeToggle = false,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // End Date Picker
                DatePicker(
                    state = endDatePickerState,
                    showModeToggle = false,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Retrieve selected dates from the DatePickerState objects
                    val selectedStartDate = startDatePickerState.selectedDateMillis ?: startDate
                    val selectedEndDate = endDatePickerState.selectedDateMillis ?: endDate
                    onDateRangeSelected(selectedStartDate, selectedEndDate)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


private fun formatDate(dateStr: String): String {
    val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return try {
        val date = inputFormat.parse(dateStr)
        date?.let { outputFormat.format(it) } ?: dateStr
    } catch (e: Exception) {
        dateStr
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onExpandClick)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = if (isExpanded)
                    Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            content()
        }
    }
}


@Composable
fun NAVLineChart(navDataList: List<NAVData>) {
    val chartEntries = remember(navDataList) {
        navDataList.mapIndexed { index, navData ->
            try {
                com.patrykandpatrick.vico.core.entry.FloatEntry(
                    x = index.toFloat(),
                    y = navData.nav.toFloatOrNull() ?: 0f
                )
            } catch (e: Exception) {
                com.patrykandpatrick.vico.core.entry.FloatEntry(
                    x = index.toFloat(),
                    y = 0f
                )
            }
        }
    }

    val chartModel = remember(chartEntries) {
        com.patrykandpatrick.vico.core.entry.entryModelOf(chartEntries)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp)
    ) {
        Chart(
            chart = lineChart(
                lines = listOf(
                    LineChart.LineSpec(
                        lineColor = MaterialTheme.colorScheme.primary.toArgb(),
                        lineThicknessDp = 2f,
                        lineBackgroundShader = createLineShader(),
                        lineCap = Paint.Cap.ROUND
                    )
                )
            ),
            model = chartModel,
            startAxis = startAxis(
                valueFormatter = { value, _ -> "₹%.2f".format(value) },
                label = createTextComponent(
                    textColor = MaterialTheme.colorScheme.onSurface.toArgb(),
                    textSize = 12f,
                    typeface = Typeface.DEFAULT
                )
            ),
            bottomAxis = bottomAxis(
                valueFormatter = { value, _ ->
                    try {
                        navDataList[value.toInt()].date.substring(0, 5)
                    } catch (e: Exception) {
                        ""
                    }
                },
                label = createTextComponent(
                    textColor = MaterialTheme.colorScheme.onSurface.toArgb(),
                    textSize = 12f,
                    typeface = Typeface.DEFAULT
                )
            )
        )
    }
}

private fun createLineShader(): DynamicShader {
    return DynamicShader { drawContext, _, _, _, height ->
        LinearGradient(
            0f,
            0f,
            0f,
            height,
            intArrayOf(
                Color.Blue.copy(alpha = 0.4f).toArgb(),
                Color.Blue.copy(alpha = 0.1f).toArgb(),
                Color.Blue.copy(alpha = 0.0f).toArgb()
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
    }
}

private fun createTextComponent(
    textColor: Int,
    textSize: Float,
    typeface: Typeface
): TextComponent {
    return TextComponent.Builder().apply {
        this.color = textColor
        this.textSizeSp = textSize
        this.typeface = typeface
    }.build()
}

fun isDateInRange(dateStr: String, startDateStr: String, endDateStr: String): Boolean {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    try {
        val date = dateFormat.parse(dateStr)
        val startDate = dateFormat.parse(startDateStr)
        val endDate = dateFormat.parse(endDateStr)
        return date != null && startDate != null && endDate != null &&
                !date.before(startDate) && !date.after(endDate)
    } catch (e: Exception) {
        return false
    }
}


