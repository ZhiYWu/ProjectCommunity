package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // 上传图片的资源存放路径
    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    // 更新的是当前用户的头像，当前用户是从hostholder中取
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;


    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "site/setting";
    }


    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        // SpringMVC提供的MultipartFile用来接收上传文件
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "您的图片格式不正确！");
            return "site/setting";
        }

        // 给图片生成随机的名字
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定该文件存放的路径
        // 先创建一个空的file准备向其中存东西
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常");
        }

        // 更新当前用户的头像的路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }


    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 浏览器响应的是一个图片（二进制数据），需要通过流手动通过response向浏览器输出，所以返回值设置为void
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 响应图片
        response.setContentType("/image" + suffix);

        try (
                // 输出流会由SpringMVC关闭
                ServletOutputStream os = response.getOutputStream();
                // 输入流由我们自己创建，所以需要由我们手动关闭
                // 但是在Java7后，将fis变量在try的括号中创建，会在finally中自动关闭
                FileInputStream fis = new FileInputStream(fileName);
        ) {
            // 输出不能一个一个字节输出，要建立一个缓冲区
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        }
    }


    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        String originalPassword = user.getPassword();
        oldPassword = oldPassword + user.getSalt();
        // 检查旧密码是否为空
        if (StringUtils.isBlank(oldPassword)) {
            model.addAttribute("checkPasswordError", "密码不能为空！");
            return "site/setting";
        }
        // 检查新密码是否为空
        if (StringUtils.isBlank(newPassword)) {
            model.addAttribute("checkNewPasswordError", "密码不能为空！");
            return "site/setting";
        }
        // 检查原密码正确性
        if (!CommunityUtil.md5(oldPassword).equals(originalPassword)) {
            model.addAttribute("checkPasswordError", "密码不正确，请重新输入密码！");
            return "site/setting";
        }
        // 检查新密码是否和旧密码是否一致
        if (CommunityUtil.md5(oldPassword).equals(CommunityUtil.md5(newPassword + user.getSalt()))) {
            model.addAttribute("checkNewPasswordError", "新旧密码不能一致，请重新输入密码！");
            return "site/setting";
        }

        // 将新密码存入数据库
        userService.updatePassword(user.getId(), CommunityUtil.md5(newPassword + user.getSalt()));

        return "redirect:/logout";
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注当前用户
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }
}
