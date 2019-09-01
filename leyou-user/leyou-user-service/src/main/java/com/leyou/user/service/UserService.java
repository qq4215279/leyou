package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.utils.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    StringRedisTemplate redisTemplate;

    static final String KEY_PREFIX = "user:code:phone";
    static final Logger logger = LoggerFactory.getLogger( UserService.class );

    public Boolean checkUserData(String data, Integer type) {

        User record = new User();
        if (type == 1){
            record.setUsername( data );

        }else if (type == 2){
            record.setPhone( data );
        }
        return this.userMapper.selectCount( record ) == 0;

    }


    public Boolean sendVerifyCode(String phone) {

        // 生成验证码
        String code = NumberUtils.generateCode( 6 );
        Map<String,String> msg = new HashMap<>(  );
        msg.put( "phone",phone );
        msg.put( "code",code );
        try {
            // 发送短信
            this.amqpTemplate.convertAndSend( "leyou.sms.exchange","sms.verify",msg );
            // 将code存入redis
            this.redisTemplate.opsForValue().set( KEY_PREFIX+phone,code, 5,TimeUnit.MINUTES );
            return true;
        } catch (AmqpException e) {
            logger.error("发送短信失败。phone：{}， code：{}", phone, code);
            return false;
        }

    }

    public Boolean register(User user, String code) {

        String cacheCode = this.redisTemplate.opsForValue().get( KEY_PREFIX+user.getPhone() );
        if (!StringUtils.equals( cacheCode,code )){
            return false;
        }
        // 生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        // 对密码加密
        user.setPassword( CodecUtils.md5Hex(user.getPassword(), salt) );
        // 强制设置不能指定的参数为null
        user.setId( null );
        user.setCreated( new Date(  ) );
        // 添加到数据库
        boolean b = this.userMapper.insertSelective(user) == 1;
        if(b){
            // 注册成功，删除redis中的记录
            this.redisTemplate.delete(KEY_PREFIX + user.getPhone());
        }
        return b;
    }

    public User queryUser(String username, String password) {
        // 查询
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);
        // 校验用户名
        if (user == null) {
            return null;
        }
        // 校验密码
        if (!user.getPassword().equals(CodecUtils.md5Hex(password, user.getSalt()))) {
            return null;
        }
        // 用户名密码都正确
        return user;
    }
}






