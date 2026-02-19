package com.example.models;

public class Persoon {
    private String voornaam;
    private String achternaam;
    private Adres adres;

    public Persoon() {
    }

    public Persoon(String voornaam, String achternaam, Adres adres) {
        this.voornaam = voornaam;
        this.achternaam = achternaam;
        this.adres = adres;
    }

    public String getVoornaam() {
        return voornaam;
    }

    public String getAchternaam() {
        return achternaam;
    }

    public Adres getAdres() {
        return adres;
    }

    public void setVoornaam(String voornaam) {
        this.voornaam = voornaam;
    }

    public void setAchternaam(String achternaam) {
        this.achternaam = achternaam;
    }

    public void setAdres(Adres adres) {
        this.adres = adres;
    }

    @Override
    public String toString() {
        return "Persoon{" +
                "voornaam='" + voornaam + '\'' +
                ", achternaam='" + achternaam + '\'' +
                ", adres=" + adres +
                '}';
    }
}
