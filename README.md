# RotaPlay Yolcu

Yolcu taksi çağırma sitesi (GitHub Pages) + aynı sayfayı açan küçük Android uygulaması. Veriler, `RotaPlay-Sofor` ile aynı Firebase'dedir.

## Kurulum
1. Şoför projesindeki `firebase/README.md` adımlarıyla Firebase'i kur (bir kez).
2. `docs/config.js` içine Firebase web ayarlarını yaz (`apiKey`, `authDomain`, `projectId`, `appId`).
3. GitHub → **Settings → Pages** → Branch `main`, klasör **/docs**. Adres: `https://KULLANICIADIN.github.io/DEPO-ADI/`
   - Yolcu sayfası: bu adres. Yönetim paneli: bu adres + `admin.html` (giriş için Firebase'de oluşturduğun yönetici hesabı).
4. `app/src/main/java/com/rotaplay/yolcu/Config.kt` içindeki `SERVER_URL`'ye bu adresi yaz → GitHub **Actions** APK'yı derler.

## Nasıl çalışır
Yolcu varış noktasını seçip "Taksi çağır" der. Talep, konumuna 3 km içindeki çevrimiçi şoförlere gider; 30 sn içinde kabul olmazsa 5 km'ye genişler.
İlk kabul eden şoför yolcuya atanır. Ücret taksimetrede yazan tutardır, tahmini fiyat gösterilmez.
