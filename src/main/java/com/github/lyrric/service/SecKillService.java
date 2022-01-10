package com.github.lyrric.service;

import com.github.lyrric.conf.Config;
import com.github.lyrric.model.BusinessException;
import com.github.lyrric.model.VaccineList;
import com.github.lyrric.ui.MainFrame;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created on 2020-07-22.
 *
 * @author wangxiaodong
 */
public class SecKillService {

    private HttpService httpService;

    private final Logger logger = LogManager.getLogger(SecKillService.class);


    public SecKillService() {
        httpService = new HttpService();
    }

    /**
     * 秒杀开启
     */
    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public void startSecKill(Integer vaccineId, String startDateStr, MainFrame mainFrame) throws ParseException, InterruptedException {
        long startDate = convertDateToInt(startDateStr);
        long now = System.currentTimeMillis();
        String msg="";
        if(now + 5000 < startDate){
            msg="还未到开始时间，等待中......";
            logger.info(msg);
            mainFrame.appendMsg(msg);
            Thread.sleep(startDate - now - 5000);
        }
        String orderId = null;
        String st = "";
        do {
            try {
                httpService.log(vaccineId.toString());
                break;
            }catch (Exception e){
                msg=String.format("httpService.log,未知异常:%s，",e.getMessage());
                logger.warn(msg);
                mainFrame.appendMsg(msg);
            }
        }while (true);
        boolean isStUsed = true;
        now = System.currentTimeMillis();
        if(now + 1000 < startDate){
            msg="还未到获取st时间，等待中......";
            logger.info("还未到获取st时间，等待中......");
            mainFrame.appendMsg(msg);
            Thread.sleep(startDate - now - 1000);
        }
        do {
            try {
                st = httpService.getSt(vaccineId.toString());
                isStUsed = false;
                msg="成功获取到st";
                logger.info(msg);
                mainFrame.appendMsg(msg);
                break;
            }catch (Exception e){
                msg=String.format("获取st失败,未知异常:%s，", e.getMessage());
                logger.warn("获取st失败,未知异常:{}，", e.getMessage());
                mainFrame.appendMsg(msg);
            }
        }while (true);
        if(now + 500 < startDate){
            msg="还未到获取开始秒杀时间，等待中......";
            logger.info("还未到获取开始秒杀时间，等待中......");
            mainFrame.appendMsg(msg);
            Thread.sleep(startDate - now - 500);
        }
        do {
            try {
                //1.直接秒杀、获取秒杀资格
                long id = Thread.currentThread().getId();
                msg=String.format("Thread ID：%s，发送请求",id);
                logger.info(msg);
                mainFrame.appendMsg(msg);
                //加密参数
                if(isStUsed){
                    st = httpService.getSt(vaccineId.toString());
                    isStUsed = false;
                }
                orderId = httpService.secKill(vaccineId.toString(), "1", Config.memberId.toString(),
                        Config.idCard, st);
                msg=String.format("Thread ID：%s，抢购成功",id);
                logger.info(msg);
                mainFrame.appendMsg(msg);
                break;
            } catch (BusinessException e) {
                isStUsed = true;
                msg=String.format("Thread ID: %s, 抢购失败: %s",Thread.currentThread().getId(),e.getErrMsg());
                logger.info(msg);
                mainFrame.appendMsg(msg);
                //如果离开始时间XX秒后，则不再继续
                if (System.currentTimeMillis() > startDate + 1000 * 60 * 20) {
                    break;
                }
                if(e.getErrMsg().contains("没抢到")){
                    break;
                }
            }catch (ConnectTimeoutException | SocketTimeoutException socketTimeoutException ){
                msg=String.format("Thread ID: %s,抢购失败: 超时了",Thread.currentThread().getId());
                logger.error(msg);
                mainFrame.appendMsg(msg);
            } catch (Exception e) {
                e.printStackTrace();
                msg=String.format("Thread ID: %s，未知异常",Thread.currentThread().getId());
                logger.warn(msg);
                mainFrame.appendMsg(msg);
            }
        } while (true);


        //等待线程结束
        if (orderId != null) {
            if (mainFrame != null) {
                mainFrame.appendMsg("抢购成功，请登录约苗小程序查看");
            }
            logger.info("抢购成功，请登录约苗小程序查看");
        } else {
            if (mainFrame != null) {
                mainFrame.appendMsg("抢购失败");
            }
        }

    }
    public List<VaccineList> getVaccines() throws IOException, BusinessException {
        return httpService.getVaccineList();
    }
    /**
     *  将时间字符串转换为时间戳
     * @param dateStr yyyy-mm-dd格式
     * @return
     */
    public long convertDateToInt(String dateStr) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = format.parse(dateStr);
        return date.getTime();
    }


}
