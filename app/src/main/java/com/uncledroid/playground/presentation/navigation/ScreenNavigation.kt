package com.uncledroid.playground.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.uncledroid.playground.presentation.contactdetail.ContactDetailScreen
import com.uncledroid.playground.presentation.contactdetail.ContactDetailViewModel
import com.uncledroid.playground.presentation.contactlist.ContactListScreen
import com.uncledroid.playground.presentation.contactlist.ContactListViewModel
import com.uncledroid.playground.presentation.contactlist.ListAction
import com.uncledroid.playground.presentation.post.DeleteEvent
import com.uncledroid.playground.presentation.post.PatchEvent
import com.uncledroid.playground.presentation.post.PostCreateScreen
import com.uncledroid.playground.presentation.post.PostCreateViewModel
import com.uncledroid.playground.presentation.post.PostDeleteScreen
import com.uncledroid.playground.presentation.post.PostDeleteViewModel
import com.uncledroid.playground.presentation.post.PostListScreen
import com.uncledroid.playground.presentation.post.PostListViewModel
import com.uncledroid.playground.presentation.post.PostOptions
import com.uncledroid.playground.presentation.post.PostPatchScreen
import com.uncledroid.playground.presentation.post.PostPatchViewModel
import com.uncledroid.playground.presentation.post.PostPutScreen
import com.uncledroid.playground.presentation.post.PostPutViewModel
import com.uncledroid.playground.presentation.post.PutEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Composable
fun ScreenNavigation() {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.PostOptions) {
        composable<Screens.PostOptions> {
            PostOptions {
                when (it) {
                    "Create Post" -> navController.navigate(Screens.PostCreate)
                    "List Post" -> navController.navigate(Screens.PostList)
                    "Update Post" -> navController.navigate(Screens.PostPut)
                    "Patch Post" -> navController.navigate(Screens.PostPatch)
                    "Delete Post" -> navController.navigate(Screens.PostDelete)
                }
            }
        }
        composable<Screens.PostCreate> {
            val viewModel = hiltViewModel<PostCreateViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            PostCreateScreen(state, viewModel::onAction)
        }
        composable<Screens.PostList> {
            val viewModel = hiltViewModel<PostListViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            PostListScreen(state = state, viewModel::onAction)
        }
        composable<Screens.PostPut> {
            val viewModel = hiltViewModel<PostPutViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            viewModel.event.collectAsEvent {
                when (it) {
                    PutEvent.Back -> navController.navigateUp()
                }
            }
            PostPutScreen(state, viewModel::onAction)
        }
        composable<Screens.PostPatch> {
            val viewModel = hiltViewModel<PostPatchViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            viewModel.event.collectAsEvent {
                when (it) {
                    PatchEvent.Back -> navController.navigateUp()
                }
            }
            PostPatchScreen(state, viewModel::onAction)
        }
        composable<Screens.PostDelete> {
            val viewModel = hiltViewModel<PostDeleteViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            viewModel.event.collectAsEvent {
                when (it) {
                    DeleteEvent.Back -> navController.navigateUp()
                }
            }
            PostDeleteScreen(state, viewModel::onAction)
        }

        // Contact screens
        composable<Screens.ContactList> {
            val viewModel = hiltViewModel<ContactListViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            ContactListScreen(state = state, onAction = { action ->
                when (action) {
                    is ListAction.OnContactSelected -> {
                        navController.navigate(Screens.ContactDetail(action.contactId))
                    }
                }
            })
        }
        composable<Screens.ContactDetail> { backStack ->
            val id = backStack.toRoute<Screens.ContactDetail>().id
            val viewModel = hiltViewModel<ContactDetailViewModel, ContactDetailViewModel.Factory> {
                it.create(id)
            }
            val state by viewModel.state.collectAsStateWithLifecycle()
            ContactDetailScreen(state = state, onAction = viewModel::onAction)
        }
    }
}

@Composable
fun <T> Flow<T>.collectAsEvent(onEvent: (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(this, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                collect(onEvent)
            }
        }
    }
}
