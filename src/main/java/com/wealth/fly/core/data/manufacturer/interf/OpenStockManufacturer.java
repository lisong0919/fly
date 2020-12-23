package com.wealth.fly.core.data.manufacturer.interf;



import com.wealth.fly.core.entity.OpenStock;
import java.math.BigDecimal;
import java.util.Map;


public interface OpenStockManufacturer extends DataManufacturer<OpenStock> {


    void manufact(OpenStock openStock, Map<String, BigDecimal> sectorValues);

}
