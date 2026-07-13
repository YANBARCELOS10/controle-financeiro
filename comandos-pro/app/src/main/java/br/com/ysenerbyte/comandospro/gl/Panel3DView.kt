package br.com.ysenerbyte.comandospro.gl

import android.content.Context
import android.opengl.GLES20
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

    var onRendererError: ((String) -> Unit)? = null

    private val panelRenderer = PanelRenderer { message ->
        post { onRendererError?.invoke(message) }
    }
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
        // ES 2.0 keeps the same real-time 3D scene while avoiding driver-specific
        // failures seen on a few Android 15/16 devices when creating ES 3 VAOs.
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
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

private class PanelRenderer(
    private val onError: (String) -> Unit
) : GLSurfaceView.Renderer {
    @Volatile var rotationX = -12f
    @Volatile var rotationY = -18f
    @Volatile var zoom = 1f
    @Volatile var energized = false
    @Volatile var activeComponent = 4
    @Volatile var visibleComponents = 7
    @Volatile var autoRotate = true

    private var program = 0
    private var vbo = 0
    private var viewportRatio = 1f
    private var contactorTravel = 0f

    private var mvpLocation = -1
    private var modelLocation = -1
    private var colorLocation = -1
    private var positionLocation = -1
    private var normalLocation = -1
    private var ready = false
    private var failureReported = false

    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val viewProjection = FloatArray(16)
    private val global = FloatArray(16)
    private val local = FloatArray(16)
    private val model = FloatArray(16)
    private val mvp = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        try {
            GLES20.glClearColor(0.018f, 0.043f, 0.075f, 1f)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_CULL_FACE)
            GLES20.glCullFace(GLES20.GL_BACK)

            program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
            mvpLocation = GLES20.glGetUniformLocation(program, "uMvp")
            modelLocation = GLES20.glGetUniformLocation(program, "uModel")
            colorLocation = GLES20.glGetUniformLocation(program, "uColor")
            positionLocation = GLES20.glGetAttribLocation(program, "aPosition")
            normalLocation = GLES20.glGetAttribLocation(program, "aNormal")
            checkLocations()

            val buffers = IntArray(1)
            GLES20.glGenBuffers(1, buffers, 0)
            vbo = buffers[0]
            check(vbo != 0) { "O driver não criou o buffer 3D." }

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
            val vertexBuffer = CUBE_VERTICES.toFloatBuffer()
            GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                CUBE_VERTICES.size * Float.SIZE_BYTES,
                vertexBuffer,
                GLES20.GL_STATIC_DRAW
            )
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
            checkGlError("inicialização")
            ready = true
        } catch (failure: Throwable) {
            reportFailure(failure)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        viewportRatio = if (height == 0) 1f else width.toFloat() / height.toFloat()
    }

    override fun onDrawFrame(gl: GL10?) {
        try {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            if (!ready) return
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

            GLES20.glUseProgram(program)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
            GLES20.glEnableVertexAttribArray(positionLocation)
            GLES20.glVertexAttribPointer(
                positionLocation,
                3,
                GLES20.GL_FLOAT,
                false,
                6 * Float.SIZE_BYTES,
                0
            )
            GLES20.glEnableVertexAttribArray(normalLocation)
            GLES20.glVertexAttribPointer(
                normalLocation,
                3,
                GLES20.GL_FLOAT,
                false,
                6 * Float.SIZE_BYTES,
                3 * Float.SIZE_BYTES
            )
            drawPanel()
            GLES20.glDisableVertexAttribArray(positionLocation)
            GLES20.glDisableVertexAttribArray(normalLocation)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        } catch (failure: Throwable) {
            reportFailure(failure)
        }
    }

    private fun checkLocations() {
        check(mvpLocation >= 0 && modelLocation >= 0 && colorLocation >= 0) {
            "O driver não disponibilizou os controles do sombreador."
        }
        check(positionLocation >= 0 && normalLocation >= 0) {
            "O driver não disponibilizou os atributos do modelo."
        }
    }

    private fun checkGlError(stage: String) {
        val errorCode = GLES20.glGetError()
        check(errorCode == GLES20.GL_NO_ERROR) {
            "Falha gráfica durante $stage (código $errorCode)."
        }
    }

    private fun reportFailure(failure: Throwable) {
        ready = false
        if (failureReported) return
        failureReported = true
        onError(failure.message?.take(180) ?: "Falha do driver gráfico.")
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
        GLES20.glUniformMatrix4fv(mvpLocation, 1, false, mvp, 0)
        GLES20.glUniformMatrix4fv(modelLocation, 1, false, model, 0)
        GLES20.glUniform4fv(colorLocation, 1, color, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertex = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragment = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        val result = GLES20.glCreateProgram()
        check(result != 0) { "O driver não criou o programa gráfico." }
        GLES20.glAttachShader(result, vertex)
        GLES20.glAttachShader(result, fragment)
        GLES20.glLinkProgram(result)
        val status = IntArray(1)
        GLES20.glGetProgramiv(result, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            val message = GLES20.glGetProgramInfoLog(result)
            GLES20.glDeleteProgram(result)
            throw IllegalStateException("Falha ao preparar o painel 3D: ${message.take(120)}")
        }
        GLES20.glDeleteShader(vertex)
        GLES20.glDeleteShader(fragment)
        return result
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        check(shader != 0) { "O driver não criou o sombreador 3D." }
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val message = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            throw IllegalStateException("Falha ao iniciar o painel 3D: ${message.take(120)}")
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
        private val VERTEX_SHADER = """
            attribute vec3 aPosition;
            attribute vec3 aNormal;
            uniform mat4 uMvp;
            uniform mat4 uModel;
            varying vec3 vNormal;
            void main() {
                gl_Position = uMvp * vec4(aPosition, 1.0);
                vNormal = (uModel * vec4(aNormal, 0.0)).xyz;
            }
        """.trimIndent()

        private val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec3 vNormal;
            uniform vec4 uColor;
            void main() {
                vec3 light = normalize(vec3(-0.35, 0.70, 0.55));
                float diffuse = max(dot(normalize(vNormal), light), 0.0);
                float shade = 0.38 + diffuse * 0.62;
                gl_FragColor = vec4(uColor.rgb * shade, uColor.a);
            }
        """.trimIndent()

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
