package com.example.timerboxing.presentation

sealed interface UiState<out Item> {
    data object Loading : UiState<Nothing>

    data class Success<Item>(val items: List<Item>) : UiState<Item>

    data class Error(val message: String) : UiState<Nothing>
}
