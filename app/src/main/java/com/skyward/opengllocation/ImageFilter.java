package com.skyward.opengllocation;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ImageFilter {

    static final int VERTEX_COORDINATE_2D_SIZE = 2;
    static final int TEXTURE_COORDINATE_2D_SIZE = 2;
    /**
     * 世界坐标系， 用于描边，确认形状，绘制一个矩形
     * (-1,1)      |     (1,1)
     * -----------------------
     * (-1,-1)    |     (1,-1)
     */
    static final float VERTEX_COORDINATE_RECTANGLE[] = {
            -1.0f, 1.0f, // //左上角坐标
            1.0f, 1.0f,  // 右上角坐标
            -1.0f, -1.0f, // 左下角坐标
            1.0f, -1.0f,  // 右下角坐标
    };

    static final float VERTEX_COORDINATE_QUARTER_TOP_LEFT_RECTANGLE[] = {
            -1.0f, 1.0f, // //左上角坐标
            0.0f, 1.0f,  // 右上角坐标
            -1.0f, 0.0f, // 左下角坐标
            0.0f, 0.0f,  // 右下角坐标
    };

    static final float TEXTURE_COORDINATE_QUARTER_TOP_LEFT[] = {
            0.0f, 0.0f, //左上角坐标
            0.5f, 0.0f, //右上角坐标
            0.0f, 0.5f, //左下角坐标
            0.5f, 0.5f, //右下角坐标
    };

    static final float TEXTURE_COORDINATE_QUARTER_CENTER[] = {
            0.25f, 0.25f, //左上角坐标
            0.75f, 0.25f, //右上角坐标
            0.25f, 0.75f, //左下角坐标
            0.75f, 0.75f, //右下角坐标
    };


    /**
     * 纹理坐标系
     * -----------------------
     * |(0,0)    |     (1,0)
     * |         |
     * |(0,1)    |     (1,1)
     * <p>
     * 逆时针旋转90度
     * -----------------------
     * |(1,0)    |     (1,1)
     * |         |
     * |(0,0)    |     (0,1)
     * <p>
     * 逆时针旋转180度
     * -----------------------
     * |(1,1)    |     (0,1)
     * |         |
     * |(1,0)    |     (0,0)
     * <p>
     * 逆时针旋转270度
     * -----------------------
     * |(0,1)    |     (0,0)
     * |         |
     * |(1,1)    |     (1,0)
     * 左右翻转
     * -----------------------
     * |(1,0)    |     (0,0)
     * |         |
     * |(1,1)    |     (0,1)
     */
    // 跟顶点坐标一一映射
    static final float TEXTURE_COORDINATE[] = {
            0.0f, 0.0f, //左上角坐标
            1.0f, 0.0f, //右上角坐标
            0.0f, 1.0f, //左下角坐标
            1.0f, 1.0f, //右下角坐标
    };
    // 逆时针旋转90度
    static final float TEXTURE_ROTATE_90_COORDINATE[] = {
            1.0f, 0.0f, //左上角坐标
            1.0f, 1.0f, //右上角坐标
            0.0f, 0.0f, //左下角坐标
            0.0f, 1.0f, //右下角坐标
    };
    // 逆时针旋转180度
    static final float TEXTURE_ROTATE_180_COORDINATE[] = {
            1.0f, 1.0f, //左上角坐标
            0.0f, 1.0f, //右上角坐标
            1.0f, 0.0f, //左下角坐标
            0.0f, 0.0f, //右下角坐标
    };
    // 逆时针旋转270度
    static final float TEXTURE_ROTATE_270_COORDINATE[] = {
            0.0f, 1.0f, //左上角坐标
            0.0f, 0.0f, //右上角坐标
            1.0f, 1.0f, //左下角坐标
            1.0f, 0.0f, //右下角坐标
    };
    // 原图左右翻转
    static final float TEXTURE_FLIP_COORDINATE[] = {
            1.0f, 0.0f, //左上角坐标
            0.0f, 0.0f, //右上角坐标
            1.0f, 1.0f, //左下角坐标
            0.0f, 1.0f, //右下角坐标
    };
    // 原图逆时针旋转270度，再左右翻转
    static final float TEXTURE_FLIP_270_COORDINATE[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    private String mVertexShader;
    private String mFragmentShader;

    private FloatBuffer mPositionBuffer;
    private FloatBuffer mTextureCubeBuffer;

    protected int mProgramId;
    protected int mPosition;
    protected int inputTextureBuffer;
    protected int mInputTexture;

    public ImageFilter(Context context) {
        this(OpenGLUtils.readRawTextFile(context, R.raw.base_vert), OpenGLUtils.readRawTextFile(context, R.raw.base_frag));
    }

    public ImageFilter(String vertexShader, String fragmentShader) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
    }

    float[] textureCoordinate = TEXTURE_COORDINATE;
    float[] vertexCoordinateArray = VERTEX_COORDINATE_RECTANGLE;

    public void loadVertex() {
        mPositionBuffer = ByteBuffer.allocateDirect(vertexCoordinateArray.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mPositionBuffer.put(vertexCoordinateArray).position(0);

        mTextureCubeBuffer = ByteBuffer.allocateDirect(textureCoordinate.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureCubeBuffer.put(textureCoordinate).position(0);
    }

    public void setVertexCoordinateArray(float[] vertexCoordinateArray) {
        this.vertexCoordinateArray = vertexCoordinateArray;
        mPositionBuffer.clear();
        mPositionBuffer.put(vertexCoordinateArray).position(0);
    }

    public void setTextureCoordinate(float[] textureCoordinate) {
        this.textureCoordinate = textureCoordinate;
        mTextureCubeBuffer.clear();
        mTextureCubeBuffer.put(textureCoordinate).position(0);
    }

    public void initShader() {
        mProgramId = OpenGLUtils.loadProgram(mVertexShader, mFragmentShader);
        mPosition = GLES20.glGetAttribLocation(mProgramId, "position");
        mInputTexture = GLES20.glGetUniformLocation(mProgramId, "inputImageTexture");
        inputTextureBuffer = GLES20.glGetAttribLocation(mProgramId,
                "inputTextureCoordinate");
    }

    public int init(Bitmap bitmap) {
        loadVertex();
        initShader();
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        return initTexture(bitmap);
    }

    private int initTexture(Bitmap bitmap) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        return textures[0];
    }

    public void drawFrame(int glTextureId) {

        GLES20.glUseProgram(mProgramId);
        mPositionBuffer.position(0);
        // *4 GL_FLOAT占四个字节
        GLES20.glVertexAttribPointer(mPosition, VERTEX_COORDINATE_2D_SIZE, GLES20.GL_FLOAT,
                false, VERTEX_COORDINATE_2D_SIZE * 4, mPositionBuffer);
        GLES20.glEnableVertexAttribArray(mPosition);

        mTextureCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(inputTextureBuffer, TEXTURE_COORDINATE_2D_SIZE, GLES20.GL_FLOAT,
                false, VERTEX_COORDINATE_2D_SIZE * 4, mTextureCubeBuffer);
        GLES20.glEnableVertexAttribArray(inputTextureBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureId);
        GLES20.glUniform1i(mInputTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0,
                vertexCoordinateArray.length / VERTEX_COORDINATE_2D_SIZE);

        GLES20.glDisableVertexAttribArray(mPosition);
        GLES20.glDisableVertexAttribArray(inputTextureBuffer);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glDisable(GLES20.GL_BLEND);
    }

}
