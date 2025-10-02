import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path

interface Occurrence {
    val file: Path
    val line: Int
    val offset: Int
}

fun searchForTextOccurrences(
    stringToSearch: String,
    directory: Path
): Flow<Occurrence> = channelFlow {
    val files = Files.walk(directory)
        .filter { Files.isRegularFile(it) }
        .toList()
    for (file in files) {
        launch(Dispatchers.IO) {
            try {
                val content = Files.readString(file)
                var currLine = 1
                var offset = 0
                var lastIndex = 0
                var index = content.indexOf(stringToSearch, 0)
                while (index >= 0) {
                    for (i in lastIndex until index) {
                        if (content[i] == '\n') {
                            currLine++
                            offset = 0
                        } else {
                            offset++
                        }
                    }
                    lastIndex = index
                    index = content.indexOf(stringToSearch, index + 1)
                    val occurrence = object : Occurrence {
                        override val file: Path = file
                        override val line: Int = currLine
                        override val offset: Int = offset
                    }
                    send(occurrence)
                }
            } catch (e: Exception) {
                println("Error reading file $file: ${e.message}")
            }
        }
    }
}
