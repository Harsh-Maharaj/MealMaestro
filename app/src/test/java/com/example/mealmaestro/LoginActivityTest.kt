import android.content.Intent
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mealmaestro.LoginActivity
import com.example.mealmaestro.MainActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@Config(sdk = [Build.VERSION_CODES.Q], manifest = Config.NONE)  // Set SDK and specify no manifest
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    private lateinit var authMock: FirebaseAuth
    private lateinit var loginActivity: LoginActivity

    @Before
    fun setUp() {
        authMock = mock(FirebaseAuth::class.java)
        loginActivity = Robolectric.buildActivity(LoginActivity::class.java).create().get()
        loginActivity.auth = authMock
    }

    @Test
    fun testUserLogin_Success() {
        // Mock a successful task
        val taskCompletionSource = TaskCompletionSource<AuthResult>()
        val taskMock: Task<AuthResult> = taskCompletionSource.task

        `when`(authMock.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(taskMock)

        // Simulate user input
        loginActivity.binding.loginUsername.setText("test@example.com")
        loginActivity.binding.loginPassword.setText("password123")
        loginActivity.UserLogin()

        // Complete the task successfully
        taskCompletionSource.setResult(mock(AuthResult::class.java))

        // Assert MainActivity is started
        val expectedIntent = Intent(loginActivity, MainActivity::class.java)
        val actualIntent = shadowOf(loginActivity).nextStartedActivity
        assertEquals(expectedIntent.component, actualIntent.component)
    }

    @Test
    fun testUserLogin_Failure() {
        // Mock a failed task
        val taskCompletionSource = TaskCompletionSource<AuthResult>()
        val taskMock: Task<AuthResult> = taskCompletionSource.task

        `when`(authMock.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(taskMock)

        // Simulate user input
        loginActivity.binding.loginUsername.setText("wrong@example.com")
        loginActivity.binding.loginPassword.setText("wrongpassword")
        loginActivity.UserLogin()

        // Complete the task with an exception to simulate failure
        taskCompletionSource.setException(Exception("Authentication failed"))

        // Capture and verify the Toast message
        val latestToastText = ShadowToast.getTextOfLatestToast()
        assertEquals("Login failed, please try again", latestToastText)
    }
}
