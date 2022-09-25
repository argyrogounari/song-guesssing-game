package com.example.songguessinggame.ui.favourites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.songguessinggame.*

/**
 * Fragment that manages a recycler view with favoured songs.
 *
 * @constructor Creates a favourites fragment.
 */
class FavouritesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var dbHelper : DatabaseHandler
    private var mContainer: ViewGroup? = null

    /**
     * When view is created it initializes class variables
     * and sets up UI.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_favourites, container, false)

        mContainer = container
        viewManager = LinearLayoutManager(context)
        dbHelper = DatabaseHandler(activity!!.applicationContext)

        setUpActionBar()
        setUpViewAdapter(root)

        return root
    }

    /**
     * Sets up the action bar.
     */
    private fun setUpActionBar() {
        val actionBar = (activity as MainActivity?)?.supportActionBar
        actionBar?.setHomeButtonEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(false)
        actionBar?.setDisplayShowHomeEnabled(false)
    }

    /**
     * Sets up the view adapter with song data.
     */
    private fun setUpViewAdapter(root: View) {
        val songList = dbHelper.getFavouriteSongs()
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
                replaceWithSongInfoFragment(songID, songCategory)
            }
        })

        if (activity != null) {
            recyclerView = root.findViewById(R.id.my_recycler_view)
            recyclerView.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }
    }

    /**
     * Adds this fragment to the back stack and it replaces it with
     * the song info fragment.
     */
    private fun replaceWithSongInfoFragment(songID: Int, category: String) {
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
}