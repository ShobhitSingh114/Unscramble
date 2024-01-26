package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class GameViewModel : ViewModel() {

    private lateinit var currentWord: String
    private var usedWords: MutableSet<String> = mutableSetOf()
    var userGuess by mutableStateOf("")
        private set

    /**
     * StateFlow is a data holder observable flow that emits the current and new state updates.
     * Its value property reflects the current state value.
     * To update state and send it to the flow, assign a new value to the value property of the
     * MutableStateFlow class.
     */
    // _uiState is accessable and available inside the VM (backing property)
    // _uiState is for Unscrambled words
    private val _uiState = MutableStateFlow(GameUiState())
    // asStateFlow() makes this mutable state flow a read-only state flow.
    val uiState : StateFlow<GameUiState> = _uiState.asStateFlow()

    fun skipWord() {
        updateGameState(_uiState.value.score)
        // Reset user guess
        updateUserGuess("")
    }

    private fun updateGameState(updateScore: Int) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            // Last round in the game, update isGameOver to true, don't pick a new word
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updateScore,
                    isGameOver = true
                )
            }
        } else{
            // Normal rpund in the game
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord =  pickRandomWordAndShuffle(),
                    score = updateScore,
                    currentWordCount = currentState.currentWordCount.inc()
                )
            }
        }
    }


    private fun pickRandomWordAndShuffle(): String {
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord = allWords.random()
        if (usedWords.contains(currentWord)){
            return pickRandomWordAndShuffle()
        }else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    // Scrambled current word so that user can unscramble it 'lol'
    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        // Scramble the word
        tempWord.shuffle()
        while (String(tempWord).equals(word)){
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }

    fun updateUserGuess(gussedWord: String) {
        // HYpo: gussedWord id "it"
        // here what is "it"? :- word jo user ne guess kiya / word jo user ne type kiya
        // then isko UI mai display kr ne kliye "userGuess" variable mai assign kr diya
        userGuess = gussedWord
    }

    // Note on copy() method: Use the copy() function to copy an object, allowing you to alter some of its properties while keeping
    // the rest unchanged.
    //Example:
    //val jack = User(name = "Jack", age = 1)
    //val olderJack = jack.copy(age = 2)
    fun checkUserGuess() {
         if (userGuess.equals(currentWord, ignoreCase = true)) {
//        if (userGuess == currentWord){
             // User's guess is correct, increase the score
             // and call updateGameState() to prepare the game for next round
             val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
             updateGameState(updatedScore)
         } else {
             // User's guess is wrong, show an error
             // outlineTextField mai error show kr na hai
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // Reset user guess
        updateUserGuess("")
    }

    init {
        resetGame()
    }

}