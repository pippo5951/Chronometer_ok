package com.gbsoft.cronometro;

import android.content.Context;


public class Cronometro implements Runnable {

    //Alcune costanti per millisecondi a conversioni di ore, minuti e secondi
    public static final long MILLIS_TO_MINUTES = 60000;
    public static final long MILLS_TO_HOURS = 3600000;

    /**
     * Contesto responsabile di questa istanza della classe
     */
    Context mContext;
    /**
     * Starting time
     */
    long mStartTime;
    /**
     * Se la classe è in esecuzione o meno
     */
    boolean mIsRunning;

    /**
     * Costruttore per la classe per uso normale
     * @param context l'attività che è responsabile di questo istanza di classe
     */
    public Cronometro(Context context) {
        mContext = context;
    }

    /**
     *Costruttore che prende il contesto e anche un tempo di partenza già impostato
           * questo viene utilizzato principalmente per onResume se l'applicazione è stata arrestata per qualsiasi motivo
     * @param context
     * @param startTime
     */
    public Cronometro(Context context, long startTime) {
        this(context);
        mStartTime = startTime;
    }

    /**
     * Starts the chronometer
     */
    public void start() {
        if(mStartTime == 0) { //se l'ora di inizio non è stata impostata prima! per esempio. da un secondo costruttore
            mStartTime = System.currentTimeMillis();
        }
        mIsRunning = true;
    }

    /**
     * Stops the chronometer
     */
    public void stop() {
        mIsRunning = false;
    }

    /**
     * Controllare se il Cronometro è in funzione o meno
           * @return true se funziona, false se non in esecuzione
     */
    public boolean isRunning() {
        return mIsRunning;
    }

    /**
     * Get the start time of the class
     * @return start time in milliseconds
     */
    public long getStartTime() {
        return mStartTime;
    }


    @Override
    public void run() {
        while(mIsRunning) {
            //Noi non chiamiamo ConvertTimeToString qui perché aggiungerà un certo overhead
            // quindi facciamo il calcolo senza chiamate di funzione!

            //Qui calcoliamo la differenza di ora di inizio e ora corrente
            long since = System.currentTimeMillis() - mStartTime;

            //convertire la differenza di tempo risultante in ore, minuti, secondi e millisecondi
            int seconds = (int) (since / 1000) % 60;
            int minutes = (int) ((since / (MILLIS_TO_MINUTES)) % 60);
            //int hours = (int) ((since / (MILLS_TO_HOURS)) % 24); //questo si ripristina a 0 dopo 24 ore!
            int hours = (int) ((since / (MILLS_TO_HOURS))); //this does not reset to 0!
            int millis = (int) since % 1000; //le ultime 3 cifre dei millisec

            ((MainActivity) mContext).updateTimerText(String.format("%02d:%02d:%02d:%03d"
                    , hours, minutes, seconds, millis));

            //Fermare per un breve periodo, per evitare un utilizzo elevato della CPU!
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
