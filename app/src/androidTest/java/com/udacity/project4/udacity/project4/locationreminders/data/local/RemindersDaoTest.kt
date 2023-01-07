package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.runner.RunWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.Assert.assertEquals


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    private lateinit var database: RemindersDatabase

    private val reminder1 = ReminderDTO("title", "description", "location", 0.0, 1.0,"1")
    private val reminder2 = ReminderDTO("title", "description", "location", 0.0, 1.0, "2")
    private val reminder3 = ReminderDTO("title", "description", "location", 0.0, 1.0, "3")

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDatabase() {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun saveReminder_getReminders() {

        runBlocking {
            database.reminderDao().saveReminder(reminder1)
            database.reminderDao().saveReminder(reminder2)
            database.reminderDao().saveReminder(reminder3)

            val testDatabase = database.reminderDao().getReminders()

            assertEquals(3, testDatabase.size)

        }
    }

    @Test
    fun getReminderById_idInDatabase_returnData() {

        runBlocking {

            database.reminderDao().saveReminder(reminder1)

            val testDatabase = database.reminderDao().getReminderById(reminder1.id)

            assertThat<ReminderDTO>(testDatabase as ReminderDTO, notNullValue())
            assertEquals(testDatabase.title, (reminder1.title))
            assertEquals(testDatabase.description, (reminder1.description))
            assertEquals(testDatabase.location, (reminder1.location))
            assertEquals(testDatabase.latitude, (reminder1.latitude))
            assertEquals(testDatabase.longitude, (reminder1.longitude))
            assertEquals(testDatabase.id, (reminder1.id))
        }
    }


    @Test
    fun getReminderById_idNotInDatabase_returnNull() {

        runBlocking {
            database.reminderDao().saveReminder(reminder1)
            database.reminderDao().saveReminder(reminder2)
            database.reminderDao().saveReminder(reminder3)

            val testDatabase = database.reminderDao().getReminderById("8888")

            assertEquals(null, testDatabase)
        }

    }

    @Test
    fun deleteAllReminders_getReminders() {
        runBlocking {
            database.reminderDao().saveReminder(reminder1)
            database.reminderDao().saveReminder(reminder2)
            database.reminderDao().saveReminder(reminder3)

            database.reminderDao().deleteAllReminders()
            val testDatabase = database.reminderDao().getReminders()

            assertEquals(0, testDatabase.size)

        }
    }


}