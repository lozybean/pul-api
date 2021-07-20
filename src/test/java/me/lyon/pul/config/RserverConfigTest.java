package me.lyon.pul.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RserverConfigTest {
    @Resource
    RConnection rConnection;

    @Test
    public void testConnection() throws RserveException, REXPMismatchException {
        double[] d = rConnection.eval("rnorm(10)").asDoubles();
        Assert.assertEquals(10, d.length);
    }
}
