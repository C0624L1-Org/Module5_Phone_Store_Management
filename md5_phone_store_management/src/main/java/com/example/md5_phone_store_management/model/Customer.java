package com.example.md5_phone_store_management.model;

import com.example.md5_phone_store_management.model.unique.Unique;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerID;

    @NotBlank(message = "Họ và tên không được để trống!")
    @Size(max = 50, message = "Họ và tên không được vượt quá 50 ký tự!")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "Họ và tên chỉ được chứa chữ cái và khoảng trắng!")
    @Column(name = "full_name", length = 50, nullable = false)
    private String fullName;

    @Pattern(regexp = "\\d{10,15}", message = "Số điện thoại phải chứa từ 10 đến 15 chữ số!")
//    @Unique(fieldName = "phone", message = "Số điện thoại đã tồn tại!")
    @Unique(fieldName = "phone", message = "Số điện thoại đã tồn tại!", id = @Value("#{customerID}"))
    @Column(name = "phone", length = 15)
    private String phone;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự!")
    @Column(name = "address", length = 500)
    private String address;

    @Email(message = "Định dạng email không hợp lệ!")
    @Size(max = 50, message = "Email không được vượt quá 50 ký tự!")
//    @Unique(fieldName = "email", message = "Email đã tồn tại!")
    @Unique(fieldName = "email", message = "Email đã tồn tại!",  id = @Value("#{customerID}"))
    @Column(name = "email", length = 50)
    private String email;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ!")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "dob")
    @Temporal(TemporalType.DATE)
    private Date dob;

    @NotNull(message = "Giới tính không được để trống!")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Min(value = 0, message = "Số lần mua hàng không được âm!")
    @Column(name = "purchaseCount", nullable = false)
    private int purchaseCount = 0;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invoice> invoices;

    //Constructor
    public Customer() {}

    public Customer(Integer customerID, String fullName, String phone, String address, String email, Date dob, Gender gender, int purchaseCount, List<Invoice> invoices) {
        this.customerID = customerID;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.dob = dob;
        this.gender = gender;
        this.purchaseCount = purchaseCount;
        this.invoices = invoices;
    }

    // Getters and Setters
    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getPurchaseCount() {
        return purchaseCount;
    }

    public void setPurchaseCount(int purchaseCount) {
        this.purchaseCount = purchaseCount;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerID=" + customerID +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", dob=" + dob +
                ", gender=" + gender +
                ", purchaseCount=" + purchaseCount +
                '}';
    }
}


