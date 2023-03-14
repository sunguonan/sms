package com.ydl.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = SmsManageApplication.class)
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    //@Test
    public void testSendToRedis() {
        redisTemplate.convertAndSend("MYTOPIC", "im itnanaoshi123");
    }

}
