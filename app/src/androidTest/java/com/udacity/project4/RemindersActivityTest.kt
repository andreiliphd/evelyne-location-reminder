package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.robolectric.annotation.Config
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed

import androidx.test.espresso.matcher.RootMatchers.withDecorView

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNot.not


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val viewModel = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(viewModel))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @Before
    fun registerIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        register(EspressoIdlingResource.countingIdlingResource)
        register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        unregister(EspressoIdlingResource.countingIdlingResource)
        unregister(dataBindingIdlingResource)
    }

    @Test
    fun saveEmptyReminderErrorSnackBars() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())

        val snackBarMessage = appContext.getString(R.string.err_enter_title)
        onView(withText(snackBarMessage)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun launchRemindersActivityWithOneReminder() {
        val reminder = ReminderDTO(
                "Title",
                "Description",
                "Location",
                89.23,
                8.45
        )

        runBlocking {
            repository.saveReminder(reminder)
        }

        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        onView(withText(reminder.title)).check(matches(isDisplayed()))
        onView(withText(reminder.description)).check(matches(isDisplayed()))
        onView(withText(reminder.location)).check(matches(isDisplayed()))
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

    @Test
    fun addASampleReminder() {
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.selectLocation)).perform(click())

        onView(withId(R.id.map)).perform(click())
        onView(withId(R.id.alertDialogInput))
            .inRoot(isDialog()) // <---
            .check(matches(isDisplayed()))
            .perform(ViewActions.typeText("Saint Petersburg Restaurant Location"))

        onView(withText("Yes"))
            .check(matches(isDisplayed()))
            .perform(click())
        onView(withText(R.string.toastAdd)).inRoot(withDecorView(not(`is`(getActivity(scenario)?.window?.decorView))))
            .check(
                matches(
                    isDisplayed()
                )
            )

//        onView(withText(R.string.toastAdd)).inRoot(withDecorView(not(mActivityRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()))



        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText("Saint Petersburg Restaurant"))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText("Favorite place of Peter the First."))

        Espresso.closeSoftKeyboard()

        onView(withId(R.id.saveReminder)).perform(click())
        Espresso.pressBack()
        onView(withText("Saint Petersburg Restaurant")).check(matches(isDisplayed()))
        onView(withText("Favorite place of Peter the First.")).check(matches(isDisplayed()))
    }
}
