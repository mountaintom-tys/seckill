package com.github.lyrric.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.mail.MailUtil;
import com.github.lyrric.conf.Config;
import com.github.lyrric.model.BusinessException;
import com.github.lyrric.model.Member;
import com.github.lyrric.ui.MainFrame;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import sun.rmi.runtime.Log;

import javax.swing.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimerTask;

public class RefreshVaccinesTask extends TimerTask {
    private MainFrame mainFrame;
    public static boolean membersOverdue = false;

    public RefreshVaccinesTask() {
    }

    public RefreshVaccinesTask(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void run() {
        System.out.println("RefreshVaccinesTask execute at :"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.scheduledExecutionTime()));
        mainFrame.refreshVaccines();
        //定时刷新成员信息检测是否过期
        if(!membersOverdue){
            try {
                List<Member> members = new HttpService().getMembers();
                System.out.println("refresh members:"+members.toString());
                mainFrame.appendMsg("refresh members:"+members.toString());
            } catch (Exception e) {
                String msg = "刷新成员信息："+e.getMessage();
                System.out.println(msg);
                mainFrame.appendMsg(msg);
                MailUtil.sendText(Config.mailTo,"秒苗刷新成员信息异常通知！",msg);
                membersOverdue = true;
            }
        }
    }
}
