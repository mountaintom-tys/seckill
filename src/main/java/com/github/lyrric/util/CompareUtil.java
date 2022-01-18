package com.github.lyrric.util;

import cn.hutool.extra.mail.MailUtil;
import com.github.lyrric.conf.Config;
import com.github.lyrric.model.VaccineList;
import com.github.lyrric.ui.MainFrame;

import javax.jws.Oneway;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Objects;

public class CompareUtil<T extends VaccineList> implements Comparator<T> {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private MainFrame mainFrame;

    public CompareUtil(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public int compare(T o1, T o2) {
        boolean after=true;
        try {
            after = sdf.parse(o1.getStartTime()).after(sdf.parse(o2.getStartTime()));
        } catch (ParseException e) {
            String msg = "CompareUtil():日期解析异常！";
            mainFrame.appendMsg(msg);
            MailUtil.sendText(Config.mailTo,"秒苗seckill异常通知",msg);
            e.printStackTrace();
        }
        return after?1:-1;
    }
}
