package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    //    TODO: Add testing implementation to the RemindersDao.kt
    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var reminderDB: RemindersDatabase

    val reminderTestCase = ReminderDTO(
        "Do Chors",
        "Clean Home",
        "Cairop",
        30.0,
        31.0,
        "101"
    )

    @Before
    fun initialiseDatabase() {
        reminderDB = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() {
        reminderDB.close()
    }
    @Test
    fun insertReminderDTOAndGetById() = runBlockingTest {


        reminderDB.reminderDao().saveReminder(reminderTestCase)

        val savedReminderDTO = reminderDB.reminderDao().getReminderById(reminderTestCase.id)

        assertThat<ReminderDTO>(savedReminderDTO, notNullValue())

        assertThat(savedReminderDTO?.id, `is`(reminderTestCase.id))
        assertThat(savedReminderDTO?.title, `is`(reminderTestCase.title))
        assertThat(savedReminderDTO?.description, `is`(reminderTestCase.description))
        assertThat(savedReminderDTO?.location, `is`(reminderTestCase.location))
        assertThat(savedReminderDTO?.latitude, `is`(reminderTestCase.latitude))
        assertThat(savedReminderDTO?.longitude, `is`(reminderTestCase.longitude))
        assertThat(savedReminderDTO?.id, `is`(reminderTestCase.id))
    }




}