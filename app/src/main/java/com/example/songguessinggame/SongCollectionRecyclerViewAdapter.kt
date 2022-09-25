package com.example.songguessinggame

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.songguessinggame.ui.favourites.BYTE_ARRAY_OFFSET
import com.example.songguessinggame.ui.favourites.SONG_CATEGORY_DEFAULT_VALUE
import kotlinx.android.synthetic.main.fragment_recycler_view_entry.view.*

/**
 * A view adapter to help create a recycler view.
 *
 * @constructor Creates the view adapter.
 */
class SongCollectionRecyclerViewAdapter(
    private val myDataset: ArrayList<Song>,
    private val context: Context) :
    RecyclerView.Adapter<SongCollectionRecyclerViewAdapter.SongCollectionRecyclerViewViewHolder>() {

    private var mListener: OnItemClickListener? = null
    interface OnItemClickListener {
        fun onItemClick(
            songID: Int,
            viewOrGuessButtonText: String,
            songCategory: String = SONG_CATEGORY_DEFAULT_VALUE
        )
    }

    /**
     * Sets the click listener for the card views.
     * @param listener the listener to set.
     */
    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    /**
     * Replace contents of the view with song data.
     *
     * Dummy data is assigned to not yet guessed songs and
     * guessed songs are displaying their real values.
     *
     * @param holder the view holder.
     * @param position current position on the recycler view.
     */
    override fun onBindViewHolder(holder: SongCollectionRecyclerViewViewHolder, position: Int) {
        holder.songCategory = myDataset[position].category.categoryString
        holder.cardView.card_view.song_id.text = myDataset[position].id.toString()
        if (myDataset[position].isGuessed) {
            holder.cardView.card_view.song_title.text = myDataset[position].title
            holder.cardView.card_view.song_artist.text = myDataset[position].artist
            holder.cardView.card_view.album_cover.setImageBitmap(
                BitmapFactory.decodeByteArray(
                    myDataset[position].albumCover,
                    BYTE_ARRAY_OFFSET,
                    myDataset[position].albumCover.size - 1))
            holder.cardView.card_view.guess_or_view_button.text =
                context.resources.getString(R.string.song_view_button)
        } else {
            holder.cardView.card_view.song_title.text =
                context.resources.getString(R.string.unknown_song_title)
            holder.cardView.card_view.song_artist.text =
                context.resources.getString(R.string.unknown_song_artists)
            holder.cardView.card_view.album_cover.setImageDrawable(
                ContextCompat.getDrawable(
                    context.applicationContext,
                    R.drawable.ic_default_album_cover)
            )
            holder.cardView.card_view.guess_or_view_button.text =
                context.resources.getString(R.string.song_guess_button)
        }
    }

    /**
     * Sets the item count for the recycler view
     * to the size of our given data set.
     */
    override fun getItemCount() = myDataset.size

    /**
     * Sets the card views inside the recycler view.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            SongCollectionRecyclerViewViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_recycler_view_entry, parent, false) as CardView
        return SongCollectionRecyclerViewViewHolder(cardView, mListener)
    }

    /**
     * A view holder to help manage the recycler view.
     *
     * @constructor sets the on click listener for the card view button.
     */
    class SongCollectionRecyclerViewViewHolder(
        val cardView: CardView,
        listener: OnItemClickListener?
    ) : RecyclerView.ViewHolder(cardView) {
        var guessOrViewButton: TextView = itemView.findViewById(R.id.guess_or_view_button)
        lateinit var songCategory: String

        init {
            guessOrViewButton.setOnClickListener {
                if (listener != null) {
                    val position = adapterPosition
                    val songID =
                        itemView.findViewById<TextView>(R.id.song_id).text.toString().toInt()
                    val viewOrGuessButtonText =
                        itemView.findViewById<TextView>(R.id.guess_or_view_button).text.toString()
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(songID, viewOrGuessButtonText, songCategory)
                    }
                }
            }
        }
    }
}