package com.lstu.kovalchuk.androidlabs;

public class RequestData {
    private Integer rqt_id;
    private Integer cli_id;
    private String uRL;
    private String imagesource;
    private String comment;
    private String fullName;
    private String email;
    private String phone;
    private String address;

    public String getuRL() {
        return uRL;
    }

    public void setuRL(String uRL) {
        this.uRL = uRL;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public RequestData() {
    }

    public String getImagesource() {
        return imagesource;
    }

    public void setImagesource(String imagesource) {
        this.imagesource = imagesource;
    }

    public Integer getCli_id() {
        return cli_id;
    }

    public void setCli_id(Integer cli_id) {
        this.cli_id = cli_id;
    }

    public Integer getRqt_id() {
        return rqt_id;
    }

    public void setRqt_id(Integer rqt_id) {
        this.rqt_id = rqt_id;
    }
}
