package app.dayerize.imaging
import java.io.Closeable
import ai.onnxruntime.*
import java.nio.FloatBuffer
class SegmentationModel(model:ByteArray):Closeable {
    private val environment=OrtEnvironment.getEnvironment()
    private val session=OrtSession.SessionOptions().use { options->
        options.setIntraOpNumThreads(2);options.setInterOpNumThreads(1)
        environment.createSession(model,options)
    }
    fun predict(channels:FloatArray):FloatArray {
        require(channels.size==3*320*320 && channels.all { it.isFinite() })
        OnnxTensor.createTensor(environment,FloatBuffer.wrap(channels),longArrayOf(1,3,320,320)).use { input->
            session.run(mapOf(session.inputNames.first() to input)).use { outputs->
                val tensor=outputs[0] as OnnxTensor
                val mask=FloatArray(320*320);tensor.floatBuffer.get(mask)
                val low=mask.minOrNull()!!;val high=mask.maxOrNull()!!
                require(high-low>0.00001f) { "No separable subject" }
                return FloatArray(mask.size) { ((mask[it]-low)/(high-low)).coerceIn(0f,1f) }
            }
        }
    }
    override fun close() { session.close() }
}
