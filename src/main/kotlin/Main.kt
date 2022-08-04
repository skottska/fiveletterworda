import java.io.File
import kotlin.system.measureTimeMillis

const val requiredWordLength = 5
const val requiredWordCombinations = 5
fun main() {
    val elapsed = measureTimeMillis {
        val words = File("src/main/resources/words_alpha.txt").useLines { it.toList() }
            .filter { it.length == requiredWordLength }
            .filter { it.chars().distinct().count().toInt() == requiredWordLength }
            .distinctBy { it.toCharArray().sorted() }

        val hashMapBase = HashMap<Char, Set<String>>()
        words.forEach { word ->
            word.toCharArray().forEach { char ->
                hashMapBase[char] = (hashMapBase[char] ?: mutableSetOf()) + word
            }
        }

        val searchWords = (hashMapBase['j'] ?: emptySet()) + (hashMapBase['q']?: emptySet())
        val results = mutableListOf<Set<String>>()
        searchWords.forEachIndexed { index, word ->
            println("Processing $index of ${searchWords.size}")
            removeWords(hashMapBase, setOf(word))
            validCombinations(hashMapBase.toMutableMap(), setOf(word), results)
        }
        println("Results found:" + results.size + "\n" + results)
    }
    println("Time taken: $elapsed ms")
}

fun removeWords(letterHashMap: MutableMap<Char, Set<String>>, words: Set<String>) {
    letterHashMap.keys.forEach {
        letterHashMap[it] = (letterHashMap[it] ?: arrayListOf()).toMutableSet().subtract(words)
    }
}

fun findAnyWord(letterHashMap: Map<Char, Set<String>>, numWordsAlreadyFound: Int): String? {
    val mapSizes = letterHashMap.map { it.key to it.value.size }.sortedBy { it.second }
    if (mapSizes.filter { it.second == 0 }.size > 1 + numWordsAlreadyFound * requiredWordLength) return null
    mapSizes.forEach { letter ->
        letterHashMap[letter.first].let {
            if (it?.isNotEmpty() == true) return it.first()
        }
    }
    return null
}

fun removeConflictingWords(letterHashMap: MutableMap<Char, Set<String>>, words: Set<String>) {
    words.map { it.toCharArray() }.reduce { a, b -> a + b }.distinct().forEach {
        val conflictingWords = letterHashMap[it] ?: emptySet()
        removeWords(letterHashMap, conflictingWords)
    }
}

fun validCombinations(letterHashMap: MutableMap<Char, Set<String>>, foundWords: Set<String>, results: MutableList<Set<String>>) {
    removeConflictingWords(letterHashMap, foundWords)
    var newWord = findAnyWord(letterHashMap, foundWords.size)
    while (newWord != null) {
        val newFoundWords = foundWords + newWord
        if (newFoundWords.size == requiredWordCombinations) results.add(newFoundWords)
        else validCombinations(letterHashMap.toMutableMap(), newFoundWords, results)
        removeWords(letterHashMap, setOf(newWord))
        newWord = findAnyWord(letterHashMap, foundWords.size)
    }
}
