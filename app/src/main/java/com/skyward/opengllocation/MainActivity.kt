package com.skyward.opengllocation

import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.skyward.opengllocation.databinding.ActivityMainBinding
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    lateinit var binding: ActivityMainBinding
    private var mSurfaceView: GLSurfaceView? = null
    private var mTextureId = -1
    lateinit var filter: ImageFilter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mSurfaceView = GLSurfaceView(this)
        mSurfaceView!!.setEGLContextClientVersion(2)
        binding.main.addView(mSurfaceView)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.demo)
        filter = ImageFilter(this)
        mSurfaceView!!.setRenderer(object : GLSurfaceView.Renderer {
            override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
                mTextureId = filter.init(bitmap)
            }

            override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
                Log.d(
                    TAG,
                    "onSurfaceChanged() called with: gl = $gl, width = $width, height = $height"
                )
            }

            override fun onDrawFrame(gl: GL10) {
                filter.drawFrame(mTextureId)
            }
        })

    }

    private val resourceId2TextureCoordinateMap = mutableMapOf<Int, FloatArray>().apply {
        put(R.id.full_normal_vertex, ImageFilter.TEXTURE_COORDINATE)
        put(R.id.rotate_90, ImageFilter.TEXTURE_ROTATE_90_COORDINATE)
        put(R.id.rotate_180, ImageFilter.TEXTURE_ROTATE_180_COORDINATE)
        put(R.id.rotate_270, ImageFilter.TEXTURE_ROTATE_270_COORDINATE)
        put(R.id.flip, ImageFilter.TEXTURE_FLIP_COORDINATE)
        put(R.id.rotate_270_flip, ImageFilter.TEXTURE_FLIP_270_COORDINATE)
        put(R.id.clip_top_left_quarter, ImageFilter.TEXTURE_COORDINATE_QUARTER_TOP_LEFT)
        put(R.id.clip_center_quarter, ImageFilter.TEXTURE_COORDINATE_QUARTER_CENTER)
    }

    private val resourceId2VertexCoordinateMap = mutableMapOf<Int, FloatArray>().apply {
        put(R.id.top_left_quarter_rectangle, ImageFilter.VERTEX_COORDINATE_QUARTER_TOP_LEFT_RECTANGLE)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_image_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val textureCoordinateArray =
            resourceId2TextureCoordinateMap[item.itemId] ?: ImageFilter.TEXTURE_COORDINATE
        val vertexCoordinateArray =
            resourceId2VertexCoordinateMap[item.itemId] ?: ImageFilter.VERTEX_COORDINATE_RECTANGLE
        return if (textureCoordinateArray != null) {
            filter.setTextureCoordinate(textureCoordinateArray)
            filter.setVertexCoordinateArray(vertexCoordinateArray)
            mSurfaceView?.requestRender()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}