package com.example;

public class Adres {
    private String straat;
    private String stad;

    public Adres() {
    }

    public Adres(String straat, String stad) {
        this.straat = straat;
        this.stad = stad;
    }

    public String getStraat() {
        return straat;
    }

    public String getStad() {
        return stad;
    }

    public void setStraat(String straat) {
        this.straat = straat;
    }

    public void setStad(String stad) {
        this.stad = stad;
    }

    @Override
    public String toString() {
        return "Adres{" +
                "straat='" + straat + '\'' +
                ", stad='" + stad + '\'' +
                '}';
    }
}
