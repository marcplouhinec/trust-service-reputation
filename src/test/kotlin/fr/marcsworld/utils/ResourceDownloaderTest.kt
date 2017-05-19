package fr.marcsworld.utils

import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.springframework.core.io.UrlResource
import org.junit.rules.ExpectedException
import java.io.IOException


/**
 * Test for the [ResourceDownloader].
 *
 * @author Marc Plouhinec
 */
class ResourceDownloaderTest {

    @Rule
    @JvmField
    val expectedException = ExpectedException.none()

    @Test
    fun testDownloadResource() {
        val resourceUrl = "http://sp.ants.gouv.fr/antsv2/ANTS_AC_ACT_PC_v1.9.pdf"
        val content = ResourceDownloader.downloadResource(UrlResource(resourceUrl))
        Assert.assertNotNull(content)
        Assert.assertTrue(content.isNotEmpty())
    }

    @Test
    fun testDownloadUnknownResource() {
        val resourceUrl = "http://sp.ants.gouv.fr/antsv2/en/ANTS_ACT_CA_CP_EN_v1.9.pdf"
        expectedException.expect(IOException::class.java)
        expectedException.expectMessage("Resource '$resourceUrl' unavailable: the status code is 404 instead of 200.")

        ResourceDownloader.downloadResource(UrlResource(resourceUrl))
    }
}