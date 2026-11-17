package com.otosasepro;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.text.InputType;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
 EditText vin;
 TextView output;
 Button go;
 ExecutorService worker=Executors.newSingleThreadExecutor();

 public void onCreate(Bundle b) {
  super.onCreate(b);
  showHome();
 }

 void showHome() {
  LinearLayout l = new LinearLayout(this);
  l.setOrientation(1);
  l.setPadding(28,32,28,20);
  l.setBackgroundColor(0xfff4f6f9);

  TextView title = new TextView(this);
  title.setText("OTO ŞASE PRO");
  title.setTextSize(26);
  title.setTextColor(0xff0f1e32);
  title.setGravity(Gravity.CENTER);
  l.addView(title);

  TextView label = new TextView(this);
  label.setText("17 HANELİ ŞASE NUMARASI (VIN)");
  label.setPadding(0,30,0,8);
  l.addView(label);

  vin = new EditText(this);
  vin.setSingleLine(true);
  vin.setHint("VIN numarasını girin");
  vin.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
  l.addView(vin);

  go = new Button(this);
  go.setText("ÇEVRİMİÇİ ŞASE SORGULA");
  l.addView(go);

  Button catalog = new Button(this);
  catalog.setText("ORİJİNAL PARÇA KATALOĞU");
  l.addView(catalog);

  output = new TextView(this);
  output.setText("VIN numarasını girip sorgulayın.\nVeri kaynağı: NHTSA vPIC.");
  output.setTextSize(15);
  output.setTextColor(Color.DKGRAY);
  output.setPadding(0,24,0,0);
  ScrollView s = new ScrollView(this);
  s.addView(output);
  l.addView(s,new LinearLayout.LayoutParams(-1,0,1));
  setContentView(l);

  go.setOnClickListener(v -> lookup());
  catalog.setOnClickListener(v -> showCatalog());
 }

 void showCatalog() {
  LinearLayout l = new LinearLayout(this);
  l.setOrientation(1);
  l.setPadding(24,28,24,20);
  l.setBackgroundColor(0xfff4f6f9);

  TextView title = new TextView(this);
  title.setText("ORİJİNAL PARÇA KATALOĞU");
  title.setTextSize(22);
  title.setTextColor(0xff0f1e32);
  title.setGravity(Gravity.CENTER);
  l.addView(title);

  TextView info = new TextView(this);
  info.setText("Şase numarasıyla araç ve OEM parça arama\n\nDestek kapsamı: Isuzu D-Max, kamyonetler, N/F serisi kamyonlar, otobüsler ve diğer markalar.");
  info.setTextSize(15);
  info.setTextColor(Color.DKGRAY);
  info.setPadding(0,24,0,18);
  l.addView(info);

  EditText frame = new EditText(this);
  frame.setSingleLine(true);
  frame.setHint("VIN / FRAME / Şase numarası");
  frame.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
  l.addView(frame);

  Spinner groups = new Spinner(this);
  String[] choices = {"Araç grubunu seçin","Isuzu D-Max / Kamyonet","Isuzu N Serisi Kamyon","Isuzu F Serisi Kamyon","Isuzu Otobüs","Diğer Isuzu modelleri","Diğer marka ve modeller"};
  ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, choices);
  adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
  groups.setAdapter(adapter);
  l.addView(groups);

  Button search = new Button(this);
  search.setText("ORİJİNAL PARÇA ARA");
  l.addView(search);

  TextView status = new TextView(this);
  status.setText("Katalog bağlantısı henüz etkin değil. Gerçek OEM parça numarası ve çizimlerini göstermek için lisanslı katalog API erişimi ve güvenli sunucu bağlantısı gerekiyor. Bu nedenle şu anda sahte parça sonucu gösterilmez.");
  status.setTextSize(14);
  status.setTextColor(0xff8a3b16);
  status.setPadding(0,20,0,20);
  l.addView(status);

  Button back = new Button(this);
  back.setText("ANA EKRANA DÖN");
  l.addView(back);
  setContentView(l);

  search.setOnClickListener(v -> {
   String number = frame.getText().toString().trim();
   if (number.isEmpty()) {
    status.setText("Lütfen önce VIN / FRAME / şase numarasını girin.");
   } else if (groups.getSelectedItemPosition() == 0) {
    status.setText("Lütfen araç grubunu seçin.");
   } else {
    status.setText("Şase numarası alındı. Ancak lisanslı OEM katalog servisi henüz bağlanmadığı için parça numarası doğrulanamıyor. API bağlantısı etkinleştirildiğinde bu arama gerçek katalogda yapılacak.");
   }
  });
  back.setOnClickListener(v -> showHome());
 }

 void lookup() {
  String x = vin.getText().toString().trim().toUpperCase().replace(" ","").replace("-","");
  if (!x.matches("[A-HJ-NPR-Z0-9]{17}")) {
   output.setText("HATA: VIN 17 karakter olmalı; I, O ve Q kullanılamaz.");
   return;
  }
  go.setEnabled(false);
  output.setText("Sorgulanıyor...");
  worker.execute(() -> {
   String msg;
   try {
    HttpURLConnection c = (HttpURLConnection)new URL("https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVinValuesExtended/"+x+"?format=json").openConnection();
    c.setConnectTimeout(15000);
    c.setReadTimeout(20000);
    BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(),"UTF-8"));
    StringBuilder z = new StringBuilder();
    String q;
    while ((q=r.readLine())!=null) z.append(q);
    r.close();
    JSONObject d = new JSONObject(z.toString()).getJSONArray("Results").getJSONObject(0);
    String[] keys = {"Make","Manufacturer","Model","ModelYear","VehicleType","BodyClass","DriveType","EngineModel","EngineNumberOfCylinders","DisplacementL","FuelTypePrimary","TransmissionStyle","PlantCountry","PlantCity"};
    String[] labels = {"Marka","Üretici","Model","Model yılı","Araç tipi","Kasa tipi","Çekiş tipi","Motor","Silindir","Motor hacmi (L)","Yakıt","Şanzıman","Üretim ülkesi","Üretim şehri"};
    StringBuilder o = new StringBuilder("VIN: "+x+"\n\n");
    for (int i=0;i<keys.length;i++) {
     String val=d.optString(keys[i],"");
     if (!val.isEmpty()&&!val.equals("null")) o.append(labels[i]).append(": ").append(val).append("\n");
    }
    o.append("\nKaynak: NHTSA vPIC. Türkiye/Avrupa araçlarında veri eksik olabilir.");
    msg=o.toString();
   } catch(Exception e) {
    msg="Sorgu tamamlanamadı: "+e.getMessage()+"\nİnternet bağlantısını kontrol edin. NHTSA tüm araçlar için veri sağlamaz.";
   }
   final String result=msg;
   runOnUiThread(() -> { output.setText(result); go.setEnabled(true); });
  });
 }

 protected void onDestroy() {
  super.onDestroy();
  worker.shutdownNow();
 }
}