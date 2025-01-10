import com.woocommerce.android.ui.payments.receipt.preview.ReceiptHtmlInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptHtmlInterceptorTest {

    private val interceptor = ReceiptHtmlInterceptor()

    @Test
    fun `should add viewport meta tag when head is present`() {
        val originalHtml = """
            <html>
            <head><title>Test</title></head>
            <body>Content</body>
            </html>
        """.trimIndent()

        val modifiedHtml = interceptor.interceptHtmlContent(originalHtml)

        assertTrue(
            modifiedHtml.contains("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
        )
    }

    @Test
    fun `should return original content when head is missing`() {
        val originalHtml = """
            <html>
            <body>Content</body>
            </html>
        """.trimIndent()

        val modifiedHtml = interceptor.interceptHtmlContent(originalHtml)

        assertEquals(originalHtml, modifiedHtml)
    }

    @Test
    fun `should handle empty input`() {
        val originalHtml = ""

        val modifiedHtml = interceptor.interceptHtmlContent(originalHtml)

        assertEquals(originalHtml, modifiedHtml)
    }
}
