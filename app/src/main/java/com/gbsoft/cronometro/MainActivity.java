package com.gbsoft.cronometro;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    /**
     * Tasto per ottenere il tempo di partenza salvato della classe Cronometro
           * questo è usato per onResume / onPause / etc.
     */
    public static final String START_TIME = "START_TIME";
    /**
     * Stessa storia, ma per sapere se il Cronometro è in esecuzione o no
     */
    public static final String CHRONO_WAS_RUNNING = "CHRONO_WAS_RUNNING";
    /**
     * Stessa storia, ma se il Cronometro è stato arrestato, non vogliamo perdere il tempo di arresto
           il tv_timer
     */
    public static final String TV_TIMER_TEXT = "TV_TIMER_TEXT";
    /**
     * Stessa storia, non vogliamo perdere i giri registrati
     */
    public static final String ET_LAPST_TEXT = "ET_LAPST_TEXT";
    /**
     * Same story...keeps the value of the lap counter
     */
    public static final String LAP_COUNTER  = "LAP_COUNTER";

    //Variabili membro per accedere agli elementi UI
    Button mBtnStart, mBtnLap, mBtnStop,mBtnReset; //buttons
    TextView mTvTimer; //timer textview
    EditText mEtLaps; //laps text view
    ScrollView mSvLaps; //scroll view which wraps the et_laps

    //tenere traccia di quante volte è stato fatto clic su btn_lap
    int mLapCounter = 1;

    //Instance of Cronometro
    Cronometro mChrono;

    //Thread for chronometer
    Thread mThreadChrono;

    //Reference to the MainActivity (this class!)
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Mantenere il display acceso in un'Activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Istanziazione di tutte le variabili associate
        mContext = this;

        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnLap = (Button) findViewById(R.id.btn_lap);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mBtnReset = (Button) findViewById(R.id.btn_reset);

        mTvTimer = (TextView) findViewById(R.id.tv_timer);
        mEtLaps = (EditText) findViewById(R.id.et_laps);
        mEtLaps.setEnabled(false); //impedire che le i giri siano modificabili

        mSvLaps = (ScrollView) findViewById(R.id.sv_lap);

        //btn_start click handler
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // bottone reset attivato
                mBtnReset.setEnabled( true );
                //se il Cronometro non è stato istanziato prima...
                if(mChrono == null) {
                    //istaziare il cronometrok
                    mChrono = new Cronometro(mContext);
                    //run the chronometer on a separate thread
                    mThreadChrono = new Thread(mChrono);
                    mThreadChrono.start();

                    //start il conometro!
                    mChrono.start();

                    //clear giri se presenti
                    mEtLaps.setText(""); //vuoto stringa

                    //reset contatore di giri
                    mLapCounter = 1;
                }
            }
        });

        //btn_stop click handler
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // bottone reset disattivato
                mBtnReset.setEnabled( false );
                //se il Cronometro era stato istanziato prima...
                if(mChrono != null) {
                    //stop the chronometer
                    mChrono.stop();
                    //stop the thread
                    mThreadChrono.interrupt();
                    mThreadChrono = null;
                    //kill the chrono class
                    mChrono = null;
                }
            }
        });

        mBtnReset.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mChrono != null) {
                    //stop the chronometer
                    mChrono.stop();
                    //stop the thread
                    mThreadChrono.interrupt();
                    mThreadChrono = null;
                    //kill the chrono class
                    mChrono = null; }
                mTvTimer.setText( "00:00:00:000" );
                mEtLaps.setText( "" );
            }
        } );

        mBtnLap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //se il chrono non è in esecuzione non dovremmo catturare il giro!
                if(mChrono == null) {
                    Toast.makeText(mContext
                            , R.string.warning_lap_button, Toast.LENGTH_SHORT).show();
                    return; //do nothing!
                }

                //basta semplicemente copiare il testo corrente di tv_timer e appenderlo a et_laps

                if (mLapCounter >= 101){ Toast.makeText(mContext
                            , R.string.warning_max_giri, Toast.LENGTH_SHORT).show();
                } else {
                    mEtLaps.append("GIRO " + String.valueOf(mLapCounter++)
                            + "   " + mTvTimer.getText() + "\n");
                }
                //scroll to the bottom of et_laps
                mSvLaps.post(new Runnable() {
                    @Override
                    public void run() {
                        mSvLaps.smoothScrollTo(0, mEtLaps.getBottom());
                    }
                });
            }
        });
    }

    /**
     * Update the text of tv_timer
     * @param timeAsText the text to update tv_timer with
     */
    public void updateTimerText(final String timeAsText) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvTimer.setText(timeAsText);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInstance();

        //interrompere i servizi e le notifiche di sfondo
        ((CronometroApplication)getApplication()).stopBackgroundServices();
        ((CronometroApplication)getApplication()).cancelNotification();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveInstance();

        if(mChrono != null && mChrono.isRunning()) {
            //avviare la notifica di sfondo e il timer
            ((CronometroApplication)getApplication())
                    .startBackgroundServices(mChrono.getStartTime());
        }
    }

    @Override
    protected void onDestroy() {

        saveInstance();

        //Quando viene premuto il pulsante di back, l'applicazione viene distrutta da parte dell'OS. Non vogliamo che questo ci impedisca
        // di mostrare la notifica se il Cronometro è in esecuzione!
        if(mChrono == null || !mChrono.isRunning()) {
            //stop background services and notifications
            ((CronometroApplication) getApplication()).stopBackgroundServices();
            ((CronometroApplication) getApplication()).cancelNotification();
        }

        super.onDestroy();
    }

    /*
     * Se l'applicazione passa allo sfondo o all'orientamento o ad ogni altra possibilità che
           * metterà in pausa l'applicazione, salveremo alcuni valori di istanza, per riprendere indietro dall'ultimo stato
     */
    private void saveInstance() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        if(mChrono != null && mChrono.isRunning()) {
            editor.putBoolean(CHRONO_WAS_RUNNING, mChrono.isRunning());
            editor.putLong(START_TIME, mChrono.getStartTime());
            editor.putInt(LAP_COUNTER, mLapCounter);
        } else {
            editor.putBoolean(CHRONO_WAS_RUNNING, false);
            editor.putLong(START_TIME, 0); //0 significa che il Cronometro non era attivo! un controllo ridondante!
            editor.putInt(LAP_COUNTER, 1);
        }

        //Salvo il testo del giro in ogni caso. solo un nuovo clic sul pulsante di avvio dovrebbe cancellare questo testo!
        editor.putString(ET_LAPST_TEXT, mEtLaps.getText().toString());

        //Stessa storia per il testo del timer
        editor.putString(TV_TIMER_TEXT, mTvTimer.getText().toString());

        editor.commit();
    }

    /**
     * Carica le preferenze condivise per riprendere l'ultimo stato noto dell'applicazione
     */
    private void loadInstance() {

        SharedPreferences pref = getPreferences(MODE_PRIVATE);

        //if chronometer was running
        if (pref.getBoolean(CHRONO_WAS_RUNNING, false)) {
            //get the last start time from the bundle
            long lastStartTime = pref.getLong(START_TIME, 0);
            //if the last start time is not 0
            if (lastStartTime != 0) { //perché 0 significa che il valore non è stato salvato correttamente!

                if (mChrono == null) { //assicuratevi di non creare nuove istanze e thread!

                    if (mThreadChrono != null) { //se il thread esiste ... prima interrompere e annullarlo!
                        mThreadChrono.interrupt();
                        mThreadChrono = null;
                    }

                    //avviare il Cronometro con il vecchio tempo salvato
                    mChrono = new Cronometro(mContext, lastStartTime);
                    mThreadChrono = new Thread(mChrono);
                    mThreadChrono.start();
                    mChrono.start();
                }
            }
        }

        //caricheremo comunque il testo del giro in ogni caso!
        // impostare il vecchio valore del contatore del giro
        mLapCounter = pref.getInt(LAP_COUNTER, 1);

        String oldEtLapsText = pref.getString(ET_LAPST_TEXT, "");
        if (!oldEtLapsText.isEmpty()) { //if old timer was saved correctly
            mEtLaps.setText(oldEtLapsText);
        }

        String oldTvTimerText = pref.getString(TV_TIMER_TEXT, "");
        if (!oldTvTimerText.isEmpty()) {
            mTvTimer.setText(oldTvTimerText);
        }
    }
}
