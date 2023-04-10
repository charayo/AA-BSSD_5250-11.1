package com.example.videoview

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.VideoView
import androidx.core.view.GestureDetectorCompat

private const val VIDEO_PATH = "video_path"

class VideoViewFragment : Fragment() {
    private var videoPath: String? = null
    private lateinit var paths: Array<String>
    private var videosMP: MediaPlayer? = null
    private lateinit var videoView: VideoView
    private lateinit var mDetector: GestureDetectorCompat
    private var currentVideoIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            paths = arguments?.getStringArray(VIDEO_PATH) as Array<String>
            videoPath = paths[currentVideoIndex]

        Log.d("video","video is: $videoPath")
        mDetector = GestureDetectorCompat(requireContext(), VideoGestureListener())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
            videoView =VideoView(context).apply {
            setVideoPath(videoPath)
            start()
            setOnTouchListener(object: View.OnTouchListener{
                override fun onTouch(p0: View?, p1: MotionEvent): Boolean {
                    mDetector.onTouchEvent(p1)
                    return true
                }
            })
                setOnPreparedListener(object:MediaPlayer.OnPreparedListener{
                    override fun onPrepared(p0: MediaPlayer?) {
                        videosMP = p0
                    }
                })
        }

        return videoView
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(paths: Array<String>) =
            VideoViewFragment().apply {
                arguments = Bundle().apply {
                    putStringArray(VIDEO_PATH, paths)
                }
            }
    }

    private inner class VideoGestureListener: GestureDetector.SimpleOnGestureListener(){
        override fun onDoubleTap(e: MotionEvent): Boolean {
            videosMP?.setVolume(0F,0F)
            if(videoView.isPlaying){
                videoView.pause()
            }else{
                videoView.start()
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            currentVideoIndex ++
            if(currentVideoIndex > (paths.size-1)){
                currentVideoIndex = 0
            }
            videoPath = paths[currentVideoIndex]
            videoView.setVideoPath(videoPath)
            videoView.start()
        }
        override  fun onFling(
            event1: MotionEvent,
            event2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean{
            Log.d("Frag", "onFling: ${event1.x} ${event1.y} ${event2.x} ${event2.y} ")
            //to know when to increase volume or fastseek
            if((event1.x - event2.x) > (event1.y - event2.y)){
                var pos = videoView.currentPosition
                val amt = 10000
                if(velocityX < 0){
                    pos -= amt
                    if(pos < 0){
                        pos = 0
                    }
                    videoView.seekTo(pos)
                }else{
                    pos += amt
                    videoView.seekTo(pos)
                }
            }else{
                if(velocityY > 0){
                    //when you fling down
                    val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val newVolume = (currentVolume - maxVolume * 0.1).coerceAtLeast(0.0).toInt()
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                }
                else{
                    val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toDouble()
                    val newVolume = (currentVolume + maxVolume * 0.1).coerceAtMost(maxVolume).toInt()
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
                }
            }





            return true
        }
    }
}