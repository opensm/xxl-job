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

    @Value("${alarm.wechatWebhook}")
    private String WechatWebhook;
    @Value("${alarm.wechatTitle:default}")
    private String wechatTitle;

    @Override
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog) {
        System.out.println("wechat ---------------------");
        try {
            HashMap<String, Object> map = new HashMap<>(2);
            map.put("msgtype", "text");
            HashMap<String, String> cmap = new HashMap<>(1);
            StringBuilder content = new StringBuilder("任务失败:");
            content.append("{所属环境：").append(wechatTitle);
            content.append(",任务名称：").append(info.getJobDesc());
            content.append(",执行器名称：").append(info.getExecutorHandler());
            content.append(",执行器ip：").append(jobLog.getExecutorAddress());
            content.append(",任务参数：").append(jobLog.getExecutorParam());
            String msg = jobLog.getTriggerMsg();
            if(null != msg && !"".equals(msg.trim())){
                msg = msg.substring(msg.lastIndexOf("</span><br>")+11,msg.lastIndexOf("<br><br>"));
            }
            content.append(msg);
            content.append(",执行任务时间：").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("}");
            cmap.put("content",content.toString());
            map.put("text", cmap);
            restTemplate.postForEntity(WechatWebhook, map, Object.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}