import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.mealmaestro.MainActivity
import com.example.mealmaestro.SignUpActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.FirebaseApp
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
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [30], application = Application::class)
class SignUpActivityTest {

    private lateinit var authMock: FirebaseAuth
    private lateinit var signUpActivity: SignUpActivity

    @Before
    fun setUp() {
        // Initialize Firebase
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())

        // Mock FirebaseAuth and initialize SignUpActivity
        authMock = mock(FirebaseAuth::class.java)
        signUpActivity = Robolectric.buildActivity(SignUpActivity::class.java).create().get()
        signUpActivity.auth = authMock
    }

    @Test
    fun testUserSignUp_Success() {
        // Mock a successful task with TaskCompletionSource<AuthResult>
        val taskCompletionSource = TaskCompletionSource<AuthResult>()
        val taskMock: Task<AuthResult> = taskCompletionSource.task

        `when`(authMock.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(taskMock)

        signUpActivity.binding.signUpEmail.setText("test@example.com")
        signUpActivity.binding.signUpPassword.setText("password123")
        signUpActivity.binding.signUpConfirmPassword.setText("password123")
        signUpActivity.binding.signUpUsername.setText("testuser")
        signUpActivity.binding.signUpContinueBtn.performClick()

        taskCompletionSource.setResult(mock(AuthResult::class.java))

        val expectedIntent = Intent(signUpActivity, MainActivity::class.java)
        val actualIntent = shadowOf(signUpActivity).nextStartedActivity
        assertEquals(expectedIntent.component, actualIntent.component)
    }

    @Test
    fun testUserSignUp_Failure() {
        val taskCompletionSource = TaskCompletionSource<AuthResult>()
        val taskMock: Task<AuthResult> = taskCompletionSource.task

        `when`(authMock.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(taskMock)

        signUpActivity.binding.signUpEmail.setText("test@example.com")
        signUpActivity.binding.signUpPassword.setText("password123")
        signUpActivity.binding.signUpConfirmPassword.setText("password123")
        signUpActivity.binding.signUpUsername.setText("testuser")
        signUpActivity.binding.signUpContinueBtn.performClick()

        taskCompletionSource.setException(Exception("SignUp failed"))

        val latestToastText = ShadowToast.getTextOfLatestToast()
        assertEquals("Registration failed, please try again", latestToastText)
    }
}
