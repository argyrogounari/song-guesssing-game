package com.example.songguessinggame

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.BaseColumns
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.ByteArrayOutputStream

/**
 * An SQLite database handler.
 *
 * This class handles all requests to the database.
 *
 * @param context the context for this class.
 * @constructor Creates a DatabaseHandler with context.
 */
class DatabaseHandler(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private var isCreating = false
    private var currentDB : SQLiteDatabase? = null

    /**
     * Creates a current songs table, a classic songs table
     * and the markers table.
     * @param database the SQLiteDatabase.
     */
    override fun onCreate(database: SQLiteDatabase?) {
        isCreating = true
        currentDB = database

        val createCurrentPlaylistTable = "CREATE TABLE ${SongTable.CURRENT_SONGS_TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${SongTable.COLUMN_ARTIST} TEXT," +
                "${SongTable.COLUMN_TITLE} TEXT," +
                "${SongTable.COLUMN_LYRIC} TEXT," +
                "${SongTable.COLUMN_ALBUM_COVER} BLOB," +
                "${SongTable.COLUMN_CATEGORY} TEXT," +
                "${SongTable.COLUMN_FAVOURED} INTEGER," +
                "${SongTable.COLUMN_GUESSED} INTEGER," +
                "${SongTable.COLUMN_COLLECTED} INTEGER)"
        database?.execSQL(createCurrentPlaylistTable)
        populateCurrentPlaylistTable()

        val createClassicPlaylistTable = "CREATE TABLE ${SongTable.CLASSIC_SONGS_TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${SongTable.COLUMN_ARTIST} TEXT," +
                "${SongTable.COLUMN_TITLE} TEXT," +
                "${SongTable.COLUMN_LYRIC} TEXT," +
                "${SongTable.COLUMN_ALBUM_COVER} BLOB," +
                "${SongTable.COLUMN_CATEGORY} TEXT," +
                "${SongTable.COLUMN_FAVOURED} INTEGER," +
                "${SongTable.COLUMN_GUESSED} INTEGER," +
                "${SongTable.COLUMN_COLLECTED} INTEGER)"
        database?.execSQL(createClassicPlaylistTable)
        populateClassicPlaylistTable()

        val createMarkersTable = "CREATE TABLE ${MarkersTable.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${MarkersTable.COLUMN_LATITUDE} REAL," +
                "${MarkersTable.COLUMN_LONGITUDE} REAL," +
                "${MarkersTable.COLUMN_CATEGORY} TEXT)"
        database?.execSQL(createMarkersTable)
        populateMarkersTable()

        isCreating = false
        currentDB = null
    }

    /**
     * Updates the current songs table, classic songs table
     * and markers table.
     * @param database the SQLiteDatabase.
     * @param oldVersion the previous version of the database.
     * @param newVersion the current version of the database.
     */
    override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        database?.execSQL("DROP TABLE IF EXISTS $SongTable.CURRENT_SONGS_TABLE_NAME")
        database?.execSQL("DROP TABLE IF EXISTS $SongTable.CLASSIC_SONGS_TABLE_NAME")
        database?.execSQL("DROP TABLE IF EXISTS $MarkersTable.TABLE_NAME")
        onCreate(database)
    }

    /**
     * Calls onUpgrade when device is requests previous
     * versions of the database.
     * @param database the SQLiteDatabase.
     * @param oldVersion the previous version of the database.
     * @param newVersion the current version of the database.
     */
    override fun onDowngrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(database, oldVersion, newVersion)
    }

    /**
     * Protects from recursive calls to the readable database.
     * @return the database.
     */
    override fun getReadableDatabase(): SQLiteDatabase? {
        return if (isCreating && currentDB != null) {
            currentDB
        } else super.getReadableDatabase()
    }

    /**
     * Protects from recursive calls to the writable database.
     * @return the database.
     */
    override fun getWritableDatabase(): SQLiteDatabase? {
        return if (isCreating && currentDB != null) {
            currentDB
        } else super.getWritableDatabase()
    }

    /**
     * Creates a BitMap from a drawable.
     * @param drawableId the drawables id.
     * @return the BitMap as a ByteArray.
     */
    private fun createImageBitMap(drawableId: Int) : ByteArray {
        val image = BitmapFactory.decodeResource(
            context.resources,
            drawableId
        )
        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    /**
     * Creates a song list based on the given cursor.
     * @param cursor the cursor.
     * @return the list of songs.
     */
    private fun getSongsListFromCursor(cursor: Cursor?) : ArrayList<Song> {
        val songs = ArrayList<Song>()
        if (cursor != null) {
            with(cursor) {
                while (moveToNext()) {
                    val songCategory =
                        getString(getColumnIndexOrThrow(SongTable.COLUMN_CATEGORY))
                    val song = Song(
                        getInt(getColumnIndexOrThrow(BaseColumns._ID)),
                        getString(getColumnIndexOrThrow(SongTable.COLUMN_ARTIST)),
                        getString(getColumnIndexOrThrow(SongTable.COLUMN_TITLE)),
                        getString(getColumnIndexOrThrow(SongTable.COLUMN_LYRIC)),
                        getBlob(getColumnIndexOrThrow(SongTable.COLUMN_ALBUM_COVER)),
                        if (songCategory == SongCategory.CURRENT.categoryString) SongCategory.CURRENT
                        else SongCategory.CLASSIC,
                        getInt(getColumnIndexOrThrow(SongTable.COLUMN_FAVOURED)) == 1,
                        getInt(getColumnIndexOrThrow(SongTable.COLUMN_GUESSED)) == 1,
                        getInt(getColumnIndexOrThrow(SongTable.COLUMN_COLLECTED)) == 1
                    )
                    songs.add(song)
                }
            }
        }

        return songs
    }

    /**
     * Returns the song category as a SongCategory.
     * @param category the song category as a string.
     */
    private fun getTableNameFromCategory(category: String) : String {
        return if (category == SongCategory.CURRENT.categoryString) {
            SongTable.CURRENT_SONGS_TABLE_NAME
        } else  {
            SongTable.CLASSIC_SONGS_TABLE_NAME
        }
    }

    /**
     * Returns the markers table from the database.
     * @return the list of CustomMarkers.
     */
    fun getMarkersTable() : MutableList<CustomMarker> {
        val projection = arrayOf(
            BaseColumns._ID,
            MarkersTable.COLUMN_LATITUDE,
            MarkersTable.COLUMN_LONGITUDE,
            MarkersTable.COLUMN_CATEGORY)

        val db = readableDatabase
        val cursor = db!!.query(
            MarkersTable.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        val markersList = mutableListOf<CustomMarker>()
        with(cursor) {
            while (moveToNext()) {
                val position = LatLng(
                    getDouble(getColumnIndexOrThrow(MarkersTable.COLUMN_LATITUDE)),
                    getDouble(getColumnIndexOrThrow(MarkersTable.COLUMN_LONGITUDE))
                )
                val title = getString(getColumnIndexOrThrow(MarkersTable.COLUMN_CATEGORY))
                val marker = CustomMarker(getInt(getColumnIndexOrThrow(BaseColumns._ID)),
                    MarkerOptions()
                    .position(position)
                    .title(title))
                markersList.add(marker)
            }
        }

        return markersList
    }

    /**
     * Returns the uncollected songs from the specified table.
     * @param category the table for the songs.
     * @return the list of songs.
     */
    fun getUncollectedSongsFromPlaylist(category: SongCategory) : MutableList<Song> {
        val projection = arrayOf(
            BaseColumns._ID,
            SongTable.COLUMN_ARTIST,
            SongTable.COLUMN_TITLE,
            SongTable.COLUMN_LYRIC,
            SongTable.COLUMN_ALBUM_COVER,
            SongTable.COLUMN_CATEGORY,
            SongTable.COLUMN_FAVOURED,
            SongTable.COLUMN_GUESSED,
            SongTable.COLUMN_COLLECTED)

        val selection = "${SongTable.COLUMN_COLLECTED} = ?"
        val selectionArgs = arrayOf("0")

        val cursor = readableDatabase!!.query(
                getTableNameFromCategory(category.categoryString),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
            )

        return getSongsListFromCursor(cursor)
    }

    /**
     * Returns the collected songs from the specified table.
     * @param category the table for the songs.
     * @return the list of songs.
     */
    fun getCollectedSongsFromPlaylist(category: SongCategory, collected: Int? = null) : ArrayList<Song> {
        val projection = arrayOf(
            BaseColumns._ID,
            SongTable.COLUMN_ARTIST,
            SongTable.COLUMN_TITLE,
            SongTable.COLUMN_LYRIC,
            SongTable.COLUMN_ALBUM_COVER,
            SongTable.COLUMN_CATEGORY,
            SongTable.COLUMN_FAVOURED,
            SongTable.COLUMN_GUESSED,
            SongTable.COLUMN_COLLECTED)

        val db = readableDatabase
        val cursor: Cursor
        if (collected != null) {
            val selection = "${SongTable.COLUMN_COLLECTED} = ?"
            val selectionArgs = if (collected == 1) {
                arrayOf("1")
            } else {
                arrayOf("0")
            }

            cursor = db!!.query(
                getTableNameFromCategory(category.categoryString),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                SongTable.COLUMN_GUESSED+" DESC"
            )
        } else {
            cursor = db!!.query(
                getTableNameFromCategory(category.categoryString),
                projection,
                null,
                null,
                null,
                null,
                SongTable.COLUMN_GUESSED+" DESC"
            )
        }

        return getSongsListFromCursor(cursor)
    }

    /**
     * Returns the favoured songs.
     * @return the list of favoured songs.
     */
    fun getFavouriteSongs() : ArrayList<Song> {
        val projection = arrayOf(
            BaseColumns._ID,
            SongTable.COLUMN_ARTIST,
            SongTable.COLUMN_TITLE,
            SongTable.COLUMN_LYRIC,
            SongTable.COLUMN_ALBUM_COVER,
            SongTable.COLUMN_CATEGORY,
            SongTable.COLUMN_FAVOURED,
            SongTable.COLUMN_GUESSED,
            SongTable.COLUMN_COLLECTED)

        val selection = "${SongTable.COLUMN_FAVOURED} = ?"
        val selectionArgs = arrayOf("1")

        val db = readableDatabase
        var cursor: Cursor?

        cursor = db!!.query(
            SongTable.CURRENT_SONGS_TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val favouriteSongs = arrayListOf<Song>()
        favouriteSongs.addAll(getSongsListFromCursor(cursor))

        cursor = db.query(
            SongTable.CLASSIC_SONGS_TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        favouriteSongs.addAll(getSongsListFromCursor(cursor))

        return favouriteSongs
    }

    /**
     * Returns the song with the specified id in the given table.
     * @param songID the songs id.
     * @param category the table for the songs.
     * @return the song.
     */
    fun getSongByID(songID: Int, category: String) : Song {
        val cursor = readableDatabase?.rawQuery("SELECT * from " +
                getTableNameFromCategory(category) + " WHERE "+BaseColumns._ID+
                " = "+songID + " Limit 1",null)

        return getSongsListFromCursor(cursor)[0]
    }

    /**
     * Returns two random songs from the specified table.
     * @param category the table for the songs.
     * @return the array with the two songs.
     */
    fun getRandomSongs(category: String) : ArrayList<Song> {
        val cursor = readableDatabase?.rawQuery("SELECT * FROM " +
                getTableNameFromCategory(category) + " ORDER BY RANDOM() LIMIT 2;",null)

        return getSongsListFromCursor(cursor)
    }

    /**
     * Updates the collection attribute of the song with
     * the specified id in the specified table.
     * @param songId the id of the song.
     * @param category the table of the song.
     */
    fun updateSongCollectionStatus(songId: Int, category: SongCategory) {
        val values = ContentValues().apply {
            put(SongTable.COLUMN_COLLECTED, 1)
        }
        writableDatabase?.update(
            getTableNameFromCategory(category.categoryString),
            values,
            "_id=$songId",
            null
        )
    }

    /**
     * Updates the guessed attribute of the song with
     * the specified id in the specified table.
     * @param songId the id of the song.
     * @param category the table of the song.
     */
    fun updateSongGuessedStatus(songId: Int, category: SongCategory) {
        val values = ContentValues().apply {
            put(SongTable.COLUMN_GUESSED, 1)
        }
        writableDatabase?.update(
            getTableNameFromCategory(category.categoryString),
            values,
            "_id=$songId",
            null
        )
    }

    /**
     * Updates the favourite attribute of the song with
     * the specified id in the specified table.
     * @param songId the id of the song.
     * @param category the table of the song.
     * @param isFavourite current favourite status.
     */
    fun updateSongFavouriteStatus(songId: Int, category: SongCategory, isFavourite: Int) {
        val values = ContentValues().apply {
            put(SongTable.COLUMN_FAVOURED, isFavourite)
        }
        writableDatabase?.update(
            getTableNameFromCategory(category.categoryString),
            values,
            "_id=$songId",
            null
        )
    }

    /**
     * Deletes the marker with the specified id
     * from the markers table.
     * @param markerID the id of the marker.
     */
    fun deleteMarker(markerID: String) {
        writableDatabase!!.execSQL("DELETE FROM ${MarkersTable.TABLE_NAME} WHERE ${BaseColumns._ID}= '$markerID'")
    }
    
    /**
     * Adds a song to its categories table.
     * @param song the song to add.
     */
    private fun addSong(song: Song) {
        val values = ContentValues().apply {
            put(SongTable.COLUMN_ARTIST, song.artist)
            put(SongTable.COLUMN_TITLE, song.title)
            put(SongTable.COLUMN_LYRIC, song.lyric)
            put(SongTable.COLUMN_ALBUM_COVER, song.albumCover)
            put(SongTable.COLUMN_CATEGORY, song.category.categoryString)
            put(SongTable.COLUMN_FAVOURED, song.isFavourite)
            put(SongTable.COLUMN_GUESSED, song.isGuessed)
            put(SongTable.COLUMN_COLLECTED, song.isCollected)
        }

        writableDatabase?.insert(
            getTableNameFromCategory(song.category.categoryString),
            null,
            values)
    }

    /**
     * Adds a marker to the markers table.
     * @param latitude the latitude of the marker.
     * @param longitude the longitude of the marker.
     * @param category the category of the marker.
     */
    private fun insertMarker(latitude: Double, longitude: Double, category: String) {
        val values = ContentValues().apply {
            put(MarkersTable.COLUMN_LATITUDE, latitude)
            put(MarkersTable.COLUMN_LONGITUDE, longitude)
            put(MarkersTable.COLUMN_CATEGORY, category)
        }

        val db = writableDatabase
        db?.insert(MarkersTable.TABLE_NAME, null, values)
    }

    /**
     * Populates the current songs table with songs.
     */
    private fun populateCurrentPlaylistTable() {
        addSong(Song(
            0,
            context.getString(R.string.current_song1_artist),
            context.getString(R.string.current_song1_title),
            context.getString(R.string.current_song1_lyric),
            createImageBitMap(R.drawable.ic_higher_love),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song2_artist),
            context.getString(R.string.current_song2_title),
            context.getString(R.string.current_song2_lyric),
            createImageBitMap(R.drawable.ic_both),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song3_artist),
            context.getString(R.string.current_song3_title),
            context.getString(R.string.current_song3_lyric),
            createImageBitMap(R.drawable.ic_sorry),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song4_artist),
            context.getString(R.string.current_song4_title),
            context.getString(R.string.current_song4_lyric),
            createImageBitMap(R.drawable.ic_landbroke_grove),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song5_artist),
            context.getString(R.string.current_song5_title),
            context.getString(R.string.current_song5_lyric),
            createImageBitMap(R.drawable.ic_taste),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song6_artist),
            context.getString(R.string.current_song6_title),
            context.getString(R.string.current_song6_lyric),
            createImageBitMap(R.drawable.ic_dont_call_me_angel),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song7_artist),
            context.getString(R.string.current_song7_title),
            context.getString(R.string.current_song7_lyric),
            createImageBitMap(R.drawable.ic_goodbyes),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song8_artist),
            context.getString(R.string.current_song8_title),
            context.getString(R.string.current_song8_lyric),
            createImageBitMap(R.drawable.ic_dance_monkey),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song9_artist),
            context.getString(R.string.current_song9_title),
            context.getString(R.string.current_song9_lyric),
            createImageBitMap(R.drawable.ic_how_do_you_sleep),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song10_artist),
            context.getString(R.string.current_song10_title),
            context.getString(R.string.current_song10_lyric),
            createImageBitMap(R.drawable.ic_take_me_back_to_london),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song11_artist),
            context.getString(R.string.current_song11_title),
            context.getString(R.string.current_song11_lyric),
            createImageBitMap(R.drawable.ic_ransom),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song12_artist),
            context.getString(R.string.current_song12_title),
            context.getString(R.string.current_song12_lyric),
            createImageBitMap(R.drawable.ic_strike_a_pose),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song13_artist),
            context.getString(R.string.current_song13_title),
            context.getString(R.string.current_song13_lyric),
            createImageBitMap(R.drawable.ic_be_honest),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song14_artist),
            context.getString(R.string.current_song14_title),
            context.getString(R.string.current_song14_lyric),
            createImageBitMap(R.drawable.ic_ride_it),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song15_artist),
            context.getString(R.string.current_song15_title),
            context.getString(R.string.current_song15_lyric),
            createImageBitMap(R.drawable.ic_circles),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song16_artist),
            context.getString(R.string.current_song16_title),
            context.getString(R.string.current_song16_lyric),
            createImageBitMap(R.drawable.ic_outnumbered),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song17_artist),
            context.getString(R.string.current_song17_title),
            context.getString(R.string.current_song17_lyric),
            createImageBitMap(R.drawable.ic_three_nights),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song18_artist),
            context.getString(R.string.current_song18_title),
            context.getString(R.string.current_song18_lyric),
            createImageBitMap(R.drawable.ic_professor_x),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song19_artist),
            context.getString(R.string.current_song19_title),
            context.getString(R.string.current_song19_lyric),
            createImageBitMap(R.drawable.ic_post_malone),
            SongCategory.CURRENT))
        addSong(Song(
            0,
            context.getString(R.string.current_song20_artist),
            context.getString(R.string.current_song20_title),
            context.getString(R.string.current_song20_lyric),
            createImageBitMap(R.drawable.ic_senorita),
            SongCategory.CURRENT))
    }

    /**
     * Populates the classic songs table with songs.
     */
    private fun populateClassicPlaylistTable() {
        addSong(Song(
            0,
            context.getString(R.string.classic_song1_artist),
            context.getString(R.string.classic_song1_title),
            context.getString(R.string.classic_song1_lyric),
            createImageBitMap(R.drawable.ic_stairway_to_heaven),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song2_artist),
            context.getString(R.string.classic_song2_title),
            context.getString(R.string.classic_song2_lyric),
            createImageBitMap(R.drawable.ic_imagine),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song3_artist),
            context.getString(R.string.classic_song3_title),
            context.getString(R.string.classic_song3_lyric),
            createImageBitMap(R.drawable.ic_smells_like_teen_spirit),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song4_artist),
            context.getString(R.string.classic_song4_title),
            context.getString(R.string.classic_song4_lyric),
            createImageBitMap(R.drawable.ic_hotel_california),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song5_artist),
            context.getString(R.string.classic_song5_title),
            context.getString(R.string.classic_song5_lyric),
            createImageBitMap(R.drawable.ic_hey_jude),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song6_artist),
            context.getString(R.string.classic_song6_title),
            context.getString(R.string.classic_song6_lyric),
            createImageBitMap(R.drawable.ic_sweet_child_o_mine),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song7_artist),
            context.getString(R.string.classic_song7_title),
            context.getString(R.string.classic_song7_lyric),
            createImageBitMap(R.drawable.ic_i_can_get_no_satisfaction),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song8_artist),
            context.getString(R.string.classic_song8_title),
            context.getString(R.string.classic_song8_lyric),
            createImageBitMap(R.drawable.ic_over_the_rainbow),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song9_artist),
            context.getString(R.string.classic_song9_title),
            context.getString(R.string.classic_song9_lyric),
            createImageBitMap(R.drawable.ic_waterloo_sunset),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song10_artist),
            context.getString(R.string.classic_song10_title),
            context.getString(R.string.classic_song10_lyric),
            createImageBitMap(R.drawable.ic_your_song),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song11_artist),
            context.getString(R.string.classic_song11_title),
            context.getString(R.string.classic_song11_lyric),
            createImageBitMap(R.drawable.ic_like_a_rolling_stone),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song12_artist),
            context.getString(R.string.classic_song12_title),
            context.getString(R.string.classic_song12_lyric),
            createImageBitMap(R.drawable.ic_billiejean),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song13_artist),
            context.getString(R.string.classic_song13_title),
            context.getString(R.string.classic_song13_lyric),
            createImageBitMap(R.drawable.ic_i_will_always_love_you),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song14_artist),
            context.getString(R.string.classic_song14_title),
            context.getString(R.string.classic_song14_lyric),
            createImageBitMap(R.drawable.ic_london_calling),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song15_artist),
            context.getString(R.string.classic_song15_title),
            context.getString(R.string.classic_song15_lyric),
            createImageBitMap(R.drawable.ic_god_save_the_queen),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song16_artist),
            context.getString(R.string.classic_song16_title),
            context.getString(R.string.classic_song16_lyric),
            createImageBitMap(R.drawable.ic_one),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song17_artist),
            context.getString(R.string.classic_song17_title),
            context.getString(R.string.classic_song17_lyric),
            createImageBitMap(R.drawable.ic_bohemian_rhapsody),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song18_artist),
            context.getString(R.string.classic_song18_title),
            context.getString(R.string.classic_song18_lyric),
            createImageBitMap(R.drawable.ic_live_forever),
            SongCategory.CLASSIC))
        addSong(Song(
            0,
            context.getString(R.string.classic_song19_artist),
            context.getString(R.string.classic_song19_title),
            context.getString(R.string.classic_song19_lyric),
            createImageBitMap(R.drawable.ic_life_on_marks),
            SongCategory.CLASSIC))
    }

    /**
     * Populates the markers table with markers using the
     * GoogleMapHelper to generate the latitudes and longitudes.
     */
    fun populateMarkersTable() {
        val currentMarkersSize = getUncollectedSongsFromPlaylist(SongCategory.CURRENT).size
        val classicMarkersSize = getUncollectedSongsFromPlaylist(SongCategory.CLASSIC).size
        val markerList = GoogleMapHelper(context).
            populateMarkersLatLong(currentMarkersSize, classicMarkersSize)
        this.writableDatabase?.execSQL("delete from "+ MarkersTable.TABLE_NAME)
        for (marker in markerList) {
            insertMarker(marker.position.latitude, marker.position.longitude, marker.title)
        }
    }

    companion object {
        const val DATABASE_NAME = "SongGuessingApp.db"
        var DATABASE_VERSION = 1
    }
}

object SongTable : BaseColumns {
    const val CURRENT_SONGS_TABLE_NAME = "currentPlaylistTable"
    const val CLASSIC_SONGS_TABLE_NAME = "classicPlaylistTable"
    const val COLUMN_ARTIST = "artistNames"
    const val COLUMN_TITLE = "songTitle"
    const val COLUMN_LYRIC = "lyric"
    const val COLUMN_ALBUM_COVER = "albumCover"
    const val COLUMN_CATEGORY = "songCategory"
    const val COLUMN_FAVOURED = "isFavourite"
    const val COLUMN_GUESSED = "isGuessed"
    const val COLUMN_COLLECTED = "isCollected"
}

object MarkersTable : BaseColumns {
    const val TABLE_NAME = "markersTable"
    const val COLUMN_LATITUDE = "latitude"
    const val COLUMN_LONGITUDE = "longitude"
    const val COLUMN_CATEGORY = "songCategory"
}



