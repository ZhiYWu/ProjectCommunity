package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    // tymeleaf有个核心的类，被Spring管理
    // 此处需要主动调用tymeleaf模板引擎
    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail() {
        mailClient.sendMail("mtkairo@163.com","test","Java代码测试发送邮件");
    }

    @Test
    public void testHtmlMail() {
        // 利用Context对模板引擎进行传参，Context在tymeleaf包中
        Context context = new Context();
        context.setVariable("username", "john");

        // 这里的content是返回的一个网页
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);
        mailClient.sendMail("mtkairo@163.com","html",content);
    }
}
