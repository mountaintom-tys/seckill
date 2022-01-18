package com.github.lyrric.ui;

import cn.hutool.extra.mail.MailUtil;
import com.github.lyrric.conf.Config;
import com.github.lyrric.model.Area;
import com.github.lyrric.model.BusinessException;
import com.github.lyrric.model.TableModel;
import com.github.lyrric.model.VaccineList;
import com.github.lyrric.service.RefreshVaccinesTask;
import com.github.lyrric.service.SecKillService;
import com.github.lyrric.service.SeckillServiceTask;
import com.github.lyrric.util.CompareUtil;
import com.github.lyrric.util.ParseUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

/**
 * Created on 2020-07-21.
 *
 * @author wangxiaodong
 */
public class MainFrame extends JFrame {

    SecKillService service = new SecKillService();
    /**
     * 疫苗列表
     */
    private List<VaccineList> vaccines;

    //秒杀列表
    private List<VaccineList> preVaccines = new ArrayList<>();

    private List<Timer> seckillTimerList = new ArrayList<>();

    JButton autoDetectBtn;

    JTextField detectPeriodBox;

    Timer timer;

    JButton startBtn;

    JButton setCookieBtn;

    JButton setMemberBtn;

    JTable vaccinesTable;

    JButton refreshBtn;

    DefaultTableModel tableModel;

    JTextArea note;

    JComboBox<Area> provinceBox;

    JComboBox<Area> cityBox;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public MainFrame() {
        setLayout(null);
        setTitle("Just For Fun");
        setBounds(500 , 500, 680, 340);
        init();
        setLocationRelativeTo(null);
        setVisible(true);
        this.setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void init(){
        detectPeriodBox = new JTextField();
        detectPeriodBox.setEditable(false); // 设置输入框允许编辑
        detectPeriodBox.setColumns(4); // 设置输入框的长度为11个字符

        autoDetectBtn = new JButton("开启自动检测");
        autoDetectBtn.setEnabled(false);
        autoDetectBtn.addActionListener(e->toggleAutoDetect());
        startBtn = new JButton("开始");
        startBtn.setEnabled(false);
        startBtn.addActionListener(e -> startClick());

        setCookieBtn = new JButton("设置Cookie");
        setCookieBtn.addActionListener((e)->{
            ConfigDialog dialog = new ConfigDialog(this);
            dialog.setModal(true);
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setVisible(true);
            if(dialog.success()){
                setMemberBtn.setEnabled(true);
                autoDetectBtn.setEnabled(true);
                detectPeriodBox.setEditable(true);
                startBtn.setEnabled(true);
                refreshBtn.setEnabled(true);
                appendMsg("设置cookie成功");
            }

        });
        setMemberBtn = new JButton("选择成员");
        setMemberBtn.setEnabled(false);
        setMemberBtn.addActionListener((e)->{
            MemberDialog dialog = new MemberDialog(this);
            dialog.setModal(true);
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setVisible(true);
            if(dialog.success()){
                appendMsg("已设置成员：" + Config.memberName);
            }
        });

        refreshBtn = new JButton("刷新疫苗列表");
        refreshBtn.setEnabled(false);
        refreshBtn.addActionListener((e)->{
            refreshVaccines();
        });

        note = new JTextArea();
        note.append("日记记录：\r\n");
        note.setEditable(false);
        note.setAutoscrolls(true);
        note.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(note);
        scroll.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        String[] columnNames = { "id", "疫苗名称","医院名称","秒杀时间" };
        tableModel = new TableModel(new String[0][], columnNames);
        vaccinesTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(vaccinesTable);

        List<Area> areas = ParseUtil.getAreas();
        provinceBox  = new JComboBox<>(areas.toArray(new Area[0]));
        //itemListener
        ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if(ItemEvent.SELECTED == arg0.getStateChange()){
                    Area selectedItem = (Area)arg0.getItem();
                    cityBox.removeAllItems();
                    List<Area> children = ParseUtil.getChildren(selectedItem.getName());
                    for (Area child : children) {
                        cityBox.addItem(child);
                    }
                }

            }
        };
        provinceBox.addItemListener(itemListener);
        cityBox = new JComboBox<>( ParseUtil.getChildren("直辖市").toArray(new Area[0]));

        provinceBox.setBounds(20, 275, 100, 20);
        cityBox.setBounds(130, 275, 80, 20);

        JButton setAreaBtn = new JButton("确定");
        setAreaBtn.addActionListener(e->{
            Area selectedItem = (Area) cityBox.getSelectedItem();
            Config.regionCode = selectedItem.getValue();
            appendMsg("已选择地区:"+selectedItem.getName());
        });
        setAreaBtn.setBounds(220, 270, 70, 30);

        autoDetectBtn.setBounds(300,270,120,30);

        detectPeriodBox.setBounds(425,270,50,30);

        scrollPane.setBounds(10,10,460,200);

        startBtn.setBounds(370, 230, 100, 30);

        setCookieBtn.setBounds(20, 230, 100, 30);
        setMemberBtn.setBounds(130, 230, 100, 30);
        refreshBtn.setBounds(240, 230,120, 30);

        scroll.setBounds(480, 10, 180, 280);

