package entera.test

import com.sun.javafx.application.PlatformImpl
import entera.test.Platform.runAndWait
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    setupPrimaryStage { stage ->
        stage.scene = Scene(StackPane(Label("primary stage")), 200.0, 100.0)
        stage.show()
    }
}

fun setupPrimaryStage(block: (Stage) -> Unit = {}): Stage {
    val launcher = Platform.appLauncher
    thread {
        launcher.launch(Platform.appClass)
    }

    val stage = Platform.stageFuture.get()
    runAndWait {
        block(stage)
    }
    return stage
}

internal object Platform {
    val appClass: Class<out Application>
        get() = PrimaryStageApplication::class.java
    val appLauncher: ApplicationLauncher
        get() = NativeApplicationLauncher()
    val stageFuture: PrimaryStageFuture
        get() = PrimaryStageApplication.stageFuture

    fun runLater(block: () -> Unit) = PlatformImpl.runLater(block)
    fun runAndWait(block: () -> Unit) = PlatformImpl.runAndWait(block)
}

internal class PrimaryStageApplication : Application() {
    companion object {
        val stageFuture = PrimaryStageFuture()
    }

    override fun start(stage: Stage) {
        stageFuture.complete(stage)
    }
}

internal class PrimaryStageFuture : CompletableFuture<Stage>()

internal interface ApplicationLauncher {
    fun launch(appClass: Class<out Application>,
               vararg appArgs: String)
}

internal class NativeApplicationLauncher : ApplicationLauncher {
    override fun launch(appClass: Class<out Application>,
                        vararg appArgs: String) = Application.launch(appClass, *appArgs)
}
