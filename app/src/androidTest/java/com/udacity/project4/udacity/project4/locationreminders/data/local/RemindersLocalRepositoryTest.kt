package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var reminderLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    private val reminder1 = ReminderDTO("title", "description", "location", 0.0, 1.0,"1")
    private val reminder2 = ReminderDTO("title", "description", "location", 0.0, 1.0, "2")
    private val reminder3 = ReminderDTO("title", "description", "location", 0.0, 1.0, "3")

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        reminderLocalRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminder_getReminder() {
        runBlocking {
            reminderLocalRepository.saveReminder(reminder1)

            val result = reminderLocalRepository.getReminder(reminder1.id)

            result as Result.Success
            assertEquals(result.data.title, (reminder1.title))
            assertEquals(result.data.description, (reminder1.description))
            assertEquals(result.data.location, (reminder1.location))
            assertEquals(result.data.latitude, (reminder1.latitude))
            assertEquals(result.data.longitude, (reminder1.longitude))
            assertEquals(result.data.id, (reminder1.id))
        }
    }
    @Test
    fun saveReminders_checkSize() {
        runBlocking {
            reminderLocalRepository.saveReminder(reminder1)
            reminderLocalRepository.saveReminder(reminder2)
            reminderLocalRepository.saveReminder(reminder3)

            val result = reminderLocalRepository.getReminders()

            result as Result.Success
            assertEquals(3, result.data.size)
        }
    }

    @Test
    fun saveReminders_deletesAllReminders_checkSize() {
        runBlocking {
            reminderLocalRepository.saveReminder(reminder1)
            reminderLocalRepository.saveReminder(reminder2)
            reminderLocalRepository.saveReminder(reminder3)

            reminderLocalRepository.deleteAllReminders()
            val result = reminderLocalRepository.getReminders()

            result as Result.Success
            assertEquals(result.data.size, (0))
        }
    }

    @Test
    fun getReminder_returnsError() {

        runBlocking {

            reminderLocalRepository.deleteAllReminders()
            val result = reminderLocalRepository.getReminder(reminder1.id)

            result as Result.Error
            assertEquals("Reminder not found!", result.message)
        }
    }

}