        add(scrollPane);
        add(scroll);
        add(startBtn);
        add(setCookieBtn);
        add(setMemberBtn);
        add(refreshBtn);
        add(provinceBox);
        add(cityBox);
        add(setAreaBtn);
        add(autoDetectBtn);
        add(detectPeriodBox);
    }



    public void refreshVaccines(){
            try {
                int originNum = vaccines == null ? 0 : vaccines.size();
                vaccines = service.getVaccines().stream().sorted(new CompareUtil<>(this)).collect(Collectors.toList());
                System.out.println(vaccines.toString());
                appendMsg("refresh vaccines:"+vaccines.toString());
                //清除表格数据
                //通知模型更新
                ((DefaultTableModel)vaccinesTable.getModel()).getDataVector().clear();
                ((DefaultTableModel)vaccinesTable.getModel()).fireTableDataChanged();
                vaccinesTable.updateUI();//刷新表
                if(vaccines != null && !vaccines.isEmpty()){
                    for (VaccineList t : vaccines) {
                        String[] item = { t.getId().toString(), t.getVaccineName(),t.getName() ,t.getStartTime()};
                        tableModel.addRow(item);
                    }
                    if(Config.autoDetect && vaccines.size()>originNum){//只有开启自动检测并且有新疫苗活动且未过期才会设置自动定时抢购，并发邮件通知
                        List<VaccineList> tmpVaccines = new ArrayList<>();
                        tmpVaccines.addAll(vaccines);
                        tmpVaccines.removeAll(preVaccines);//找出新增的疫苗
                        //为新查询到的疫苗设置定时自动抢购
                        for (VaccineList tmpVaccine : tmpVaccines) {
                            //只有还未过期的疫苗才会设置自动抢购
                            if(sdf.parse(tmpVaccine.getStartTime()).after(new Date())){
                                Timer seckillTimer = new Timer();
                                Date startTime = sdf.parse(tmpVaccine.getStartTime());
                                //定时程序设置为提前500毫秒开始抢购
                                Date preStartTime = new Date(startTime.getTime()-500);
                                seckillTimer.schedule(new SeckillServiceTask(this,tmpVaccine.getId(),tmpVaccine.getStartTime()),preStartTime);
                                seckillTimerList.add(seckillTimer);
                                MailUtil.sendText(Config.mailTo,"九价疫苗秒苗通知！","秒苗列表有新抢购活动啦，冲冲冲啊！");
                            }
                        }
//                        JOptionPane.showMessageDialog(null,"查询到最新疫苗！");
//                        turnOffAutoDetect();
                    }
                }
                preVaccines = vaccines;
            } catch (IOException e) {
                e.printStackTrace();
                String msg ="refreshVaccines():未知错误";
                appendMsg(msg);
                MailUtil.sendText(Config.mailTo,"秒苗seckill异常通知",msg);
            } catch (BusinessException e) {
                String msg = "refreshVaccines():错误："+e.getErrMsg()+"，errCode"+e.getCode();
                appendMsg(msg);
                MailUtil.sendText(Config.mailTo,"秒苗seckill异常通知",msg);
            }catch (ParseException e){
                String msg = "refreshVaccines():日期解析异常！";
                appendMsg(msg);
                MailUtil.sendText(Config.mailTo,"秒苗seckill异常通知",msg);
            }
    }
    public void turnOffAutoDetect(){
        //取消自动刷新定时器
        timer.cancel();
        //取消所有自动抢购定时器
        for (Timer seckillTimer : seckillTimerList) {
            seckillTimer.cancel();
        }
        seckillTimerList.clear();
        Config.autoDetect=false;
        autoDetectBtn.setText("开启自动检测");
    }
    private void toggleAutoDetect(){
        if(Config.autoDetect){
            turnOffAutoDetect();
        }else{
            String periodText=detectPeriodBox.getText();
            if(StringUtils.isBlank(periodText)){
                JOptionPane.showMessageDialog(null,"请先设置自动检测间隔时间！单位:分");
                return;
            }
            try {
                if(vaccines!=null&&preVaccines!=null){
                    vaccines.clear();
                    preVaccines.clear();
                }
                Double period = Double.valueOf(periodText)*60*1000;
                timer = new Timer();
                timer.scheduleAtFixedRate(new RefreshVaccinesTask(this),new Date(),period.longValue());
                Config.autoDetect=true;
                autoDetectBtn.setText("关闭自动检测");
            } catch (NumberFormatException e) {
                appendMsg("错误：数字格式化异常");
            }
        }
    }
    public void startClick(){
        if(Config.cookie.isEmpty()){
            appendMsg("请配置cookie!!!");
            return ;
        }
        if(vaccinesTable.getSelectedRow() < 0){
            appendMsg("请选择要抢购的疫苗");
            return ;
        }
        int selectedRow = vaccinesTable.getSelectedRow();
        Integer id = vaccines.get(selectedRow).getId();
        String startTime = vaccines.get(selectedRow).getStartTime();
        start(id,startTime);
    }
    public void start(Integer id,String startTime){
        new Thread(()->{
            try {
                setCookieBtn.setEnabled(false);
                startBtn.setEnabled(false);
                setMemberBtn.setEnabled(false);
                service.startSecKill(id, startTime, this);
            } catch (ParseException | InterruptedException e) {
                appendMsg("解析开始时间失败");
                MailUtil.sendText(Config.mailTo,"秒苗seckill异常通知","start() 解析开始时间失败");
                e.printStackTrace();
            }finally {
                setCookieBtn.setEnabled(true);
                startBtn.setEnabled(true);
                setMemberBtn.setEnabled(true);
            }
        }).start();

    }


    public void appendMsg(String message){
        note.append(message);
        note.append("\r\n");
    }

    public void setStartBtnEnable(){
        startBtn.setEnabled(true);
        setCookieBtn.setEnabled(true);
        startBtn.setEnabled(true);
    }
}
