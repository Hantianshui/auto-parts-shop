package com.autoparts.shop.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("suppliers")
public class Supplier {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
