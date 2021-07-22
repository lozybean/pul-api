package me.lyon.pul.repository;

import me.lyon.pul.model.po.PulPO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class PulRepositoryTest {
    @Resource
    PulRepository repository;

    @Test
    public void test() {
        Optional<PulPO> po = repository.findById("agar_1");
        Assert.assertTrue(po.isPresent());
        System.out.println(po.get());
        System.out.println(po.get().getSpecies());
        System.out.println(po.get().getContents());
    }
}
