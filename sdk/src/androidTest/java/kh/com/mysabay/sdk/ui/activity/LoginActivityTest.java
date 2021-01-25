package kh.com.mysabay.sdk.ui.activity;

import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import kh.com.mysabay.sdk.R;
import kh.com.mysabay.sdk.ui.fragment.LoginFragment;
import kh.com.mysabay.sdk.ui.fragment.VerifiedFragment;

import static androidx.test.espresso.Espresso.onView;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> activityActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Before
    public void init() {

        FragmentManager fm = activityActivityTestRule.getActivity().getSupportFragmentManager();
        fm.beginTransaction().add(new LoginFragment(), "1").add(new VerifiedFragment(), "2").commit();
        fm.executePendingTransactions();
    }

    @Test
    public void testLoginFrament() {
        onView(ViewMatchers.withId(R.id.container)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}