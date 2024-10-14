import android.app.Application
import android.content.Intent
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowToast
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [30], application = Application::class)
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
        // Mock a successful task with TaskCompletionSource<AuthResult>
        val taskCompletionSource = TaskCompletionSource<AuthResult>()
        val taskMock: Task<AuthResult> = taskCompletionSource.task

        // Configure authMock to return a successful task when signInWithEmailAndPassword is called
        `when`(authMock.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(taskMock)

        // Set up the UserLogin and verify intent to MainActivity
        loginActivity.binding.loginUsername.setText("111@111.cl")
        loginActivity.binding.loginPassword.setText("test000")
        loginActivity.UserLogin()

        // Complete the task as successful
        taskCompletionSource.setResult(mock(AuthResult::class.java))

        // Assert MainActivity is started
        val expectedIntent = Intent(loginActivity, MainActivity::class.java)
        val actualIntent = shadowOf(loginActivity).nextStartedActivity
        assertEquals(expectedIntent.component, actualIntent.component)
    }

    @Test
    fun testUserLogin_Failure() {
        // Mock a failed task with TaskCompletionSource<AuthResult>
        val taskCompletionSource = TaskCompletionSource<AuthResult>()
        val taskMock: Task<AuthResult> = taskCompletionSource.task

        // Configure authMock to return a failed task when signInWithEmailAndPassword is called
        `when`(authMock.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(taskMock)

        // Set up the UserLogin and trigger a failed login
        loginActivity.binding.loginUsername.setText("testuser@example.com")
        loginActivity.binding.loginPassword.setText("wrongpassword")
        loginActivity.UserLogin()

        // Complete the task with an exception to simulate failure
        taskCompletionSource.setException(Exception("Authentication failed"))

        // Capture and verify the Toast message
        val latestToastText = ShadowToast.getTextOfLatestToast()
        assertEquals("Login failed, please try again", latestToastText)
    }

}
