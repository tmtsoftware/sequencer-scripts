package esw

import org.junit.Assert
import org.junit.Test
import java.io.File
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import esw.ocs.script.evalFile

class ScriptTest {
    private fun assertSucceeded(res: ResultWithDiagnostics<EvaluationResult>) {
        Assert.assertTrue(
            "test failed:\n  ${res.reports.joinToString("\n  ") { it.message + if (it.exception == null) "" else ": ${it.exception}" }}",
            res is ResultWithDiagnostics.Success
        )
    }

    private fun assertFailed(expectedError: String, res: ResultWithDiagnostics<EvaluationResult>) {
        Assert.assertTrue(
            "test failed - expecting a failure with the message \"$expectedError\" but received " +
                    (if (res is ResultWithDiagnostics.Failure) "failure" else "success") +
                    ":\n  ${res.reports.joinToString("\n  ") { it.message + if (it.exception == null) "" else ": ${it.exception}" }}",
            res is ResultWithDiagnostics.Failure && res.reports.any { it.message.contains(expectedError) }
        )
    }

    @Test
    fun testResolveJunit() {
        val res = evalFile(File("scripts/esw/IRIS_ImagerOnly.seq.kts"))
        println("XXX res = ${res}")
        assertSucceeded(res)
    }
}