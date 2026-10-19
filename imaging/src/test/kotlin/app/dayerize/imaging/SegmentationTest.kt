package app.dayerize.imaging
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO
class SegmentationTest {
    @Test fun modelProducesForegroundAndBackground() {
        // PNG fixture keeps ImageIO independent from optional WebP plugins.
        val fixture=ImageIO.read(File("src/test/resources/laptop.png"))
        val channels=FloatArray(3*320*320)
        for(y in 0 until 320)for(x in 0 until 320){
            val pixel=fixture.getRGB(x*fixture.width/320,y*fixture.height/320)
            val alpha=(pixel ushr 24 and 255)/255f
            for(c in 0..2){val value=((pixel ushr (16-8*c) and 255)/255f)*alpha+(1-alpha)
                channels[c*320*320+y*320+x]=(value-floatArrayOf(.485f,.456f,.406f)[c])/floatArrayOf(.229f,.224f,.225f)[c]
            }
        }
        SegmentationModel(File("../app/src/main/assets/models/u2netp.onnx").readBytes()).use { model->
            val mask=model.predict(channels)
            assertEquals(320*320,mask.size)
            assertTrue(mask.all { it.isFinite() && it in 0f..1f })
            assertTrue(mask.count { it>.5f }>1000)
            assertTrue(mask.count { it<.1f }>1000)
        }
    }
}
