package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    private val reminder = ReminderDataItem("title", "description", "location", 0.0, 1.0,"1")

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel(){
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun onClear_returnsNull(){

        saveReminderViewModel.reminderTitle.value = reminder.title
        saveReminderViewModel.reminderDescription.value = reminder.description
        saveReminderViewModel.reminderSelectedLocationStr.value = reminder.location
        saveReminderViewModel.latitude.value = reminder.latitude
        saveReminderViewModel.longitude.value = reminder.longitude
        saveReminderViewModel.reminderId.value = reminder.id

        saveReminderViewModel.onClear()

        assertEquals(null , saveReminderViewModel.reminderTitle.getOrAwaitValue())
        assertEquals(null , saveReminderViewModel.reminderDescription.getOrAwaitValue())
        assertEquals(null , saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue())
        assertEquals(null , saveReminderViewModel.latitude.getOrAwaitValue())
        assertEquals(null , saveReminderViewModel.longitude.getOrAwaitValue())
        assertEquals(null , saveReminderViewModel.reminderId.getOrAwaitValue())
    }

    @Test
    fun saveReminder_addsReminder() {

        runBlocking {

            saveReminderViewModel.saveReminder(reminder)

            val checkReminder = fakeDataSource.getReminder("1") as Result.Success

            assertEquals(checkReminder.data.title, (reminder.title))
            assertEquals(checkReminder.data.description, (reminder.description))
            assertEquals(checkReminder.data.location,  (reminder.location))
            assertEquals(checkReminder.data.latitude,  (reminder.latitude))
            assertEquals(checkReminder.data.longitude,(reminder.longitude))
            assertEquals(checkReminder.data.id,(reminder.id))
        }

    }

    @Test
    fun saveReminder_checkLoading_returnsTrueAndFalse() {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        saveReminderViewModel.saveReminder(reminder)

        assertEquals(true , saveReminderViewModel.showLoading.getOrAwaitValue())

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        assertEquals(false , saveReminderViewModel.showLoading.getOrAwaitValue())

    }

    @Test
    fun validateEnteredData_noTitle_showErrorEnterTitleAndReturnFalse(){

        val reminder = ReminderDataItem("", "description", "location", 0.0, 1.0, "2")

        val validate = saveReminderViewModel.validateEnteredData(reminder)

        assertEquals(R.string.err_enter_title , saveReminderViewModel.showSnackBarInt.getOrAwaitValue())
        assertEquals(false, validate)
    }

    @Test
    fun validateEnteredData_noLocation_showErrorSelectLocationAndReturnFalse(){

        val reminder = ReminderDataItem("title", "description", "", 0.0, 1.0, "3")

        val validate = saveReminderViewModel.validateEnteredData(reminder)

        assertEquals(R.string.err_select_location , saveReminderViewModel.showSnackBarInt.getOrAwaitValue())
        assertEquals(false, validate)
    }




}