package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.test.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest : KoinTest {

    private lateinit var remindersRepository: ReminderDataSource
    private lateinit var appContext: Application

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        stopKoin()
        appContext = getApplicationContext()


        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }

        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }

        remindersRepository = get()

        runBlocking {
            remindersRepository.deleteAllReminders()
        }
    }

    @Test
    fun clickAddReminderFAB_navigateToSaveReminderFragment()  {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_Project4)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB))
            .perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun saveReminder_andShowTheRecycleView() {
        runBlocking {
            val reminder = ReminderDTO(
                "title",
                "description",
                "location",
                0.0,
                1.0,
                "id"
            )
            remindersRepository.saveReminder(reminder)

            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.Theme_Project4)

            onView(withText(reminder.title)).check(matches(isDisplayed()))
            onView(withText(reminder.description)).check(matches(isDisplayed()))
            onView(withText(reminder.location)).check(matches(isDisplayed()))
        }
    }
}