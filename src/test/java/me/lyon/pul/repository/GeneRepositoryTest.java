package me.lyon.pul.repository;

import me.lyon.pul.model.po.GenePO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class GeneRepositoryTest {
    @Resource
    GeneRepository repository;

    @Test
    public void testQuery() {
        List<GenePO> genePOS = repository.findAllByPulId("agar_1");
        System.out.println(genePOS);
    }
}
