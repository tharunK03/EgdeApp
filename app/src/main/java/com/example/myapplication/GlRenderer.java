package com.example.myapplication;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GlRenderer implements GLSurfaceView.Renderer {

    private final MainActivity activity;
    private int textureId = 0;
    private int program = 0;
    private int positionHandle;
    private int texCoordHandle;
    private int textureUniform;
    private int frameWidth = 0;
    private int frameHeight = 0;

    private static final float[] VERTICES = {
            -1f, -1f,  0f, 1f,
             1f, -1f,  1f, 1f,
            -1f,  1f,  0f, 0f,
             1f,  1f,  1f, 0f
    };
    private final FloatBuffer vertexBuffer;

    public GlRenderer(MainActivity activity) {
        this.activity = activity;
        vertexBuffer = ByteBuffer.allocateDirect(VERTICES.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(VERTICES).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String vsh = "attribute vec2 aPos; attribute vec2 aTex; varying vec2 vTex; void main(){ vTex=aTex; gl_Position=vec4(aPos,0.0,1.0);}";
        String fsh = "precision mediump float; uniform sampler2D uTex; varying vec2 vTex; void main(){ gl_FragColor = texture2D(uTex, vTex); }";
        program = compileProgram(vsh, fsh);
        positionHandle = GLES20.glGetAttribLocation(program, "aPos");
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTex");
        textureUniform = GLES20.glGetUniformLocation(program, "uTex");

        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        textureId = tex[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        byte[] rgba = activity.getLastProcessedRgba();
        if (rgba != null && frameWidth > 0 && frameHeight > 0) {
            ByteBuffer buf = ByteBuffer.wrap(rgba);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, frameWidth, frameHeight, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        }

        GLES20.glUseProgram(program);
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer);
        GLES20.glEnableVertexAttribArray(positionHandle);
        vertexBuffer.position(2);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer);
        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glUniform1i(textureUniform, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void setFrameSize(int width, int height) {
        this.frameWidth = width;
        this.frameHeight = height;
    }

    private static int compileShader(int type, String src) {
        int s = GLES20.glCreateShader(type);
        GLES20.glShaderSource(s, src);
        GLES20.glCompileShader(s);
        return s;
    }

    private static int compileProgram(String vSrc, String fSrc) {
        int vs = compileShader(GLES20.GL_VERTEX_SHADER, vSrc);
        int fs = compileShader(GLES20.GL_FRAGMENT_SHADER, fSrc);
        int prog = GLES20.glCreateProgram();
        GLES20.glAttachShader(prog, vs);
        GLES20.glAttachShader(prog, fs);
        GLES20.glLinkProgram(prog);
        return prog;
    }
}


