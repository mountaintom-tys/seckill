package com.github.lyrric.service;

import com.github.lyrric.ui.MainFrame;

import java.util.TimerTask;

public class SeckillServiceTask extends TimerTask {
    private MainFrame mainFrame;
    private Integer id;
    private String startTime;

    public SeckillServiceTask() {
    }

    public SeckillServiceTask(MainFrame mainFrame, Integer id, String startTime) {
        this.mainFrame = mainFrame;
        this.id = id;
        this.startTime = startTime;
    }

    @Override
    public void run() {
        mainFrame.start(id,startTime);
    }
}
