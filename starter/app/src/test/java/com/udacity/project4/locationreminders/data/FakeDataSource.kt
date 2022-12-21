package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeRemindersLocalRepositoryTestDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source
    var shouldReturnError: Boolean = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
//        TODO("Return the reminders")
        return if (shouldReturnError) Result.Error("Something went wrong")
        else
            reminders.let { return Result.Success(ArrayList(it)) }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
//        TODO("save the reminder")
        reminders?.add(reminder)

    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
//        TODO("return the reminder with the id")
        val reminder = reminders?.firstOrNull() { it.id == id }
        return if (shouldReturnError) Result.Error("Something went wrong")
        else if (reminder != null) Result.Success(reminder)
        else Result.Error("Reminder not found")
    }

    override suspend fun deleteAllReminders() {
//        TODO("delete all the reminders")
        reminders?.clear()

    }


}