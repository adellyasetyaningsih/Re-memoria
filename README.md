# 📼 Re-Memoria

> **Vintage Cassette Tape Journaling & AI Audio Diary Android Application**  
> *"Capture your thoughts, preserve your memories, and replay your life in vintage cassette tapes."*

---

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Language-Java-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)
[![Supabase](https://img.shields.io/badge/Storage-Supabase-3ECF8E?logo=supabase&logoColor=white)](https://supabase.com)
[![Groq AI](https://img.shields.io/badge/AI-Groq%20Cloud-F55036?logo=fastapi&logoColor=white)](https://groq.com)

---

## 📲 Download Aplikasi (APK)

Kamu dapat langsung mengunduh dan menginstal aplikasi **Re-Memoria** di ponsel Android melalui tautan berikut:

[![Download APK](https://img.shields.io/badge/Download-Re--Memoria%20APK%20(Google%20Drive)-B33B38?style=for-the-badge&logo=google-drive&logoColor=white)](https://drive.google.com/drive/folders/1puXq-vIZVvQIiJEQqLx4Z-2Uyppl0Dck?usp=drive_link)

🔗 **Link Google Drive:** [Unduh Re-Memoria APK di Google Drive](https://drive.google.com/drive/folders/1puXq-vIZVvQIiJEQqLx4Z-2Uyppl0Dck?usp=drive_link)

> 💡 *Catatan: Jika muncul peringatan saat instalasi di HP Android, pilih "Tetap Instal" / aktifkan izin "Install unknown apps" (Izinkan dari sumber ini).*

## ✨ Tentang Re-Memoria

**Re-Memoria** adalah aplikasi jurnal audio personal berbasis Android yang mengusung konsep nostalgia pemutar pita kaset analog (*vintage cassette tape*). Setiap rekaman suara, catatan harian, dan refleksi hidup disimpan dalam wujud kaset virtual dengan label, warna kustom, serta roda pita (*spindle reels*) yang berputar autentik.

Dilengkapi dengan integrasi **AI canggih** (Groq Whisper & Llama 3), Re-Memoria secara otomatis mentranskripsikan rekaman suaramu menjadi teks serta memberikan rangkuman insight emosional dan kata-kata reflektif yang bermakna.

---

## 🌟 Fitur Utama

### 1. 🎙️ Pemutar & Perekam Kaset Vintage (*Cassette Deck*)
* Antarmuka audio player retro dengan tombol fisik analog (*Play, Pause, Stop, Rewind*).
* Visual roda pita kaset (*reel-to-reel*) berputar dinamis saat audio dimainkan.
* Kemudahan merekam suara harian langsung dari aplikasi dengan durasi fleksibel.

### 2. 📅 Kalender Kaset Interaktif (*Cassette Calendar*)
* Tampilan ubin tanggal berwujud kaset mini unik lengkap dengan label tanggal.
* **Indikator Dinamis:** Warna kaset pada kalender otomatis berubah sesuai warna kaset rekaman hari tersebut.
* **Highlight Tanggal Aktif:** Border seleksi wax-red kontras tinggi yang memastikan tanggal yang sedang dilihat selalu tampak jelas.

### 3. 🤖 Transkripsi & Refleksi Berbasis AI (Groq Cloud)
* **Speech-to-Text Cepat:** Menggunakan Groq Whisper API untuk transkripsi audio ke teks dalam hitungan detik.
* **AI Insight & Refleksi:** Model bahasa Llama 3 mengekstrak rangkuman perasaan, poin penting, serta refleksi penyemangat dari setiap rekamanmu.

### 4. 🌙 Dark Mode & Light Mode (Retro Midnight Studio)
* Dukungan penuh tema Gelap & Terang dengan palet warna retro hangat (*sepia paper, warm gold, wax red, & deep brown*).
* Kontras teks dan ikon dioptimalkan secara presisi agar nyaman di mata saat malam hari.

### 5. 🗄️ Rak Kaset & Personalisasi (*Tape Shelf*)
* Koleksi seluruh rekaman tersusun rapi di rak kaset virtual (*Tape Shelf*).
* Kostumisasi warna bodi kaset (*Brick Red, Deep Brown, Muted Olive, Tape Gold, Tape Teal*, dll).
* Menu aksi cepat (putar audio, lihat transkripsi/insight lengkap, atau hapus kaset).

### 6. 🔒 Otentikasi & Penyimpanan Awan
* Registrasi dan Login aman menggunakan **Firebase Authentication**.
* Sinkronisasi data jurnal & metadata kaset via **Cloud Firestore**.
* Penyimpanan file rekaman audio m4a di **Supabase Storage**.

---

## 🛠️ Tech Stack & Arsitektur

| Komponen | Teknologi |
|---|---|
| **Platform** | Android (Min SDK 24, Target SDK 36) |
| **Bahasa Pemrograman** | Java |
| **User Interface** | XML Layouts, Custom Vector Drawables, ConstraintLayout |
| **Audio Processing** | Android MediaRecorder, MediaPlayer |
| **Cloud Storage** | Supabase Storage (Audio bucket) |
| **Database & Auth** | Firebase Authentication & Cloud Firestore |
| **AI Processing** | Groq API (Whisper-large-v3-turbo & Llama-3.1-8b-instant) |
| **HTTP Client** | OkHttp 4 |

---

## 📁 Struktur Folder Proyek

```text
app/src/main/
├── java/com/example/finalproject/
│   ├── adapters/          # Adapter Kalender & Rak Kaset (RecyclerView)
│   ├── ai/                # Integrasi Groq API (Speech-to-Text & Insights)
│   ├── firebase/          # AuthManager & FirestoreManager
│   ├── models/            # Model Tape & CalendarDay
│   ├── network/           # Supabase Storage Uploader
│   ├── ui/                # Activities & Dialogs
│   │   ├── addtape/       # Dialog Rekam & Simpan Kaset Baru
│   │   ├── auth/          # Login & Registrasi Akun
│   │   ├── home/          # Beranda, Kalender & Shelf Kaset
│   │   ├── onboarding/    # Layar Pengenalan Pengguna Baru
│   │   ├── profile/       # Profil Pengguna & Preferensi Tema
│   │   ├── splash/        # Splash Screen
│   │   └── tapedetail/    # Detail Isi Kaset, Audio & AI Insight
│   └── utils/             # ThemeHelper, LoadingOverlay
└── res/
    ├── drawable/          # Aset Grafis Kaset Vintage & Ikon
    ├── layout/            # Antarmuka Layar & Komponen UI
    ├── values/            # Palet Warna, Style, & String (Light Mode)
    └── values-night/      # Palet Warna Studio Malam (Dark Mode)
```

---

## 🚀 Panduan Menjalankan Proyek (Getting Started)

### 1. Clone Repository
```bash
git clone https://github.com/adellyasetyaningsih/Re-memoria.git
cd Re-memoria
```

### 2. Buka di Android Studio
* Buka Android Studio > Pilih **Open** > Pilih folder `Re-memoria`.
* Tunggu proses *Gradle Sync* hingga selesai.

### 3. Konfigurasi Kredensial & API Keys
1. **Atur API Key di `local.properties`:**  
   Buka atau buat file `local.properties` di root folder proyek, lalu tambahkan:
   ```properties
   GROQ_API_KEY=gsk_your_groq_api_key_here
   SUPABASE_URL=https://your-supabase-id.supabase.co
   SUPABASE_KEY=your_supabase_publishable_key
   ```
   *(File `local.properties` otomatis diabaikan oleh `.gitignore` sehingga aman dari publik).*

2. **Konfigurasi Firebase (`google-services.json`):**  
   Salin file `app/google-services.json.example` menjadi `app/google-services.json` dan isi dengan konfigurasi Firebase project milikmu dari Firebase Console.

### 4. Build & Run
* Sambungkan perangkat Android fisik (aktifkan USB Debugging) atau gunakan Android Emulator.
* Klik tombol **Run ▶ (Shift + F10)** pada Android Studio.

---

## 📦 Build File APK
Untuk menghasilkan file APK debug siap pasang di HP:
```bash
./gradlew assembleDebug
```
File APK akan terbentuk di direktori:
```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 👩‍💻 Author
Dibuat dengan ❤️ oleh **[Adellya Setyaningsih](https://github.com/adellyasetyaningsih)**.
