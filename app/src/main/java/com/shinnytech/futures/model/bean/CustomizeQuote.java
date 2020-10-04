package com.shinnytech.futures.model.bean;

import com.shinnytech.futures.model.bean.futureinfobean.QuoteEntity;
import com.shinnytech.futures.model.engine.DataManager;
import com.shinnytech.futures.utils.MathUtils;

import static java.lang.Math.min;

public class CustomizeQuote {
    public String quoteName1;
    public String quoteName2;

    public QuoteEntity quote1;
    public QuoteEntity quote2;

    public CustomizeQuote(String name1,String name2){
        this.quoteName1 = name1;
        this.quoteName2 = name2;
        quote1 =  DataManager.getInstance().getRtnData().getQuote(name1);
        quote2 =  DataManager.getInstance().getRtnData().getQuote(name2);
    }

    public static String getName(String name1,String name2){
        String str1 = name1.substring(name1.indexOf(".")+1);
        String str2 = name2.substring(name2.indexOf(".")+1);
        return str1 +"-"+ str2;
    }

    public String getName(){
        String str1 = quoteName1.substring(quoteName1.indexOf(".")+1);
        String str2 = quoteName2.substring(quoteName2.indexOf(".")+1);
        return str1 +"-"+ str2;
    }

    public String getBidPrice(){
        if(quote1==null || quote2==null) return "";

        String bid_price_str = quote1.getBid_price1();
        String ask_price_str = quote2.getAsk_price1();

        if(bid_price_str.equals("") || ask_price_str.equals("")) return "";

        float bid_price = Float.parseFloat(bid_price_str);
        float ask_price = Float.parseFloat(ask_price_str);

        String ret = Float.toString(bid_price-ask_price);
        return MathUtils.subZeroAndDot(ret);
    }

    public String getBidVolume(){
        if(getBidPrice().equals("")) return "";

        Integer volume1 = Integer.parseInt(quote1.getBid_volume1());
        Integer volume2 = Integer.parseInt(quote2.getAsk_volume1());

        Integer volume = min(volume1,volume2);

        if(volume == 0) return "";

        return Integer.toString(volume);
    }

    public String getAskPrice(){
        if(quote1==null || quote2==null) return "";

        String ask_price_str = quote1.getAsk_price1();
        String bid_price_str = quote2.getBid_price1();

        if(bid_price_str.equals("") || ask_price_str.equals("")) return "";

        float ask_price = Float.parseFloat(ask_price_str);
        float bid_price = Float.parseFloat(bid_price_str);

        String ret = Float.toString(ask_price-bid_price);
        return MathUtils.subZeroAndDot(ret);
    }

    public String getAskVolume(){
        if(getAskPrice().equals("")) return "";

        Integer volume1 = Integer.parseInt(quote1.getAsk_volume1());
        Integer volume2 = Integer.parseInt(quote2.getBid_volume1());

        Integer volume = min(volume1,volume2);

        if(volume == 0) return "";

        return Integer.toString(volume);
    }
}
