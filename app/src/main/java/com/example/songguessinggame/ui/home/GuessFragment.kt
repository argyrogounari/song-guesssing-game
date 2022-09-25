package com.example.songguessinggame.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.songguessinggame.*
import kotlinx.android.synthetic.main.fragment_guess.view.*
import kotlin.random.Random

const val RANDOM_SONG_ARRAY_INDEX_ZERO = 0
const val RANDOM_SONG_ARRAY_INDEX_ONE = 1
const val NUM_OF_EXTRA_BUTTONS = 3

/**
 * Fragment that enables user to guess a song.
 *
 * @constructor Creates a song guessing fragment.
 */
class GuessFragment: Fragment() {
    private lateinit var dbHelper : DatabaseHandler
    private val songID: Int by lazy {
        arguments?.getInt(BundleValues.SONG_ID, -1) ?: -1
    }
    private val category: String by lazy {
        arguments?.getString(BundleValues.SONG_CATEGORY, "") ?: ""
    }

    /**
     * When view is created it initializes class variables and sets up UI.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_guess, container, false)

        dbHelper = DatabaseHandler(activity!!.applicationContext)
        setUpUI(root)

        return root
    }

    /**
     * When view is resumed the action bar changes text to describe current fragment.
     */
    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)?.supportActionBar?.title =
            resources.getString(R.string.guess_the_song)
    }

    /**
     * When view is destroyed the action bar changes text to describe previous fragment.
     */
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity?)?.supportActionBar?.title =
            category + resources.getString(R.string.songs)
    }

    /**
     * Sets up the buttons with songs and displays the songs lyric.
     */
    private fun setUpUI(root: View) {
        val messagePage = root.findViewById<View>(R.id.successful_guess_message)
        val songGuessingPage = root.findViewById<View>(R.id.guessing_page)

        val song = dbHelper.getSongByID(songID, category)
        val buttonList = arrayListOf<Button>()
        buttonList.add(root.findViewById(R.id.random_answer_button1))
        buttonList.add(root.findViewById(R.id.random_answer_button2))
        buttonList.add(root.findViewById(R.id.random_answer_button3))
        buttonList.add(root.findViewById(R.id.random_answer_button4))

        val correctAnswerButton = createLabelsForButtons(song, buttonList)
        messagePage.findViewById<Button>(R.id.back_to_library_button).setOnClickListener {
            activity?.onBackPressed()
        }

        // Set up button listeners and display message based on the correct or wrong answer.
        for (button in buttonList) {
            if (button != buttonList[correctAnswerButton]) {
                button.setOnClickListener {
                    messagePage.findViewById<ImageView>(R.id.success_or_fail_imageView)
                        .setImageDrawable(
                        ContextCompat.getDrawable(
                            context!!.applicationContext,
                            R.drawable.ic_dislike)
                    )
                    messagePage.findViewById<TextView>(R.id.congratulation_or_oh_no).text =
                        resources.getString(R.string.fail_big_message)
                    messagePage.findViewById<TextView>(R.id.song_master_or_next_time).text =
                        resources.getString(R.string.fail_small_message)
                    messagePage.isVisible = true
                    songGuessingPage.isVisible = false
                }
            } else {
                button.setOnClickListener {
                    messagePage.isVisible = true
                    songGuessingPage.isVisible = false
                    dbHelper.updateSongGuessedStatus(songID, song.category)
                }
            }
        }

        root.lyric_text.text = song.lyric
    }

    /**
     * Create the labels for the buttons based on randomly assigning other songs artists.
     */
    private fun createLabelsForButtons(song: Song, buttonList: ArrayList<Button>) : Int {
        val correctAnswerButton = Random.nextInt(0, NUM_OF_EXTRA_BUTTONS)
        buttonList[correctAnswerButton].text = song.title + "\n" +song.artist
        for (button in buttonList) {
            if (button != buttonList[correctAnswerButton]) {
                val randomSongs = dbHelper.getRandomSongs(category)
                var buttonText = randomSongs[RANDOM_SONG_ARRAY_INDEX_ZERO].title + "\n"+
                        randomSongs[RANDOM_SONG_ARRAY_INDEX_ZERO].artist
                if (buttonText != buttonList[correctAnswerButton].text) {
                    button.text = buttonText
                } else {
                    buttonText = randomSongs[RANDOM_SONG_ARRAY_INDEX_ONE].title + "\n" +
                            randomSongs[RANDOM_SONG_ARRAY_INDEX_ONE].artist
                    button.text = buttonText
                }
            }
        }
        return correctAnswerButton
    }

    companion object {
        /**
         * Create a new instance of GuessFragment, initialized to
         * get the songs id and category.
         */
        fun newInstance(songID: Int, category: String): GuessFragment {
            val fragment = GuessFragment()

            val args = Bundle()
            args.putInt(BundleValues.SONG_ID, songID)
            args.putString(BundleValues.SONG_CATEGORY, category)
            fragment.arguments = args

            return fragment
        }
    }
}

object BundleValues {
    const val SONG_ID = "songId"
    const val SONG_CATEGORY = "songCategory"
}