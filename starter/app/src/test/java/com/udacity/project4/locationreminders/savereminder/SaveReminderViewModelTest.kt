package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = intArrayOf(Build.VERSION_CODES.P))

class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects
    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val fakeDataSource = FakeDataSource()

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun createViewModel() {
        stopKoin()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }


    @Test
    fun check_loading() {
        val reminderDataItem = ReminderDataItem(
            "Do SomeChores",
            "So SomeChores in El-Tahrir Squere",
            "El-Tahrir Squere",
            30.7563,
            31.5018,
            "1"
        )
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminderDataItem)
        var showLoading = saveReminderViewModel.showLoading.getOrAwaitValue()
        MatcherAssert.assertThat(showLoading, CoreMatchers.`is`(true))
        mainCoroutineRule.resumeDispatcher()
        showLoading = saveReminderViewModel.showLoading.getOrAwaitValue()
        MatcherAssert.assertThat(showLoading, CoreMatchers.`is`(false))
    }

    @Test
    fun check_toast() {
        val reminderDataItem = ReminderDataItem(
            "Do SomeChores",
            "So SomeChores in El-Medan Squere",
            "El-MEdan Squere",
            30.7563,
            31.5018,
            "2"
        )
        saveReminderViewModel.saveReminder(reminderDataItem)
        val showToast = saveReminderViewModel.showToast.getOrAwaitValue()
        MatcherAssert.assertThat(showToast, CoreMatchers.`is`("Reminder Saved !"))
    }

    @Test
    fun check_navigation() {
        val reminderDataItem = ReminderDataItem(
            "Do SomeChores",
            "So SomeChores in El-Medan Squere",
            "El-MEdan Squere",
            30.7563,
            31.5018,
            "2"
        )
        saveReminderViewModel.saveReminder(reminderDataItem)
        val navigate = saveReminderViewModel.navigationCommand.getOrAwaitValue()
        navigate as NavigationCommand
        MatcherAssert.assertThat(
            navigate,
            CoreMatchers.instanceOf(NavigationCommand.Back::class.java)
        )
    }

    @Test
    fun shouldReturnError_noTitle() {
        val reminderDataItem = ReminderDataItem(
            null,
            "So SomeChores in El-Medan Squere",
            "El-MEdan Squere",
            30.7563,
            31.5018,
            "2"
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.validateEnteredData(reminderDataItem),
            CoreMatchers.`is`(false)
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            CoreMatchers.`is`(R.string.err_enter_title)
        )
    }

    @Test
    fun shouldReturnError_noLocation() {
        val reminderDataItem = ReminderDataItem(
            "Do SomeChores",
            "So SomeChores in El-Medan Squere",
            null,
            30.7563,
            31.5018,
            "2"
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.validateEnteredData(reminderDataItem),
            CoreMatchers.`is`(false)
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            CoreMatchers.`is`(R.string.err_select_location)
        )
    }


}