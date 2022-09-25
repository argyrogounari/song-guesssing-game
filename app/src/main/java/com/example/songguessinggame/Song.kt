package com.example.songguessinggame

/**
 * A song object.
 *
 * This class contains an object of a single song.
 *
 * @constructor Creates a song with relevant attributes.
 */
class Song (
    val id: Int,
    val artist: String,
    val title: String,
    val lyric: String,
    val albumCover: ByteArray,
    val category: SongCategory,
    var isFavourite : Boolean = false,
    var isGuessed : Boolean = false,
    var isCollected : Boolean = false
)

enum class SongCategory(val categoryString: String) {
    CURRENT("Current"),
    CLASSIC("Classic"),
    ;

    override fun toString(): String {
        return categoryString
    }
}