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
  // Yandex adres araması için anahtar (ücretsiz): https://developer.tech.yandex.com → "Get a key" →
  // "JavaScript API and HTTP Geocoder". Boş bırakırsan OpenStreetMap araması kullanılır.
  // Anahtarın aktif olması ~15 dk sürebilir. Ücretsiz sürümün şartlarını (özellikle ticari kullanım) kendin kontrol et.
  YANDEX_API_KEY: "",

  // Sesli arama için sunucular. STUN ücretsizdir. Çok katı mobil ağlarda bazı aramalar bağlanamazsa
  // buraya bir TURN sunucusu ekleyebilirsin: {urls: "turn:...", username: "...", credential: "..."}
  ICE_SERVERS: [
    { urls: "stun:stun.l.google.com:19302" },
    { urls: "stun:stun1.l.google.com:19302" }
  ]
};
