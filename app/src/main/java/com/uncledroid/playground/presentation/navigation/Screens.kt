package com.uncledroid.playground.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Screens {

    @Serializable
    data object PostOptions : Screens

    @Serializable
    data object PostCreate : Screens

    @Serializable
    data object PostList : Screens

    @Serializable
    data object PostPut : Screens

    @Serializable
    data object PostPatch : Screens

    @Serializable
    data object PostDelete : Screens

    @Serializable
    data object ContactList : Screens

    @Serializable
    data class ContactDetail(val id: Int) : Screens
}