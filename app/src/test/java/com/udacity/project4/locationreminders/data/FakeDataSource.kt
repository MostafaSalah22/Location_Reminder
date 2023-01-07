package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

// Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var remindersServiceData: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

    // if any exception with the database happened while fetching the data from it as maybe something unknown happened to the schema so we should catch the exception and send it to the viewmodel
    private var shouldReturnError = false

    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }


    // Since we are using fake data, we must manually return errors,
    // And since we cannot check on empty lists because we have another test for empty data that should not return an error
    // On the other hand, In real data sources, we use try and catch to catch any exceptions and return them to viewModel.
    // If returns an error them show an error message, if not, it'll returns the value from remindersServiceData.
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //get all reminders
        // if we need to test it with error handling
        // make datasource return error even if it's not empty to test error.
        if (shouldReturnError) {
            return Result.Error("Returning testing error!")
        }

        // when no reminders are found, as requested
        // Room returns an empty list and the data source actually returns Result.success
        if (remindersServiceData.isEmpty()) {
            return Result.Success(remindersServiceData)
        } else {
            return Result.Success(remindersServiceData)
        }
    }

    //save the reminder
    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersServiceData.add(reminder)
    }

    //return the reminder with id
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if(shouldReturnError){
            return Result.Error("Exception error")
        }
        val reminder = remindersServiceData.find {
            it.id == id
        }
        return if (reminder!=null){
            Result.Success(reminder)
        } else{
            Result.Error("Reminder not found")
        }
    }

    // Delete all the reminders
    override suspend fun deleteAllReminders() {
        remindersServiceData.clear()
    }
}