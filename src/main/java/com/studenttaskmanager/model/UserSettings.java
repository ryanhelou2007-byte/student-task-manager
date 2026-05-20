package com.studenttaskmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(name = "digest_frequency", nullable = false, length = 20)
    private DigestFrequency digestFrequency = DigestFrequency.DAILY;

    @Column(name = "exam_reminders", nullable = false)
    private boolean examReminders = true;

    @Column(name = "assignment_reminders", nullable = false)
    private boolean assignmentReminders = true;

    @Column(nullable = false, length = 80)
    private String timezone = "UTC";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public DigestFrequency getDigestFrequency() {
        return digestFrequency;
    }

    public void setDigestFrequency(DigestFrequency digestFrequency) {
        this.digestFrequency = digestFrequency;
    }

    public boolean isExamReminders() {
        return examReminders;
    }

    public void setExamReminders(boolean examReminders) {
        this.examReminders = examReminders;
    }

    public boolean isAssignmentReminders() {
        return assignmentReminders;
    }

    public void setAssignmentReminders(boolean assignmentReminders) {
        this.assignmentReminders = assignmentReminders;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
