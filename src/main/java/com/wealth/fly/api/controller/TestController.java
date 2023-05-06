package com.wealth.fly.api.controller;

import com.wealth.fly.core.Monitor;
import com.wealth.fly.core.constants.GridStatus;
import com.wealth.fly.core.dao.GridDao;
import com.wealth.fly.core.dao.GridLogDao;
import com.wealth.fly.core.entity.Grid;
import com.wealth.fly.core.entity.GridLog;
import com.wealth.fly.core.exchanger.Exchanger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

@RestController
@Slf4j
public class TestController {

    @Resource
    private GridLogDao gridLogDao;
    @Resource
    private GridDao gridDao;
    @Resource
    private Exchanger exchanger;

    @Value("${grid.default.strategy}")
    private Integer gridStrategy;

    @Value("${grid.inst.id}")
    private String activeInstId;

    @Value("${min.force.close.price.eth}")
    private String minForceClosePrice;

    @RequestMapping("/test")
    public Object proxyOkex() {

        return "test ok...";
    }

    @RequestMapping("/stop")
    public void stop() {
        log.info(">>>>>> stopAll");
        Monitor.stopAll = true;
    }

    @RequestMapping("/open")
    public void open() {
        log.info(">>>>> openAll");
        Monitor.stopAll = false;
    }

    @RequestMapping("/preview")
    public String preview() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        String markPrice = exchanger.getMarkPriceByInstId(activeInstId).getMarkPx();
        List<Grid> gridList = gridDao.listByStatusOrderByBuyPrice(Arrays.asList(GridStatus.ACTIVE.getCode(), GridStatus.PENDING.getCode()), 100);

        StringBuilder sb = new StringBuilder();
        sb.append("markPrice>>>>").append(markPrice).append("<br/>");
        sb.append("最近止盈点>>>>").append(CollectionUtils.isEmpty(gridList) ? "无" : gridList.get(0).getSellPrice()).append("<br/>");
        sb.append("强平风控>>>>").append(minForceClosePrice).append("<br/>");
        sb.append("stopAll>>>>").append(Monitor.stopAll).append("<br/>");
        sb.append("gridStatusLastFetchTime>>>>").append(Monitor.gridStatusLastFetchTime == null ? "无" : sdf.format(Monitor.gridStatusLastFetchTime)).append("<br/>");
        sb.append("markPriceLastFetchTime>>>>").append(Monitor.markPriceLastFetchTime == null ? "无" : sdf.format(Monitor.markPriceLastFetchTime)).append("<br/>");
        sb.append("<br/><br/>").append("==========操作日志=========<br/>");
        List<GridLog> gridLogs = gridLogDao.listRecentLogs(20);
        if (gridLogs != null) {
            for (GridLog gridLog : gridLogs) {
                sb.append(sdf.format(gridLog.getCreatedAt()) + ">>>>" + gridLog.getMessage() + "<br/>");
            }
        }

        sb.append("<br/><br/>").append("==========活跃网格=========<br/>");
        if (gridList != null) {
            for (Grid grid : gridList) {
                sb.append(String.format("%s-%s-%s-%s", grid.getBuyPrice(), grid.getSellPrice(), grid.getNum(), grid.getStatus())).append("<br/>");
            }
        }

        sb.append("<br/><br/>").append("==========排队网格=========<br/>");
        gridList = gridDao.listGrids(gridStrategy, new BigDecimal(markPrice), 20);
        if (gridList != null) {
            for (Grid grid : gridList) {
                sb.append(String.format("%s-%s-%s-%s", grid.getBuyPrice(), grid.getSellPrice(), grid.getNum(), grid.getStatus())).append("<br/>");
            }
        }

        return sb.toString();
    }


}
