package br.com.ysenerbyte.comandospro.gl

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

class Panel3DView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val panelRenderer = PanelRenderer()
    private var previousX = 0f
    private var previousY = 0f

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                panelRenderer.zoom = (panelRenderer.zoom * detector.scaleFactor).coerceIn(0.72f, 1.8f)
                return true
            }
        }
    )

    init {
        setEGLContextClientVersion(3)
        setRenderer(panelRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        preserveEGLContextOnPause = true
        contentDescription = "Painel elétrico tridimensional interativo. Arraste para girar e use dois dedos para ampliar."
    }

    fun setActiveComponent(index: Int) {
        panelRenderer.activeComponent = index.coerceIn(0, 6)
    }

    fun setEnergized(energized: Boolean) {
        panelRenderer.energized = energized
    }

    fun setVisibleComponents(count: Int) {
        panelRenderer.visibleComponents = count.coerceIn(0, 7)
    }

    fun setAutoRotate(enabled: Boolean) {
        panelRenderer.autoRotate = enabled
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                previousX = event.x
                previousY = event.y
                parent?.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> if (!scaleDetector.isInProgress && event.pointerCount == 1) {
                val dx = event.x - previousX
                val dy = event.y - previousY
                if (abs(dx) + abs(dy) > 1f) {
                    panelRenderer.rotationY += dx * 0.35f
                    panelRenderer.rotationX = (panelRenderer.rotationX + dy * 0.28f).coerceIn(-58f, 58f)
                    panelRenderer.autoRotate = false
                }
                previousX = event.x
                previousY = event.y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                performClick()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

private class PanelRenderer : GLSurfaceView.Renderer {
    @Volatile var rotationX = -12f
    @Volatile var rotationY = -18f
    @Volatile var zoom = 1f
    @Volatile var energized = false
    @Volatile var activeComponent = 4
    @Volatile var visibleComponents = 7
    @Volatile var autoRotate = true

    private var program = 0
    private var vao = 0
    private var vbo = 0
    private var viewportRatio = 1f
    private var contactorTravel = 0f

    private var mvpLocation = -1
    private var modelLocation = -1
    private var colorLocation = -1

    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val viewProjection = FloatArray(16)
    private val global = FloatArray(16)
    private val local = FloatArray(16)
    private val model = FloatArray(16)
    private val mvp = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.018f, 0.043f, 0.075f, 1f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        GLES30.glCullFace(GLES30.GL_BACK)

        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        mvpLocation = GLES30.glGetUniformLocation(program, "uMvp")
        modelLocation = GLES30.glGetUniformLocation(program, "uModel")
        colorLocation = GLES30.glGetUniformLocation(program, "uColor")

        val buffers = IntArray(1)
        GLES30.glGenVertexArrays(1, buffers, 0)
        vao = buffers[0]
        GLES30.glGenBuffers(1, buffers, 0)
        vbo = buffers[0]

        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        val vertexBuffer = CUBE_VERTICES.toFloatBuffer()
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            CUBE_VERTICES.size * Float.SIZE_BYTES,
            vertexBuffer,
            GLES30.GL_STATIC_DRAW
        )
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 6 * Float.SIZE_BYTES, 0)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(
            1,
            3,
            GLES30.GL_FLOAT,
            false,
            6 * Float.SIZE_BYTES,
            3 * Float.SIZE_BYTES
        )
        GLES30.glBindVertexArray(0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        viewportRatio = if (height == 0) 1f else width.toFloat() / height.toFloat()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        if (autoRotate) rotationY += 0.12f

        val targetTravel = if (energized) 1f else 0f
        contactorTravel += (targetTravel - contactorTravel) * 0.09f

        Matrix.perspectiveM(projection, 0, 43f, viewportRatio, 1f, 40f)
        val distance = 13.2f / zoom
        Matrix.setLookAtM(view, 0, 0f, 0.3f, distance, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(viewProjection, 0, projection, 0, view, 0)

        Matrix.setIdentityM(global, 0)
        Matrix.rotateM(global, 0, rotationX, 1f, 0f, 0f)
        Matrix.rotateM(global, 0, rotationY, 0f, 1f, 0f)

        GLES30.glUseProgram(program)
        GLES30.glBindVertexArray(vao)
        drawPanel()
        GLES30.glBindVertexArray(0)
    }

    private fun drawPanel() {
        val board = floatArrayOf(0.10f, 0.16f, 0.21f, 1f)
        val boardEdge = floatArrayOf(0.18f, 0.28f, 0.34f, 1f)
        val metal = floatArrayOf(0.42f, 0.50f, 0.54f, 1f)
        val wire = if (energized) {
            floatArrayOf(1f, 0.33f, 0.10f, 1f)
        } else {
            floatArrayOf(0.12f, 0.42f, 0.57f, 1f)
        }

        drawCube(0f, 0f, -0.48f, 7.4f, 7.2f, 0.30f, board)
        drawCube(-3.62f, 0f, -0.28f, 0.12f, 7.15f, 0.20f, boardEdge)
        drawCube(3.62f, 0f, -0.28f, 0.12f, 7.15f, 0.20f, boardEdge)
        drawCube(0f, 3.48f, -0.28f, 7.15f, 0.12f, 0.20f, boardEdge)
        drawCube(0f, -3.48f, -0.28f, 7.15f, 0.12f, 0.20f, boardEdge)

        listOf(2.35f, 0.25f, -1.95f).forEach { y ->
            drawCube(0f, y, -0.12f, 6.6f, 0.13f, 0.12f, metal)
            for (slot in -6..6) {
                drawCube(slot * 0.45f, y, -0.03f, 0.18f, 0.035f, 0.04f, board)
            }
        }

        if (visibleComponents >= 1) drawBreaker(-2.8f, 2.35f, 0)
        if (visibleComponents >= 2) drawPowerSupply(-1.65f, 2.30f, 1)
        if (visibleComponents >= 3) drawPlc(0.25f, 2.30f, 2)
        if (visibleComponents >= 4) drawSafetyRelay(2.60f, 2.30f, 3)
        if (visibleComponents >= 5) drawContactor(-1.90f, 0.22f, 4, energized)
        if (visibleComponents >= 6) drawContactor(0f, 0.22f, 5, false)
        if (visibleComponents >= 7) drawTerminals(2.45f, 0.20f, 6)

        if (visibleComponents >= 5) {
            drawWire(-2.80f, 1.55f, -1.90f, 1.00f, wire)
            drawWire(-1.90f, -0.62f, -1.90f, -1.92f, wire)
        }
        if (visibleComponents >= 6) drawWire(0f, -0.62f, 0f, -1.92f, wire)
        if (visibleComponents >= 7) drawWire(1.90f, 0.22f, 2.05f, 0.22f, wire)

        drawMotor(0f, -2.70f, energized && visibleComponents >= 5)
        drawIndicator(-2.95f, -2.85f, energized)
        drawIndicator(2.95f, -2.85f, false)
    }

    private fun drawBreaker(x: Float, y: Float, index: Int) {
        val body = selectedColor(index, floatArrayOf(0.84f, 0.87f, 0.88f, 1f))
        drawCube(x, y, 0.28f, 0.72f, 1.35f, 0.58f, body)
        drawCube(x, y + 0.12f, 0.61f, 0.28f, 0.48f, 0.15f, floatArrayOf(0.08f, 0.12f, 0.15f, 1f))
        terminalDots(x, y, 0.63f, 2)
    }

    private fun drawPowerSupply(x: Float, y: Float, index: Int) {
        val body = selectedColor(index, floatArrayOf(0.36f, 0.40f, 0.43f, 1f))
        drawCube(x, y, 0.26f, 1.10f, 1.45f, 0.55f, body)
        drawCube(x, y + 0.23f, 0.58f, 0.68f, 0.42f, 0.10f, floatArrayOf(0.10f, 0.13f, 0.15f, 1f))
        drawCube(x - 0.26f, y + 0.23f, 0.66f, 0.10f, 0.10f, 0.05f, floatArrayOf(0.18f, 0.95f, 0.48f, 1f))
    }

    private fun drawPlc(x: Float, y: Float, index: Int) {
        val body = selectedColor(index, floatArrayOf(0.16f, 0.34f, 0.47f, 1f))
        drawCube(x, y, 0.28f, 2.25f, 1.42f, 0.58f, body)
        drawCube(x - 0.67f, y, 0.61f, 0.63f, 0.95f, 0.09f, floatArrayOf(0.11f, 0.17f, 0.20f, 1f))
        for (i in 0..5) {
            val led = if (energized && i < 3) {
                floatArrayOf(0.18f, 1f, 0.47f, 1f)
            } else {
                floatArrayOf(0.12f, 0.24f, 0.25f, 1f)
            }
            drawCube(x + 0.15f + i * 0.22f, y + 0.40f, 0.64f, 0.09f, 0.09f, 0.05f, led)
        }
    }

    private fun drawSafetyRelay(x: Float, y: Float, index: Int) {
        val body = selectedColor(index, floatArrayOf(0.90f, 0.64f, 0.08f, 1f))
        drawCube(x, y, 0.27f, 0.72f, 1.43f, 0.56f, body)
        drawCube(x, y + 0.25f, 0.59f, 0.44f, 0.36f, 0.09f, floatArrayOf(0.12f, 0.15f, 0.16f, 1f))
        drawCube(x, y + 0.25f, 0.66f, 0.10f, 0.10f, 0.04f, floatArrayOf(0.22f, 0.96f, 0.45f, 1f))
    }

    private fun drawContactor(x: Float, y: Float, index: Int, active: Boolean) {
        val body = selectedColor(index, floatArrayOf(0.30f, 0.35f, 0.38f, 1f))
        drawCube(x, y, 0.32f, 1.20f, 1.52f, 0.65f, body)
        val travel = if (active) contactorTravel * 0.17f else 0f
        drawCube(
            x,
            y + 0.06f,
            0.72f - travel,
            0.74f,
            0.60f,
            0.18f,
            if (active) floatArrayOf(0.95f, 0.28f, 0.08f, 1f) else floatArrayOf(0.12f, 0.16f, 0.18f, 1f)
        )
        terminalDots(x, y, 0.70f, 3)
    }

    private fun drawTerminals(x: Float, y: Float, index: Int) {
        val base = selectedColor(index, floatArrayOf(0.20f, 0.34f, 0.42f, 1f))
        drawCube(x, y, 0.17f, 1.48f, 0.78f, 0.36f, base)
        for (i in 0..4) {
            val terminalColor = if (i == 4) {
                floatArrayOf(0.15f, 0.72f, 0.38f, 1f)
            } else {
                floatArrayOf(0.74f, 0.75f, 0.70f, 1f)
            }
            drawCube(x - 0.52f + i * 0.26f, y, 0.42f, 0.19f, 0.55f, 0.18f, terminalColor)
        }
    }

    private fun drawMotor(x: Float, y: Float, active: Boolean) {
        val motor = if (active) {
            floatArrayOf(0.12f, 0.63f, 0.82f, 1f)
        } else {
            floatArrayOf(0.16f, 0.31f, 0.39f, 1f)
        }
        drawCube(x, y, 0.24f, 1.75f, 0.90f, 0.67f, motor)
        drawCube(x - 1.02f, y, 0.24f, 0.30f, 0.30f, 0.95f, floatArrayOf(0.50f, 0.56f, 0.58f, 1f))
        val rotorOffset = if (active) contactorTravel * 0.05f else 0f
        drawCube(x + 0.98f, y + rotorOffset, 0.24f, 0.55f, 0.18f, 0.18f, floatArrayOf(0.62f, 0.67f, 0.70f, 1f))
    }

    private fun drawIndicator(x: Float, y: Float, on: Boolean) {
        drawCube(x, y, 0.24f, 0.48f, 0.48f, 0.30f, floatArrayOf(0.12f, 0.15f, 0.16f, 1f))
        drawCube(
            x,
            y,
            0.46f,
            0.25f,
            0.25f,
            0.12f,
            if (on) floatArrayOf(0.15f, 1f, 0.40f, 1f) else floatArrayOf(0.32f, 0.07f, 0.06f, 1f)
        )
    }

    private fun terminalDots(x: Float, y: Float, z: Float, count: Int) {
        val start = -(count - 1) * 0.18f
        repeat(count) { i ->
            val dx = start + i * 0.36f
            drawCube(x + dx, y + 0.58f, z, 0.13f, 0.13f, 0.08f, floatArrayOf(0.74f, 0.60f, 0.25f, 1f))
            drawCube(x + dx, y - 0.58f, z, 0.13f, 0.13f, 0.08f, floatArrayOf(0.74f, 0.60f, 0.25f, 1f))
        }
    }

    private fun drawWire(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        color: FloatArray
    ) {
        val middleX = (x1 + x2) / 2f
        val middleY = (y1 + y2) / 2f
        drawCube(middleX, y1, 0.03f, abs(x2 - x1).coerceAtLeast(0.06f), 0.06f, 0.06f, color)
        drawCube(x2, middleY, 0.03f, 0.06f, abs(y2 - y1).coerceAtLeast(0.06f), 0.06f, color)
    }

    private fun selectedColor(index: Int, normal: FloatArray): FloatArray =
        if (index == activeComponent) {
            floatArrayOf(
                (normal[0] + 0.24f).coerceAtMost(1f),
                (normal[1] + 0.22f).coerceAtMost(1f),
                (normal[2] + 0.05f).coerceAtMost(1f),
                1f
            )
        } else {
            normal
        }

    private fun drawCube(
        x: Float,
        y: Float,
        z: Float,
        width: Float,
        height: Float,
        depth: Float,
        color: FloatArray
    ) {
        Matrix.setIdentityM(local, 0)
        Matrix.translateM(local, 0, x, y, z)
        Matrix.scaleM(local, 0, width, height, depth)
        Matrix.multiplyMM(model, 0, global, 0, local, 0)
        Matrix.multiplyMM(mvp, 0, viewProjection, 0, model, 0)
        GLES30.glUniformMatrix4fv(mvpLocation, 1, false, mvp, 0)
        GLES30.glUniformMatrix4fv(modelLocation, 1, false, model, 0)
        GLES30.glUniform4fv(colorLocation, 1, color, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 36)
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertex = compileShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        val fragment = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
        val result = GLES30.glCreateProgram()
        GLES30.glAttachShader(result, vertex)
        GLES30.glAttachShader(result, fragment)
        GLES30.glLinkProgram(result)
        val status = IntArray(1)
        GLES30.glGetProgramiv(result, GLES30.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            val message = GLES30.glGetProgramInfoLog(result)
            GLES30.glDeleteProgram(result)
            error("OpenGL program link failed: $message")
        }
        GLES30.glDeleteShader(vertex)
        GLES30.glDeleteShader(fragment)
        return result
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        val status = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val message = GLES30.glGetShaderInfoLog(shader)
            GLES30.glDeleteShader(shader)
            error("OpenGL shader compile failed: $message")
        }
        return shader
    }

    private fun FloatArray.toFloatBuffer(): FloatBuffer = ByteBuffer
        .allocateDirect(size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(this@toFloatBuffer)
            position(0)
        }

    companion object {
        private const val VERTEX_SHADER = """
            #version 300 es
            layout(location = 0) in vec3 aPosition;
            layout(location = 1) in vec3 aNormal;
            uniform mat4 uMvp;
            uniform mat4 uModel;
            out vec3 vNormal;
            void main() {
                gl_Position = uMvp * vec4(aPosition, 1.0);
                vNormal = mat3(uModel) * aNormal;
            }
        """

        private const val FRAGMENT_SHADER = """
            #version 300 es
            precision mediump float;
            in vec3 vNormal;
            uniform vec4 uColor;
            out vec4 fragColor;
            void main() {
                vec3 light = normalize(vec3(-0.35, 0.70, 0.55));
                float diffuse = max(dot(normalize(vNormal), light), 0.0);
                float shade = 0.38 + diffuse * 0.62;
                fragColor = vec4(uColor.rgb * shade, uColor.a);
            }
        """

        private val CUBE_VERTICES = floatArrayOf(
            // Front
            -0.5f, -0.5f,  0.5f, 0f, 0f, 1f,   0.5f, -0.5f,  0.5f, 0f, 0f, 1f,   0.5f,  0.5f,  0.5f, 0f, 0f, 1f,
            -0.5f, -0.5f,  0.5f, 0f, 0f, 1f,   0.5f,  0.5f,  0.5f, 0f, 0f, 1f,  -0.5f,  0.5f,  0.5f, 0f, 0f, 1f,
            // Back
             0.5f, -0.5f, -0.5f, 0f, 0f,-1f,  -0.5f, -0.5f, -0.5f, 0f, 0f,-1f,  -0.5f,  0.5f, -0.5f, 0f, 0f,-1f,
             0.5f, -0.5f, -0.5f, 0f, 0f,-1f,  -0.5f,  0.5f, -0.5f, 0f, 0f,-1f,   0.5f,  0.5f, -0.5f, 0f, 0f,-1f,
            // Left
            -0.5f, -0.5f, -0.5f,-1f, 0f, 0f,  -0.5f, -0.5f,  0.5f,-1f, 0f, 0f,  -0.5f,  0.5f,  0.5f,-1f, 0f, 0f,
            -0.5f, -0.5f, -0.5f,-1f, 0f, 0f,  -0.5f,  0.5f,  0.5f,-1f, 0f, 0f,  -0.5f,  0.5f, -0.5f,-1f, 0f, 0f,
            // Right
             0.5f, -0.5f,  0.5f, 1f, 0f, 0f,   0.5f, -0.5f, -0.5f, 1f, 0f, 0f,   0.5f,  0.5f, -0.5f, 1f, 0f, 0f,
             0.5f, -0.5f,  0.5f, 1f, 0f, 0f,   0.5f,  0.5f, -0.5f, 1f, 0f, 0f,   0.5f,  0.5f,  0.5f, 1f, 0f, 0f,
            // Top
            -0.5f,  0.5f,  0.5f, 0f, 1f, 0f,   0.5f,  0.5f,  0.5f, 0f, 1f, 0f,   0.5f,  0.5f, -0.5f, 0f, 1f, 0f,
            -0.5f,  0.5f,  0.5f, 0f, 1f, 0f,   0.5f,  0.5f, -0.5f, 0f, 1f, 0f,  -0.5f,  0.5f, -0.5f, 0f, 1f, 0f,
            // Bottom
            -0.5f, -0.5f, -0.5f, 0f,-1f, 0f,   0.5f, -0.5f, -0.5f, 0f,-1f, 0f,   0.5f, -0.5f,  0.5f, 0f,-1f, 0f,
            -0.5f, -0.5f, -0.5f, 0f,-1f, 0f,   0.5f, -0.5f,  0.5f, 0f,-1f, 0f,  -0.5f, -0.5f,  0.5f, 0f,-1f, 0f
        )
    }
}
