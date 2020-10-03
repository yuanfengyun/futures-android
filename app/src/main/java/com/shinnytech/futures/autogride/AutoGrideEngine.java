package com.shinnytech.futures.autogride;

import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.TradeEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.TDUtils;

import java.util.HashMap;
import java.util.HashSet;

class SingleGride{
    public String name="";
    public String exchange_id="";
    public String instructment_id="";
    public float gride=0;
    public boolean enable=false;
};

public class AutoGrideEngine {
    private HashMap<String,SingleGride> grideConfig = new HashMap<String,SingleGride>() ;
    private DataManager sDataManager = DataManager.getInstance();
    private HashSet<String> trades=new HashSet<String>();

    private static final AutoGrideEngine INSTANCE = new AutoGrideEngine();

    static public AutoGrideEngine getInstance(){
        return INSTANCE;
    }

    public void updateSchedule(String id,float gride,boolean enable){
        SingleGride item = grideConfig.get(id);
        if(item==null){
            item = new SingleGride();
            grideConfig.put(id,item);
        }
        item.instructment_id = id;
        item.gride = gride;
        item.enable = enable;
    }

    public void initTrades(){
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        for (TradeEntity trade :
                userEntity.getTrades().values()) {
            trades.add(trade.getTrade_id());
        }
    }

    public void RefreshTD(){
        UserEntity userEntity = sDataManager.getTradeBean().getUsers().get(sDataManager.USER_ID);
        if (userEntity == null) return;
        for (TradeEntity trade :
                userEntity.getTrades().values()) {
            if(trades.add(trade.getTrade_id())) continue;

            OrderEntity order = userEntity.getOrder(trade.getOrder_id());
            if(order.getDirection() != trade.getDirection()) continue;

            SingleGride item = grideConfig.get(trade.getInstrument_id());
            if(item==null || item.gride==0) continue;
            if(trade.getDirection()=="SELL"){
                TDUtils.trySell(trade.getExchange_id(),trade.getInstrument_id(),Float.parseFloat(order.getLimit_price())-item.gride,Integer.parseInt(trade.getVolume()));
            }else{
                TDUtils.tryBuy(trade.getExchange_id(),trade.getInstrument_id(),Float.parseFloat(order.getLimit_price())+item.gride,Integer.parseInt(trade.getVolume()));
            }
        }
    }
}
