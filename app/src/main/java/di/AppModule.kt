package com.example.placely.di

import android.app.Application
import androidx.room.Room
import com.example.placely.data.AppDatabase
import com.example.placely.data.PlacelyRepository
import com.example.placely.data.dao.NoteDao
import com.example.placely.data.dao.ReminderDao
import com.example.placely.data.dao.TaskDao
import com.example.placely.ui.viewmodel.HomeViewModel
import com.example.placely.ui.viewmodel.NoteViewModel
import com.example.placely.ui.viewmodel.ReminderViewModel
import com.example.placely.ui.viewmodel.TaskViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin Module for Dependency Injection
 * This module provides all the dependencies needed throughout the app
 */
val appModule = module {

    // --- Database ---

    /**
     * Provides the Room database instance as a singleton
     */
    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            "placely_database"
        )
            .fallbackToDestructiveMigration() // For development - removes this in production
            .build()
    }

    // --- DAOs ---

    /**
     * Provides ReminderDao from the database
     */
    single<ReminderDao> {
        val database = get<AppDatabase>()
        database.reminderDao()
    }

    /**
     * Provides NoteDao from the database
     */
    single<NoteDao> {
        val database = get<AppDatabase>()
        database.noteDao()
    }

    /**
     * Provides TaskDao from the database
     */
    single<TaskDao> {
        val database = get<AppDatabase>()
        database.taskDao()
    }

    // --- Repository ---

    /**
     * Provides the PlacelyRepository as a singleton
     * It depends on all three DAOs
     */
    single {
        PlacelyRepository(
            reminderDao = get(),
            noteDao = get(),
            taskDao = get()
        )
    }

    // --- ViewModels ---

    /**
     * Provides HomeViewModel
     * ViewModels are scoped to the lifecycle of the composable/activity
     */
    viewModel {
        HomeViewModel(repository = get())
    }

    /**
     * Provides ReminderViewModel
     */
    viewModel {
        ReminderViewModel(repository = get())
    }

    /**
     * Provides TaskViewModel
     */
    viewModel {
        TaskViewModel(repository = get())
    }

    /**
     * Provides NoteViewModel
     */
    viewModel {
        NoteViewModel(repository = get())
    }
}