package com.hazelcast.springHibernate;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Esref Ozturk <esrefozturk93@gmail.com> on 16.07.2014.
 */

public class App
{
    public static void main( String[] args )
    {
        ApplicationContext appContext =
                new ClassPathXmlApplicationContext("configuration.xml");

        StockBo stockBo = (StockBo)appContext.getBean("stockBo");

        /** insert **/
        //Stock stock = new Stock();
        //stock.setStockCode("7668");
        //stock.setStockName("HAIO");
        //stockBo.save(stock);

        /** select **/
        Stock stock2 = stockBo.findByStockCode("7668");
        System.out.println(stock2);

        /** update **/
       // stock2.setStockName("HAIO-1");
        //stockBo.update(stock2);

        /** delete **/
        //stockBo.delete(stock2);

        System.out.println("Done");
    }
}