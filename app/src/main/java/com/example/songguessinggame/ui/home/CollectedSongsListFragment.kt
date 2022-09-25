package com.example.songguessinggame.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.songguessinggame.DatabaseHandler
import com.example.songguessinggame.MainActivity
import com.example.songguessinggame.R
import com.example.songguessinggame.SongCategory
import com.example.songguessinggame.SongCollectionRecyclerViewAdapter
import com.example.songguessinggame.ui.favourites.SongInfoFragment

const val SONG_CATEGORY_DEFAULT_VALUE = ""
const val SONG_IS_COLLECTED = 1

/**
 * Fragment that shows users songs based on song category.
 *
 * @constructor Creates a collected songs list fragment.
 */
class CollectedSongsListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var dbHelper : DatabaseHandler
    private var mContainer: ViewGroup? = null
    private var root: View? = null
    private val category: String by lazy {
        arguments?.getString(BundleValues.SONG_CATEGORY, SONG_CATEGORY_DEFAULT_VALUE)
            ?: SONG_CATEGORY_DEFAULT_VALUE
    }

    /**
     * When view is created it initializes class variables
     * and set up view adapter.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_playlists, container, false)
        mContainer = container

        viewManager = LinearLayoutManager(context)
        dbHelper = DatabaseHandler(activity!!.applicationContext)

        setUpViewAdapter()

        return root
    }

    /**
     * When view is resumed the action bar changes text to describe current fragment.
     */
    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)?.supportActionBar?.title =
            category + " " + resources.getString(R.string.songs)
    }

    /**
     * When the view is destroyed the action bar changes text to describe previous fragment
     */
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity?)?.supportActionBar?.title = resources.getString(R.string.app_name)
    }

    /**
     * Sets up the view adapter with song data.
     */
    private fun setUpViewAdapter() {
        val songCategory = if (category == SongCategory.CURRENT.categoryString) {
            SongCategory.CURRENT
        } else {
            SongCategory.CLASSIC
        }
        val songList = dbHelper.getCollectedSongsFromPlaylist(
            songCategory,
            SONG_IS_COLLECTED)
        viewAdapter =
            SongCollectionRecyclerViewAdapter(
                songList,
                context!!
            )

        (viewAdapter as SongCollectionRecyclerViewAdapter).setOnItemClickListener(
            object : SongCollectionRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClick(
                songID: Int,
                viewOrGuessButtonText: String,
                songCategory: String
            ) {
                if (viewOrGuessButtonText == resources.getString(R.string.guess_button)) {
                    replaceWithGuessFragment(songID, category)
                } else {
                    replaceWithSongInfoFragment(songID, category)
                }
            }
        })

        if (activity != null) {
            recyclerView = root!!.findViewById(R.id.my_recycler_view)
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }
    }

    /**
     * Adds this fragment to the back stack and it replaces it with
     * the guess fragment.
     */
    private fun replaceWithGuessFragment(songID: Int, songCategory: String) {
        val guessFragment = GuessFragment.newInstance(
            songID,
            songCategory)

        this.fragmentManager
            ?.beginTransaction()
            ?.replace(
                mContainer!!.id, guessFragment
            )
            ?.addToBackStack(null)
            ?.setTransition(
                FragmentTransaction.TRANSIT_FRAGMENT_OPEN
            )
            ?.commit()
    }

    /**
     * Adds this fragment to the back stack and it replaces it with
     * the guess fragment.
     */
    fun replaceWithSongInfoFragment(songID: Int, category: String) {
        val guessFragment = SongInfoFragment.newInstance(
            songID,
            category)

        this.fragmentManager
            ?.beginTransaction()
            ?.replace(
                mContainer!!.id, guessFragment
            )
            ?.addToBackStack(null)
            ?.setTransition(
                FragmentTransaction.TRANSIT_FRAGMENT_OPEN
            )
            ?.commit()
    }

    companion object {
        /**
         * Create a new instance of PlaylistFragment, initialized to
         * get the song category.
         */
        fun newInstance(category: String): CollectedSongsListFragment {
            val fragment = CollectedSongsListFragment()

            val args = Bundle()
            args.putString(BundleValues.SONG_CATEGORY, category)
            fragment.arguments = args

            return fragment
        }
    }
}