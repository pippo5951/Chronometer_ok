package com.gbsoft.cronometro;


public class Helpers {

    /**
     * Converte un tempo in millisecondi in String in formato hh: mm: ss
     * @param timeInMillis
     * @return
     */
    public static String ConvertTimeToString(long timeInMillis) {

        //convertire la differenza di tempo risultante in ore, minuti, secondi e millisecondi
        int seconds = (int) (timeInMillis / 1000) % 60;
        int minutes = (int) ((timeInMillis / (60000)) % 60);
        //int hours = (int) ((timeInMillis / (3600000)) % 24); //questo si ripristina a 0 dopo 24 ore
        int hours = (int) ((timeInMillis / (3600000))); //questo non viene ripristinato :P
        //non abbiamo bisogno di millisecs da restituire con questo metodo
        // int millis = (int) tempo In Millis% 1000;
        // le ultime 3 cifre di millisec

        return String.format("%02d:%02d:%02d"
                , hours, minutes, seconds);
    }
}
