package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    private val reminder1 = ReminderDTO("title", "description", "location", 0.0, 1.0,"1")
    private val reminder2 = ReminderDTO("title", "description", "location", 0.0, 1.0, "2")
    private val reminder3 = ReminderDTO("title", "description", "location", 0.0, 1.0, "3")

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel(){
        stopKoin()
        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun clearDataSource() = runBlockingTest{
        fakeDataSource.deleteAllReminders()
    }

    @Test
    fun showNoData_deleteAllReminders_isTrue() {

        runBlocking {
            fakeDataSource.deleteAllReminders()
            remindersListViewModel.loadReminders()


            assertEquals(0 , remindersListViewModel.remindersList.getOrAwaitValue().size)
            assertEquals(true , remindersListViewModel.showNoData.getOrAwaitValue())

        }
    }

    @Test
    fun loadReminders_saveThreeReminders_loadsThreeReminders() {
        runBlocking {

            fakeDataSource.deleteAllReminders()

            fakeDataSource.saveReminder(reminder1)
            fakeDataSource.saveReminder(reminder2)
            fakeDataSource.saveReminder(reminder3)

            remindersListViewModel.loadReminders()

            assertEquals(3 , remindersListViewModel.remindersList.getOrAwaitValue().size)
            assertEquals(false , remindersListViewModel.showNoData.getOrAwaitValue())
        }
    }

    @Test
    fun loadReminders_checkLoading_returnTrueAndFalse() {
        runBlocking {
            mainCoroutineRule.pauseDispatcher()

            fakeDataSource.deleteAllReminders()
            fakeDataSource.saveReminder(reminder1)

            remindersListViewModel.loadReminders()

            assertEquals(true , remindersListViewModel.showLoading.getOrAwaitValue())

            mainCoroutineRule.resumeDispatcher()

            assertEquals(false , remindersListViewModel.showLoading.getOrAwaitValue())
        }
    }

    // this test should return error
    @Test
    fun loadReminders_shouldReturnError() {
        runBlocking {
            fakeDataSource.setShouldReturnError(true)
            remindersListViewModel.loadReminders()
            assertEquals(
                "Returning testing error!",
                remindersListViewModel.showSnackBar.getOrAwaitValue()
            )
        }
    }

}