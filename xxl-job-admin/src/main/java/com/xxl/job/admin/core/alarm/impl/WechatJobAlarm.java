package com.xxl.job.admin.core.alarm.impl;

import com.xxl.job.admin.core.alarm.JobAlarm;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


@Component
public class WechatJobAlarm implements JobAlarm {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${alarm.wechatWebhook:default}")
    private String wechatWebhook;
    @Value("${alarm.xxlJobUrl:'http://localhost'}")
    private String xxlJobUrl;
    private String wechatUrl = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=";
    @Value("${alarm.wechatTitle:default}")
    private String wechatTitle;

    @Override
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog) {
        System.out.println("wechat ---------------------");
        if (null == wechatWebhook || "default".equals(wechatWebhook.trim()) || "".equals(wechatWebhook.trim())) {
            System.out.println("+++++++++获取到的企业微信的配置为空，跳过企业微信通知！+++++++++");
            return false;
        } try {
            HashMap<String, Object> map = new HashMap<>(2);
            map.put("msgtype", "markdown");
            HashMap<String, String> cmap = new HashMap<>(1);
            StringBuilder content = new StringBuilder("## XXL-JOB-Admin 通知:");
            content.append("\n### 任务失败通知：");
            content.append("\n> 所属环境：").append(wechatTitle);
            content.append("\n> 任务名称：").append(info.getJobDesc());
            content.append("\n> 执行器名称：").append(info.getExecutorHandler());
            content.append("\n> 执行器ip：").append(jobLog.getExecutorAddress());
            content.append("\n> 任务参数：").append(jobLog.getExecutorParam());
            content.append("\n> xxl-job地址：").append(xxlJobUrl);
            String msg = jobLog.getTriggerMsg();
            if (null != msg && !"".equals(msg.trim())) {
                msg = msg.substring(msg.lastIndexOf("</span><br>") + 11, msg.lastIndexOf("<br><br>"));
            }
            content.append(msg);
            content.append("\n >执行任务时间：").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            cmap.put("content", content.toString());
            map.put("markdown", cmap);
            String[] tokens = wechatWebhook.split(",");//根据，切分字符串
            for (int i = 0; i < tokens.length; i++) {
                wechatUrl.concat(tokens[i]);
                System.out.println("waring+++++++++++++++++++++++ 当前请求wechat地址为：" + wechatUrl.concat(tokens[i]));
                restTemplate.postForEntity(wechatUrl.concat(tokens[i]), map, Object.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}