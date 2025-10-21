package com.goBhutan.adminPanel.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_ht_guests")
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "CID is required")
    @Column(name = "cid", nullable = false)
    private String cid;  // Citizenship ID or Passport No

    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Min(value = 0, message = "Age must be positive")
    @Column(name = "age")
    private Integer age;

    @Column(name = "gender")
    private String gender;

    @NotBlank(message = "Country of origin is required")
    @Column(name = "country_of_origin")
    private String countryOfOrigin;

    @Pattern(regexp = "^(\\+?[0-9]{7,15})$", message = "Invalid phone number")
    @Column(name = "phone_number")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @Column(name = "email")
    private String email;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Constructors
    public Guest() {}

    public Guest(String cid, String name, Integer age, String gender,
                 String countryOfOrigin, String phoneNumber, String email) {
        this.cid = cid;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.countryOfOrigin = countryOfOrigin;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCid() { return cid; }
    public void setCid(String cid) { this.cid = cid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getCountryOfOrigin() { return countryOfOrigin; }
    public void setCountryOfOrigin(String countryOfOrigin) { this.countryOfOrigin = countryOfOrigin; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }
}
