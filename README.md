# OTO ŞASE PRO
Android Java uygulaması. VIN numarası biçimini kontrol eder ve NHTSA vPIC servisi üzerinden çevrimiçi çözümleme yapar.

## APK oluşturma
GitHub Actions iş akışı her push işleminde ve elle başlatıldığında debug APK üretir.
GitHub deposunda **Actions** sekmesini açın, **Android APK** iş akışının tamamlanmasını bekleyin. Başarılı çalışmanın **Artifacts** bölümünden **OTO-SASE-PRO-APK** dosyasını indirin. ZIP içindeki APK'yı Android telefona aktararak yükleyebilirsiniz.

## Sınırlar
NHTSA vPIC verisi her ülke ve model için tam değildir; özellikle Türkiye/Avrupa pazarındaki araçlarda sonuçlar eksik olabilir. Bu uygulama hasar, kilometre, mülkiyet veya servis geçmişi sorgulamaz.
