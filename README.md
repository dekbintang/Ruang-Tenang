<div align="center">
  
  # 🍃 Ruang Tenang
  **Aplikasi Regulasi Emosi Mandiri Berbasis Fitur Self-Affirmation dan Histori Jurnal Terintegrasi.**

  <p align="center">
    Aplikasi Android yang dirancang untuk membantu pengguna mengelola emosi, mencatat perjalanan perasaan mereka melalui jurnal, menemukan afirmasi positif, dan berkonsultasi dengan profesional.
  </p>
  
</div>

---

## 👥 Tim Pengembang

Proyek ini dikembangkan oleh:

| Nama | NIM |
| :--- | :--- |
| **Trio Suro Wibowo** | `2405551168` |
| **I Kadek Bintang Adi Bimantara** | `2405551049` |
| **Richard Christian Mozart Diazoni** | `2405551019` |
| **I Komang Cahya Kertha Yasa** | `2405551034` |

---

## ✨ Fitur Utama

- 📖 **Jurnal Perasaan (CRUD)**: Tulis, baca, perbarui, dan hapus catatan harian atau jurnal perasaanmu. Semua data tersimpan aman secara lokal menggunakan **Room Database**.
- 🌟 **Self-Affirmation**: Temukan kutipan afirmasi positif harian untuk memotivasi dan menenangkan pikiran.
- 💬 **Konseling Online**: Terhubung dengan psikolog dan konselor. Dilengkapi dengan simulasi alur pembayaran dan **Ruang Chat** interaktif (riwayat chat tersimpan lokal).
- 📰 **Ruang Baca (Artikel)**: Baca artikel-artikel terbaru terkait kesehatan mental dan regulasi emosi yang terintegrasi dengan API (GNews).
- 🎨 **UI/UX Modern & Menenangkan**: Desain antarmuka yang bersih, responsif, dan menggunakan palet warna *Navy Blue* yang menenangkan, dilengkapi dengan animasi yang mulus.

---

## 🛠️ Teknologi yang Digunakan

- **Bahasa Pemrograman**: Kotlin
- **Arsitektur**: MVVM (Model-View-ViewModel)
- **Database Lokal**: Room Database (SQLite)
- **Navigasi**: Android Navigation Component (`nav_graph`)
- **Tampilan Data**: RecyclerView dengan Adapter kustom
- **Asynchronous Programming**: Coroutines & LiveData/Flow
- **API Networking**: Retrofit / GNews API (untuk artikel)

---

## 📱 Struktur Navigasi Aplikasi

1. **Dashboard / Beranda**: Menampilkan sapaan, afirmasi harian, dan akses cepat ke fitur lain.
2. **Jurnal**: Daftar riwayat jurnal (RecyclerView) dengan fitur tambah catatan baru.
3. **Konseling**: Daftar psikolog, lengkap dengan badge rating dan pengalaman.
   - **Payment**: Halaman konfirmasi dan simulasi pembayaran.
   - **Chat Room**: Ruang obrolan dengan psikolog dengan balasan otomatis interaktif dan riwayat yang tersimpan.
4. **Ruang Baca**: Daftar artikel edukatif seputar kesehatan mental.
5. **Profil**: Informasi pengguna dan pengaturan aplikasi.

---

## 🚀 Cara Menjalankan Proyek

1. **Clone repositori ini** ke mesin lokal Anda:
   ```bash
   git clone https://github.com/username/RuangTenang.git
   ```
2. Buka proyek menggunakan **Android Studio** (Disarankan versi terbaru seperti *Iguana* atau *Jellyfish*).
3. Tunggu hingga proses **Gradle Sync** selesai.
4. Pastikan Anda memiliki emulator (AVD) yang berjalan atau perangkat fisik Android yang terhubung (dengan *USB Debugging* aktif).
5. Klik tombol **Run** (▶️) di Android Studio atau tekan `Shift + F10`.

---

<div align="center">
  <i>Untuk kesehatan mental yang lebih baik.</i>
</div>
