package com.udacity.project4.locationreminders.data.local

import android.util.Log
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.success
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersDAO: RemindersDao
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun dataBaseSetup() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().context,
                RemindersDatabase::class.java
        )
                .allowMainThreadQueries()
                .build()
        remindersDAO = remindersDatabase.reminderDao()
        remindersLocalRepository =
                RemindersLocalRepository(
                        remindersDAO
                )
    }

    @After
    fun databaseClose() {
        remindersDatabase.close()
    }

    @Test
    fun getReminderEntry_checkSaveReminderAndGetReminder_assertTheCheck() = runBlocking {
        //Given
        val reminder = ReminderDTO(
                "title",
                "description",
                "location",
                10.23,
                17.25
        )

        //When
        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminder(reminder.id)

        //Then
        assertThat(result.success, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.location, `is`(reminder.location))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun getReminderList_checkSaveRemindersAndAddTwoRemindersAndGetReminders_returnsSizeTwo() = runBlocking {
        val reminder1 = ReminderDTO(
                "title_1",
                "description_1",
                "location_1",
                10.01,
                10.02
        )
        val reminder2 = ReminderDTO(
                "title_2",
                "description_2",
                "location_2",
                50.15,
                45.21
        )
        remindersLocalRepository.saveReminder(reminder1)
        remindersLocalRepository.saveReminder(reminder2)

        val result = remindersLocalRepository.getReminders()
        assertThat(result.success, `is`(true))
        result as Result.Success
        val remindersResultList = result.data
        assertThat(remindersResultList.size, `is`(2))
        assertThat(remindersResultList.contains(reminder1), `is`(true))
        assertThat(remindersResultList.contains(reminder2), `is`(true))
    }

    @Test
    fun getReminderResultSize_checkSaveReminderAndDeleteAllReminders_returnZeroSize() = runBlocking {
        val reminder1 = ReminderDTO(
                "title_1",
                "description_1",
                "location_1",
                52.21,
                18.25
        )
        val reminder2 = ReminderDTO(
                "title_2",
                "description_2",
                "location_2",
                15.21,
                19.56
        )
        remindersLocalRepository.saveReminder(reminder1)
        remindersLocalRepository.saveReminder(reminder2)

        remindersLocalRepository.deleteAllReminders()
        val result = remindersLocalRepository.getReminders()
        assertThat(result.success, `is`(true))
        result as Result.Success
        val remindersResultList = result.data
        assertThat(remindersResultList.size, `is`(0))
    }

    @Test
    fun findNonExistentReminder_returnsError() = runBlocking {
        var error: Boolean = false
        var result = remindersLocalRepository.getReminder("100")
        if (result is Result.Error)
            error = true
        assertThat(error, `is`(true))
    }
}