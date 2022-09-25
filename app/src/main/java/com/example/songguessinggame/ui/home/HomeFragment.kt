package com.example.songguessinggame.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.songguessinggame.R
import com.example.songguessinggame.SongCategory

/**
 * Fragment that enables user to chose song category.
 *
 * @constructor Creates a home fragment.
 */
class HomeFragment : Fragment() {
    private var mContainer: ViewGroup? = null

    /**
     * When view is created it initializes class variables.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        mContainer = container

        return root
    }

    /**
     * When view is resumed it sets up navigation.
     */
    override fun onResume() {
        super.onResume()
        setUpNavigation()
    }

    /**
     * Sets up navigation based on user clicks.
     */
    private fun setUpNavigation() {
        val currentPlaylistCardView = activity?.findViewById<CardView>(R.id.current_playlist_cardview)
        currentPlaylistCardView?.setOnClickListener {
            replaceWithPlaylistFragment(SongCategory.CURRENT)
        }
        val classicPlaylistCardView = activity?.findViewById<CardView>(R.id.classic_playlist_cardview)
        classicPlaylistCardView?.setOnClickListener {
            replaceWithPlaylistFragment(SongCategory.CLASSIC)
        }
    }

    /**
     * Adds this fragment to the back stack and it replaces it with
     * the collected songs list fragment.
     */
    private fun replaceWithPlaylistFragment(songCategory: SongCategory) {
        val playlistFragment = CollectedSongsListFragment.newInstance(songCategory.categoryString)

        this.fragmentManager
            ?.beginTransaction()
            ?.replace(
                mContainer!!.id, playlistFragment
            )
            ?.addToBackStack(null)
            ?.setTransition(
                FragmentTransaction.TRANSIT_FRAGMENT_OPEN
            )
            ?.commit()
    }
}