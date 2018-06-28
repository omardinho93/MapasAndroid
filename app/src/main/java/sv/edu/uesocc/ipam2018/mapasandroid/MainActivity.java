package sv.edu.uesocc.ipam2018.mapasandroid;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_REQUEST_CODE = 1;
    private GoogleMap Mapa;
    private Button btnOpciones;
    private Button btnIr;
    private Button btnPosicion;
    private Button btnMostrar;
    private CircleOptions circleOptions;
    private Circle circulo;

    private int r=0, g=0, b=0;

    private TextView txtlat;
    private TextView txtlon;

    private int colorCirculo;
    private int radioCirculo;

    String cadenaLista;

    List <LatLng> centros = new ArrayList<>();

    Gson gson = new Gson();

    private int mMapTypes[] = {
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_HYBRID
    };

    //Método que se activa cuando el focus vuelve a la pantalla
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            asignarPreferences();
            //Toast.makeText(MainActivity.this,"Activity changed",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        btnOpciones = (Button) findViewById(R.id.btnOpciones);
        btnOpciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,
                        OpcionesMapa.class));

            }
        });
        btnIr = (Button) findViewById(R.id.btnMover);
        btnIr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // irUES();
                mover();
            }
        });

        btnPosicion = (Button) findViewById(R.id.btnPosicion);
        btnPosicion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerPosicion();
            }
        });

        btnMostrar = (Button) findViewById(R.id.btnPin);
        btnMostrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarPunto();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Mapa = googleMap;
        Mapa.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            public void onMapClick(LatLng point) {
                addMarker(point);
            }
        });


        //Agregar circulo
        Mapa.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            public void onMapLongClick(LatLng point) {

               dibujarCirculo(point, colorCirculo, radioCirculo);

            }
        });
        //Mapa.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
        //Mapa.getUiSettings().setZoomControlsEnabled(true);

    }


    private void irUES() {
        CameraUpdate camUpd1 =
                CameraUpdateFactory
                        .newLatLngZoom(new LatLng(13.970263, -89.574808), 16);

        Mapa.moveCamera(camUpd1);
    }


    private void obtenerPosicion() {
        CameraPosition camPosicion = Mapa.getCameraPosition();

        LatLng coordenadas = camPosicion.target;
        double latitud = coordenadas.latitude;
        double longitud = coordenadas.longitude;

        Toast.makeText(this, "Lat: " + latitud + " | Long: " + longitud, Toast.LENGTH_SHORT).show();
    }


    /*
        Agrega un marcador en las coordenadas especificadas

     */
    private void addMarker(LatLng point) {
        Mapa.addMarker(new MarkerOptions().position(point));
        //Mapa.moveCamera(CameraUpdateFactory.newLatLng(point));
        Toast.makeText(
                MainActivity.this,
                "Click\n" +
                        "Lat: " + point.latitude + "\n" +
                        "Lng: " + point.longitude + "\n",
                Toast.LENGTH_SHORT).show();
    }


    //Agrega un marcador en la UES
    private void agregarPunto() {
        Mapa.addMarker(new MarkerOptions()
                .position(new LatLng(13.970263, -89.574808))
                .title("Santa Ana: UES")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        );

    }


    //Método para cargar las preferencias
    private void asignarPreferences() {
        recuperarCirculos();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Mapa.setMapType(mMapTypes[Integer.parseInt(pref.getString("tipoMapa", ""))]);
        Mapa.getUiSettings().setZoomControlsEnabled(pref.getBoolean("Zoomcontroll", false));
        Mapa.getUiSettings().setRotateGesturesEnabled(pref.getBoolean("Rotategesture", false));
        Mapa.getUiSettings().setScrollGesturesEnabled(pref.getBoolean("Scrollgesture", false));
        Mapa.getUiSettings().setZoomGesturesEnabled(pref.getBoolean("ZoomGesture", false));

        colorCirculo = pref.getInt("color", 0);
        radioCirculo = pref.getInt("radio", 0);

        for(LatLng item : centros)
        {

            dibujarCirculo(item, colorCirculo, radioCirculo);
        }

        //Para permitir obtener la localización
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Mapa.setMyLocationEnabled(true);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Mostrar diálogo explicativo
            } else {
                // Solicitar permiso
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST_CODE);
            }
        }
    }

    private void dibujarCirculo(LatLng punto, int color, int radio)
    {
        escogerColor(color);
        circleOptions = new CircleOptions()
                .center(punto)
                .fillColor(Color.argb(128, r, g, b))
                .radius(radio)
                .strokeWidth(4);
        circulo = Mapa.addCircle(circleOptions);
        recordarCirculo(punto);
        r=g=b=0;
    }

    private void escogerColor(int col)
    {

        switch(col)
        {
            case 0:
                r=255;
                break;
            case 1:
                g=255;
                break;
            case 2:
                b=255;
                break;
        }


    }

    /*Este metodo es llamado en el oncreate, porque ahi se encuentra el metodo clic del botón ir
    * El ingeniero fue quien lo puso ahi, no se por que*/
    private void mover()
    {
        txtlat = (TextView) findViewById(R.id.lat);
        txtlon = (TextView) findViewById(R.id.lon);
        double lati= Double.parseDouble(txtlat.getText().toString());
        double longi= Double.parseDouble(txtlon.getText().toString());
        CameraUpdate camUpd1 =
                CameraUpdateFactory
                        .newLatLngZoom(new LatLng(lati, -longi), 16);

        Mapa.moveCamera(camUpd1);

    }

    private void recordarCirculo(LatLng centro)
    {
        centros.add(centro);
        String jsonList = gson.toJson(centros);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("centrosCirculos", jsonList);
        editor.commit();
    }

    private void recuperarCirculos()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String savedList = preferences.getString("centrosCirculos", "No hay nada");
        Type type = new TypeToken<List<String>>(){}.getType();
        centros = gson.fromJson(savedList, type);
    }
}
