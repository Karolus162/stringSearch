import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files

class SearcherTest {

    @Test
    fun `basic single occurrence in one file test`() = runTest {
        val tempDir = Files.createTempDirectory("testDir")
        val tempFile = tempDir.resolve("test.txt")
        Files.writeString(tempFile, "Some text for test")

        val occurrences =
            searchForTextOccurrences("text for test", tempDir)
                .toList()

        assertEquals(1, occurrences.size)
        val occ = occurrences.first()
        assertEquals(tempFile, occ.file)
        assertEquals(1, occ.line)
        assertEquals(5, occ.offset)
    }

    @Test
    fun `multiple occurrences in one file test`() = runTest {
        val tempDir = Files.createTempDirectory("testDir")
        val tempFile = tempDir.resolve("test.txt")
        Files.writeString(tempFile, "test\n123testing123\nno tes here\nhere one last test")

        val occurrences =
            searchForTextOccurrences("test", tempDir)
                .toList()

        assertEquals(3, occurrences.size)

        assertEquals(1, occurrences[0].line)
        assertEquals(0, occurrences[0].offset)

        assertEquals(2, occurrences[1].line)
        assertEquals(3, occurrences[1].offset)

        assertEquals(4, occurrences[2].line)
        assertEquals(14, occurrences[2].offset)
    }

    @Test
    fun `occurrences in multiple files test`() = runTest {
        val tempDir = Files.createTempDirectory("testDir")
        val tempFile1 = tempDir.resolve("test1.txt")
        val tempFile2 = tempDir.resolve("test2.txt")
        val tempFile3 = tempDir.resolve("test3.txt")
        val tempFile4 = tempDir.resolve("test4.txt")
        Files.writeString(tempFile1, "xyzabc abc123\n\naabc")
        Files.writeString(tempFile2, "abcabcabc\n\nbca bca")
        Files.writeString(tempFile3, "nothing here")
        Files.writeString(tempFile4, "\n\nsome random word and then abc\n")

        val occurrences =
            searchForTextOccurrences("abc", tempDir)
                .toList()

        assertEquals(7, occurrences.size)
        assert(occurrences.count { it.file == tempFile1 } == 3)
        assert(occurrences.count { it.file == tempFile2 } == 3)
        assert(occurrences.count { it.file == tempFile3 } == 0)
        assert(occurrences.count { it.file == tempFile4 } == 1)
    }

    @Test
    fun `no occurrences found`() = runTest {
        val tempDir = Files.createTempDirectory("testDir")
        val tempFile = tempDir.resolve("test.txt")
        Files.writeString(tempFile, "nothing here")

        val occurrences =
            searchForTextOccurrences("something", tempDir)
                .toList()

        assertEquals(0, occurrences.size)
    }

    @Test
    fun `multiple lines occurance test`() = runTest {
        val tempDir = Files.createTempDirectory("testDir")
        val tempFile = tempDir.resolve("test.txt")
        Files.writeString(tempFile, "\nmultiple lines\nsstring\nto\nbe\nfound")

        val occurrences =
            searchForTextOccurrences("string\nto\nbe\nfound", tempDir)
                .toList()

        assertEquals(1, occurrences.size)
        val occ = occurrences.first()
        assertEquals(tempFile, occ.file)
        assertEquals(3, occ.line)
        assertEquals(1, occ.offset)
    }

    @Test
    fun `just enter test`() = runTest {
        val tempDir = Files.createTempDirectory("testDir")
        val tempFile = tempDir.resolve("test.txt")
        Files.writeString(tempFile, "\n")

        val occurrences =
            searchForTextOccurrences("\n", tempDir)
                .toList()

        assertEquals(1, occurrences.size)
        val occ = occurrences.first()
        assertEquals(tempFile, occ.file)
        assertEquals(1, occ.line)
        assertEquals(0, occ.offset)
    }
}
