// Firebase ayarları (gizli değildir; güvenliği Firestore kuralları sağlar).
window.FIREBASE_CONFIG = {
  apiKey: "AIzaSyBYxAvUrVFUsC4aD704-C49W1I0De604qo",
  authDomain: "rotaplay-taksi.firebaseapp.com",
  projectId: "rotaplay-taksi",
  storageBucket: "rotaplay-taksi.firebasestorage.app",
  messagingSenderId: "914946635173",
  appId: "1:914946635173:web:9948600043ccfd3c4c244c"
};

// Uygulama ayarları
window.APP_CONFIG = {
  // Harita görüntüsü: "openfreemap" (varsayılan, ücretsiz, anahtarsız) veya "osm" (düz OpenStreetMap, eski telefonlarda daha hafif).
  MAP_PROVIDER: "openfreemap",

  // Yandex adres araması (Geocoder) anahtarı. DİKKAT: Yandex güncel fiyat sayfasına göre ÜCRETLİDİR (yıllık lisans, en düşük paket binlerce dolar).
  // Almak istersen: https://developer.tech.yandex.com →
  // "JavaScript API and HTTP Geocoder". Boş bırakırsan OpenStreetMap araması kullanılır.
  // Anahtarın aktif olması ~15 dk sürebilir. Boş bırakmak sorun değildir.
  YANDEX_API_KEY: "",

  // Yandex HARİTA görüntüsü için Tiles API anahtarı: https://developer.tech.yandex.com → "Tiles API" → "New key".
  // (Adres arama anahtarından AYRI bir üründür.) Boş bırakırsan OpenStreetMap haritası görünür.
  // Yandex Tiles API ÜCRETLİDİR (güncel sayfaya göre yıllık lisans, en düşük paket yaklaşık 450-585 USD). Yandex logosu da zorunludur.
  YANDEX_TILES_KEY: "",

  // Sesli arama için sunucular. STUN ücretsizdir. Çok katı mobil ağlarda bazı aramalar bağlanamazsa
  // buraya bir TURN sunucusu ekleyebilirsin: {urls: "turn:...", username: "...", credential: "..."}
  ICE_SERVERS: [
    { urls: "stun:stun.l.google.com:19302" },
    { urls: "stun:stun1.l.google.com:19302" }
  ]
};
