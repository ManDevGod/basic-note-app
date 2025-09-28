package com.manish.basicnoteapp.ui

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.manish.basicnoteapp.data.AppDatabase
import com.manish.basicnoteapp.data.NoteRepository

@Composable
fun NoteAppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = NoteRepository(database.noteDao())
    val viewModel: NoteViewModel = viewModel { NoteViewModel(repository) }

    NavHost(
        navController = navController,
        startDestination = "notes_list"
    ) {
        composable("notes_list") {
            NotesListScreen(
                notes = viewModel.notes.collectAsState().value,
                onNoteClick = { noteId ->
                    viewModel.loadNoteById(noteId)
                    navController.navigate("edit_note/$noteId")
                },
                onAddNote = {
                    viewModel.clearCurrentNote()
                    navController.navigate("edit_note/new")
                },
                onDeleteNote = { note ->
                    viewModel.deleteNote(note)
                }
            )
        }
        
        composable("edit_note/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            val currentNote = viewModel.currentNote.collectAsState().value
            val isLoading = viewModel.isLoading.collectAsState().value
            
            // Load note if editing existing note
            if (noteId != "new" && noteId != null && currentNote == null) {
                LaunchedEffect(noteId) {
                    viewModel.loadNoteById(noteId.toLong())
                }
            }
            
            EditNoteScreen(
                note = currentNote,
                onSave = { title, content ->
                    viewModel.saveNote(title, content)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                },
                isLoading = isLoading
            )
        }
    }
}
