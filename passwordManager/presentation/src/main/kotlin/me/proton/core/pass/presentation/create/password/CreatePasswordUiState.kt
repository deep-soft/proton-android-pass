package me.proton.core.pass.presentation.create.password

data class CreatePasswordUiState(
    val password: String,
    val length: Int,
    val hasSpecialCharacters: Boolean
)
