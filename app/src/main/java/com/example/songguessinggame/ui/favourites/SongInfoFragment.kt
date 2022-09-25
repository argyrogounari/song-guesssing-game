package com.example.songguessinggame.ui.favourites

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.songguessinggame.DatabaseHandler
import com.example.songguessinggame.R
import com.example.songguessinggame.Song
import com.example.songguessinggame.ui.home.BundleValues

const val SONG_ID_DEFAULT_VALUE = -1
const val SONG_CATEGORY_DEFAULT_VALUE = ""
const val BYTE_ARRAY_OFFSET = 0
const val SONG_IS_FAVOURED = 1
const val SONG_IS_NOT_FAVOURED = 0

/**
 * Fragment that displays the full information of a song.
 *
 * @constructor Creates a song information fragment.
 */
class SongInfoFragment: Fragment() {
    private lateinit var dbHelper : DatabaseHandler
    private val songID: Int by lazy {
        arguments?.getInt(BundleValues.SONG_ID, SONG_ID_DEFAULT_VALUE) ?: SONG_ID_DEFAULT_VALUE
    }
    private val category: String by lazy {
        arguments?.getString(BundleValues.SONG_CATEGORY, SONG_CATEGORY_DEFAULT_VALUE)
            ?: SONG_CATEGORY_DEFAULT_VALUE
    }

    /**
     * When view is created it initializes class variables and sets up UI.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(
            R.layout.fragment_song_info,
            container,
            false)

        dbHelper = DatabaseHandler(context!!)

        val song = dbHelper.getSongByID(songID, category)
        setUpUI(root, song)
        setUpAddToFavourites(root, song)

        return root
    }

    /**
     * Adds the songs information to the text fields and image views.
     */
    private fun setUpUI(root: View, song: Song) {
        root.findViewById<TextView>(R.id.song_title).text = song.title
        root.findViewById<TextView>(R.id.song_artists).text = song.artist
        root.findViewById<ImageView>(R.id.song_album_cover).setImageBitmap(
            BitmapFactory.decodeByteArray(
                song.albumCover,
                BYTE_ARRAY_OFFSET,
                song.albumCover.size - 1))
        root.findViewById<TextView>(R.id.song_lyric).text = song.lyric
    }

    /**
     * Adds song to favourites when the heart icon is clicked.
     */
    private fun setUpAddToFavourites(root: View, song: Song) {
        val redHeartImageView = root.findViewById<ImageView>(R.id.heart_red)
        val blackHeartImageView = root.findViewById<ImageView>(R.id.heart_black)

        if (song.isFavourite) {
            redHeartImageView.isVisible = true
        } else {
            blackHeartImageView.isVisible = true
        }

        val heartInvisibleButtonImageView =
            root.findViewById<ImageView>(R.id.heart_invisible_button)
        heartInvisibleButtonImageView.setOnClickListener {
            if (redHeartImageView.isVisible) {
                dbHelper.updateSongFavouriteStatus(songID, song.category, SONG_IS_NOT_FAVOURED)
                blackHeartImageView.isVisible = true
                redHeartImageView.isVisible = false
            } else {
                dbHelper.updateSongFavouriteStatus(songID, song.category, SONG_IS_FAVOURED)
                redHeartImageView.isVisible = true
                blackHeartImageView.isVisible = false
            }
        }
    }

    companion object {
        /**
         * Create a new instance of SongInfoFragment, initialized to
         * get the song id and category.
         */
        fun newInstance(songID: Int, category: String): SongInfoFragment {
            val fragment = SongInfoFragment()

            val args = Bundle()
            args.putInt(BundleValues.SONG_ID, songID)
            args.putString(BundleValues.SONG_CATEGORY, category)
            fragment.arguments = args

            return fragment
        }
    }
}