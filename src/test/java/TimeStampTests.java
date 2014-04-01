import junit.framework.TestCase;
import me.zfei.clichatroom.utils.VectorTimeStamp;
import org.junit.Test;

/**
 * Created by zfei on 4/1/14.
 */
public class TimeStampTests extends TestCase {
    @Test
    public void testVectorTimeStampSelfIncrement() {
        VectorTimeStamp vts1 = new VectorTimeStamp(0, 2);
        vts1.increment();

        assertEquals(vts1.toString(), "[1,0]");
    }

    @Test
    public void testVectorTimeStampExternalIncrement() {
        VectorTimeStamp vts1 = new VectorTimeStamp(0, 2);
        VectorTimeStamp vts2 = new VectorTimeStamp(1, 2);

        vts1.increment();
        vts2.increment(vts1);

        assertEquals(vts2.toString(), "[1,1]");
    }

    @Test
    public void testVectorTimeStampExternalIncrementFromString() {
        VectorTimeStamp vts1 = new VectorTimeStamp(0, 2);
        VectorTimeStamp vts2 = new VectorTimeStamp(1, 2);

        vts1.increment();
        vts2.increment(vts1.toString());

        assertEquals(vts2.toString(), "[1,1]");
    }
}
