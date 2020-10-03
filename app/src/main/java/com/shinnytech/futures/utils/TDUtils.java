package com.shinnytech.futures.utils;

import com.shinnytech.futures.application.BaseApplication;
import com.shinnytech.futures.model.bean.accountinfobean.OrderEntity;
import com.shinnytech.futures.model.bean.accountinfobean.PositionEntity;
import com.shinnytech.futures.model.bean.accountinfobean.UserEntity;
import com.shinnytech.futures.model.engine.DataManager;

import java.util.ArrayList;
import java.util.List;

import static com.shinnytech.futures.constants.AmpConstants.AMP_EVENT_PRICE_TYPE_VALUE_NUMBER;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_BUY;
import static com.shinnytech.futures.constants.TradeConstants.DIRECTION_SELL;
import static com.shinnytech.futures.constants.TradeConstants.OFFSET_CLOSE;
import static com.shinnytech.futures.constants.TradeConstants.OFFSET_CLOSE_HISTORY;
import static com.shinnytech.futures.constants.TradeConstants.OFFSET_CLOSE_TODAY;
import static com.shinnytech.futures.constants.TradeConstants.OFFSET_OPEN;
import static com.shinnytech.futures.constants.TradeConstants.PRICE_TYPE_LIMIT;
import static com.shinnytech.futures.constants.TradeConstants.STATUS_ALIVE;
import static com.shinnytech.futures.constants.TradeConstants.USER_PRICE;

/**
 * Created on 12/4/17.
 * Created by chenli.
 * Description: .
 */

public class TDUtils {
    public static boolean isVisitor(String name, String password){
        if (name.matches("^游客_[0-9]{8}$") && password.matches("^游客_[0-9]{8}$"))return true;
        else return false;
    }

    public static  List<OrderEntity> getOrdersByInstrument(String exchange_id, String instrument_id) {
        List<OrderEntity> orders = new ArrayList<>();

        UserEntity userEntity = DataManager.getInstance().getCurrentUser();
        if(null == userEntity) return orders;
        for (OrderEntity orderEntity :
                userEntity.getOrders().values()) {
            String order_id = orderEntity.getOrder_id();
            String exId = orderEntity.getExchange_id();
            String insId = orderEntity.getInstrument_id();
            String offset = orderEntity.getOffset();
            String status = orderEntity.getStatus();
            if (STATUS_ALIVE.equals(status) && exchange_id.equals(exId) && instrument_id.equals(insId)) {
                orders.add(orderEntity);
            }
        }
        return orders;
    }

    public static void cancelOrderByInstrumentAndPrice(String exchange_id, String instrument_id, float price){
        List<OrderEntity> orders = TDUtils.getOrdersByInstrument(exchange_id,instrument_id);
        for(OrderEntity orderEntity : orders){
            if(orderEntity.getLimit_price() == Float.toString(price)){
                BaseApplication.getmTDWebSocket().sendReqCancelOrder(orderEntity.getOrder_id());
            }
        }
    }

    public static void tryBuy(String exchange_id, String instrument_id, float price, int volume){
        UserEntity userEntity = DataManager.getInstance().getCurrentUser();
        if(null == userEntity) return ;
        PositionEntity pe = userEntity.getPosition(instrument_id);
        if(pe == null) return;

        // 优先平空
        int volume_short = Integer.parseInt(pe.getVolume_short());
        int volume_short_frozen = Integer.parseInt(pe.getVolume_short_frozen());
        int valid_short = volume_short - volume_short_frozen;
        if(valid_short >0){
            int close_volume = volume;
            if(valid_short < volume){
                close_volume = valid_short;
                volume = volume - valid_short;
            }else{
                volume = 0;
            }
            BaseApplication.getmTDWebSocket().sendReqInsertOrder(exchange_id, instrument_id, DIRECTION_BUY, OFFSET_CLOSE, close_volume, PRICE_TYPE_LIMIT, price, AMP_EVENT_PRICE_TYPE_VALUE_NUMBER);
        }

        if(volume<=0) return;

        BaseApplication.getmTDWebSocket().sendReqInsertOrder(exchange_id, instrument_id, DIRECTION_BUY, OFFSET_OPEN, volume, PRICE_TYPE_LIMIT, price, AMP_EVENT_PRICE_TYPE_VALUE_NUMBER);
    }

    public static void trySell(String exchange_id, String instrument_id, float price, int volume){
        UserEntity userEntity = DataManager.getInstance().getCurrentUser();
        if(null == userEntity) return ;
        PositionEntity pe = userEntity.getPosition(instrument_id);
        if(pe == null) return;

        // 优先平多
        int volume_long = Integer.parseInt(pe.getVolume_long());
        int volume_long_frozen = Integer.parseInt(pe.getVolume_long_frozen());
        int valid_long = volume_long - volume_long_frozen;
        if(valid_long >0){
            int close_volume = volume;
            if(valid_long < volume){
                close_volume = valid_long;
                volume = volume - valid_long;
            }else{
                volume = 0;
            }
            BaseApplication.getmTDWebSocket().sendReqInsertOrder(exchange_id, instrument_id, DIRECTION_SELL, OFFSET_CLOSE, close_volume, PRICE_TYPE_LIMIT, price, AMP_EVENT_PRICE_TYPE_VALUE_NUMBER);
        }

        if(volume<=0) return;

        BaseApplication.getmTDWebSocket().sendReqInsertOrder(exchange_id, instrument_id, DIRECTION_SELL, OFFSET_OPEN, volume, PRICE_TYPE_LIMIT, price, AMP_EVENT_PRICE_TYPE_VALUE_NUMBER);
    }
}
