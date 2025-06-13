/**
  * Copyright 2025 json.cn 
  */
package com.xhf.leetcode.plugin.model;

/**
 * Auto-generated: 2025-06-13 19:23:11
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/
 */
public class GlobalLocation {

    private String country;
    private String province;
    private String city;
    private boolean overseasCity;
    public void setCountry(String country) {
         this.country = country;
     }
     public String getCountry() {
         return country;
     }

    public void setProvince(String province) {
         this.province = province;
     }
     public String getProvince() {
         return province;
     }

    public void setCity(String city) {
         this.city = city;
     }
     public String getCity() {
         return city;
     }

    public void setOverseasCity(boolean overseasCity) {
         this.overseasCity = overseasCity;
     }
     public boolean getOverseasCity() {
         return overseasCity;
     }

}