package com.example.demo.dto.response;

public class DashboardResponse {

    private Long totalUsers;
    private Long totalDonors;
    private Long totalBloodUnits;
    private Long totalBloodRequests;
    private Long approvedRequests;
    private Long pendingRequests;
    private Long rejectedRequests;
    private Long totalDonations;

    public DashboardResponse() {
    }

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getTotalDonors() {
        return totalDonors;
    }

    public void setTotalDonors(Long totalDonors) {
        this.totalDonors = totalDonors;
    }

    public Long getTotalBloodUnits() {
        return totalBloodUnits;
    }

    public void setTotalBloodUnits(Long totalBloodUnits) {
        this.totalBloodUnits = totalBloodUnits;
    }

    public Long getTotalBloodRequests() {
        return totalBloodRequests;
    }

    public void setTotalBloodRequests(Long totalBloodRequests) {
        this.totalBloodRequests = totalBloodRequests;
    }

    public Long getApprovedRequests() {
        return approvedRequests;
    }

    public void setApprovedRequests(Long approvedRequests) {
        this.approvedRequests = approvedRequests;
    }

    public Long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(Long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public Long getRejectedRequests() {
        return rejectedRequests;
    }

    public void setRejectedRequests(Long rejectedRequests) {
        this.rejectedRequests = rejectedRequests;
    }

    public Long getTotalDonations() {
        return totalDonations;
    }

    public void setTotalDonations(Long totalDonations) {
        this.totalDonations = totalDonations;
    }
}