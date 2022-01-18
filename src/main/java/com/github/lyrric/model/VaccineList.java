package com.github.lyrric.model;

import java.util.Objects;

/**
 * Created on 2020-07-23.
 * 疫苗列表
 * @author wangxiaodong
 */
public class VaccineList {

    private Integer id;
    /**
     * 医院名称
     */
    private String name;
    /**
     * 医院地址
     */
    private String address;
    /**
     * 疫苗代码
     */
    private String vaccineCode;
    /**
     * 疫苗名称
     */
    private String vaccineName;
    /**
     * 秒杀时间
     */
    private String startTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVaccineCode() {
        return vaccineCode;
    }

    public void setVaccineCode(String vaccineCode) {
        this.vaccineCode = vaccineCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VaccineList that = (VaccineList) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(address, that.address) &&
                Objects.equals(vaccineCode, that.vaccineCode) &&
                Objects.equals(vaccineName, that.vaccineName) &&
                Objects.equals(startTime, that.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, address, vaccineCode, vaccineName, startTime);
    }
}
