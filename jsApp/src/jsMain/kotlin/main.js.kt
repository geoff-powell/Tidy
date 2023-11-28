import androidx.compose.ui.window.Window
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.greenmiststudios.common.UIShow
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
  onWasmReady {
    Window("tidy") {
      MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        UIShow()
      }
    }
  }
}