package com.example.labs.ui.screens.quotes

import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.labs.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(
    onAddClick: () -> Unit,
    onQuoteClick: (Long) -> Unit,
    viewModel: QuotesViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadQuotes()
    }
    val quotes = viewModel.quotes
    val isLoading = viewModel.isLoading
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.quot)) },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.generateAndSaveNewQuote() }) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Сгенерировать ИИ цитату"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        val filteredQuotes = viewModel.filteredAndSortedQuotes

        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                label = { Text("Поиск (нечеткий)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Sort Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChip(
                    selected = viewModel.sortOrder == SortOrder.RATING_DESC,
                    onClick = { viewModel.sortOrder = SortOrder.RATING_DESC },
                    label = { Text("По рейтингу") }
                )
                FilterChip(
                    selected = viewModel.sortOrder == SortOrder.AUTHOR_ASC,
                    onClick = { viewModel.sortOrder = SortOrder.AUTHOR_ASC },
                    label = { Text("По автору") }
                )
                FilterChip(
                    selected = viewModel.sortOrder == SortOrder.TITLE_ASC,
                    onClick = { viewModel.sortOrder = SortOrder.TITLE_ASC },
                    label = { Text("По названию") }
                )
            }

            if (filteredQuotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_quotes))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredQuotes) { quote ->
                        QuoteItem(
                            quote = quote,
                            onClick = { onQuoteClick(quote.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteItem(
    quote: QuoteUiModel,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {

            Column {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = quote.header,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "⭐ ${quote.rating}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = quote.content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = quote.authorName,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}