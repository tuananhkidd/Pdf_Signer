package com.beetech.card_detect.entity;

public class SignEntity {
    private int xPosition;
    private int yPosition;
    private String signPath;
    private String pdfPath;
    private int currentPage;


    public SignEntity(int xPosition, int yPosition, String signPath, String pdfPath,int currentPage) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.signPath = signPath;
        this.pdfPath = pdfPath;
        this.currentPage = currentPage;
    }

    public int getxPosition() {
        return xPosition;
    }

    public void setxPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    public int getyPosition() {
        return yPosition;
    }

    public void setyPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    public String getSignPath() {
        return signPath;
    }

    public void setSignPath(String signPath) {
        this.signPath = signPath;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
